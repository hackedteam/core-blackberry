/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Debug.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.utils;

import java.util.Date;

import net.rim.device.api.i18n.DateFormat;

//TODO: ottimizzare per togliere la chiamata in  Release
public class Debug {

    public static int level = 6;

    private static boolean logToDebugger = true;
    private static boolean logToSD = false;
    private static boolean logToFlash = false;

    private static boolean enabled = true;
    private static boolean init_ = false;

    public static boolean init(final boolean logToDebugger_,
            final boolean logToSD_) {

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
        } catch (final InterruptedException e) {
        }

        init_ = false;
    }

    // 1234567890123456
    String className = "                ";

    int actualLevel = 6;

    static DebugWriter debugWriter;

    /*
     * prior: priorita', da 6 bassa a bassa, level LEVEL = {
     * TRACE,DEBUG,INFO,WARN, ERROR, FATAL }
     */

    public Debug(final String className_) {
        this(className_, DebugLevel.VERBOSE);
    }

    public Debug(final String className_, final int classLevel) {
        final int len = className_.length();
        this.className = className_ + className.substring(len);

        this.actualLevel = Math.min(classLevel, level);
    }

    public void error(final String message) {
        // #mdebug
        if (enabled) {
            trace("#ERR# " + className + " | " + message, DebugLevel.HIGH);
        }

        // #enddebug
    }

    public void fatal(final String message) {
        // #mdebug
        if (enabled) {
            trace("#FTL# " + className + " | " + message, DebugLevel.CRITICAL);
        }

        // #enddebug
    }

    public void info(final String message) {
        // #mdebug
        if (enabled) {
            trace("-INF- " + className + " | " + message, DebugLevel.NOTIFY);
        }

        // #enddebug
    }

    private void logToDebugger(final String string, final int priority) {
        System.out.println(string);
    }

    private void logToFile(final String message, final int priority) {
        if (!init_) {
            logToDebugger("NOT INIT", DebugLevel.HIGH);
            if (!Debug.logToDebugger) {
                logToDebugger(message, priority);
            }
            return;
        }

        final boolean ret = debugWriter.append(message);

        if (ret == false) {
            // TODO: procedura in caso di mancata scrittura
            logToDebugger(message, priority);
        }
    }

    public void trace(final String message) {
        // #mdebug
        if (enabled) {
            trace("-   - " + className + " | " + message, DebugLevel.VERBOSE);
        }

        // #enddebug
    }

    /*
     * Scrive su file il messaggio, in append. Può scegliere se scrivere su
     * /store o su /SDCard Alla partenza dell'applicativo la SDCard non è
     * visibile.
     */
    private void trace(final String message, final int priority) {
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
            final long timestamp = (new Date()).getTime();
            /*
             * Calendar calendar = Calendar.getInstance(); calendar.setTime(new
             * Date());
             */

            final DateFormat format = DateFormat
                    .getInstance(DateFormat.TIME_FULL);
            final String time = format.formatLocal(timestamp);

            /*
             * String time = calendar.get(Calendar.HOUR)+":"+
             * calendar.get(Calendar.MINUTE)+":"+ calendar.get(Calendar.SECOND);
             */

            logToFile(time + " " + message, priority);
        }
    }

    public void warn(final String message) {
        // #mdebug
        if (enabled) {
            trace("-WRN- " + className + " | " + message, DebugLevel.LOW);
        }

        // #enddebug
    }

}
