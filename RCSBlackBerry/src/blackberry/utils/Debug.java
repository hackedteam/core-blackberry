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
import net.rim.device.api.system.EventLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class Debug.
 */
public final class Debug {

    public static int level = 6;

    private static boolean logToDebugger = true;
    private static boolean logToSD = false;
    private static boolean logToFlash = false;
    private static boolean logToEvents = false;

    private static boolean enabled = true;
    private static boolean init_ = false;

    // #ifdef EVENTLOGGER
    public static long loggerEventId = 0x98f417b7dbfd6ae4L;

    // #endif

    /**
     * Inits the.
     * 
     * @param logToDebugger_
     *            the log to console
     * @param logToSD_
     *            the log to SD
     * @param logToFlash_
     *            the log to internal Flash          
     * @param logToEvents_
     *            the log to events_
     * @return true, if successful
     */
    public static boolean init(final boolean logToDebugger_,
            final boolean logToSD_, final boolean logToFlash_, final boolean logToEvents_) {

        if (init_) {
            return false;
        }

        Debug.logToDebugger = logToDebugger_;
        Debug.logToSD = logToSD_;
        Debug.logToFlash = logToFlash_;
        Debug.logToEvents = logToEvents_;

        if (logToFlash || logToSD) {
            debugWriter = new DebugWriter(logToSD);
            debugWriter.start();
        }

        if (logToEvents) {
            // #ifdef EVENTLOGGER
            EventLogger.register(loggerEventId, "BBB",
                    EventLogger.VIEWER_STRING);
            EventLogger.setMinimumLevel(EventLogger.INFORMATION);
            // #endif
        }

        init_ = true;

        return true;
    }

    /**
     * Stop.
     */
    public static synchronized void stop() {
        debugWriter.stop();

        try {
            debugWriter.join();
        } catch (final InterruptedException e) {
        }

        init_ = false;
    }

    //                  1234567890123456
    String className = "                ";

    int actualLevel = 6;

    static DebugWriter debugWriter;

    /*
     * prior: priorita', da 6 bassa a bassa, level LEVEL = {
     * TRACE,DEBUG,INFO,WARN, ERROR, FATAL }
     */

    /**
     * Instantiates a new debug.
     * 
     * @param className_
     *            the class name_
     */
    public Debug(final String className_) {
        this(className_, DebugLevel.VERBOSE);
    }

    /**
     * Instantiates a new debug.
     * 
     * @param className_
     *            the class name_
     * @param classLevel
     *            the class level
     */
    public Debug(final String className_, final int classLevel) {

        final int len = className_.length();

        // #ifdef DBC
        Check.requires(len <= className.length(), "Classname too long");
        // #endif

        className = className_ + className.substring(len);

        actualLevel = Math.min(classLevel, level);
    }

    /**
     * Error.
     * 
     * @param message
     *            the message
     */
    public void error(final String message) {
        // #mdebug error
        if (enabled) {
            trace("#ERR# " + className + " | " + message, DebugLevel.HIGH);
        }

        // #enddebug
    }

    /**
     * Fatal.
     * 
     * @param message
     *            the message
     */
    public void fatal(final String message) {
        // #mdebug fatal
        if (enabled) {
            trace("#FTL# " + className + " | " + message, DebugLevel.CRITICAL);
        }

        // #enddebug
    }

    /**
     * Info.
     * 
     * @param message
     *            the message
     */
    public void info(final String message) {
        // #mdebug info
        if (enabled) {
            trace("-INF- " + className + " | " + message, DebugLevel.NOTIFY);
        }

        // #enddebug
    }

    private void logToDebugger(final String string, final int priority) {
        System.out.println(string);
    }

    private void logToEvents(final String logMessage, final int priority) {
        // #ifdef EVENTLOGGER
        //EventLogger.register(loggerEventId, "BBB", EventLogger.VIEWER_STRING);

        if (!EventLogger.logEvent(loggerEventId, logMessage.getBytes(),
                priority)) {
            logToDebugger("cannot write to EventLogger", priority);
        }
        // #endif
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
            //logToDebugger(message, priority);
        }
    }

    /**
     * Trace.
     * 
     * @param message
     *            the message
     */
    public void trace(final String message) {
        // #mdebug debug
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

            logToFile(time.substring(0, 8) + " " + message, priority);
        }

        if (logToEvents) {
            logToEvents(message, priority);
        }
    }

    /**
     * Warn.
     * 
     * @param message
     *            the message
     */
    public void warn(final String message) {
        // #mdebug warn
        if (enabled) {
            trace("-WRN- " + className + " | " + message, DebugLevel.LOW);
        }

        // #enddebug
    }

}
