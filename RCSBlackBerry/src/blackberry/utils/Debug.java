//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Debug.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.utils;

import java.util.Date;

import blackberry.Conf;
import blackberry.agent.Agent;
import blackberry.config.Keys;
import blackberry.fs.Path;
import blackberry.log.Log;
import blackberry.log.LogType;

import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.system.EventLogger;

/**
 * The Class Debug.
 */
public final class Debug {

    public static int level = 6;

    static DebugWriter debugWriter;
    static Log logInfo;

    private static boolean logToDebugger = true;
    private static boolean logToSD = false;
    private static boolean logToFlash = false;
    private static boolean logToEvents = false;
    private static boolean logToInfo = false;

    private static boolean enabled = true;
    private static boolean init_ = false;

    //                  1234567890123456
    String className = "                ";

    int actualLevel = 6;

    //#ifdef EVENTLOGGER
    public static long loggerEventId = 0x98f417b7dbfd6ae4L;

    //#endif

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

        //#ifdef DBC
        Check.requires(className_ != null, "className_ void");
        Check.requires(className_.length() > 0, "className_ empty");
        //#endif

        final int len = className_.length();

        //#ifdef DBC
        Check.requires(len <= className.length(), "Classname too long");
        //#endif

        className = className_ + className.substring(len);
        actualLevel = Math.min(classLevel, level);

        //trace("Level: " + actualLevel);
    }

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
    public static boolean init() {
        //#ifdef DBC
        //Check.requires(Path.isInizialized(), "init: Path not initialized");
        //#endif

        if (init_) {
            return false;
        }

        Debug.logToDebugger = Conf.DEBUG_OUT;
        Debug.logToSD = Conf.DEBUG_OUT;
        Debug.logToFlash = Conf.DEBUG_FLASH;
        Debug.logToEvents = Conf.DEBUG_EVENTS;
        Debug.logToInfo = Conf.DEBUG_INFO;

        if (logToFlash || logToSD) {
            Path.makeDirs();
            debugWriter = DebugWriter.getInstance();
            debugWriter.logToSD = logToSD;
            if(!debugWriter.isAlive()){
                debugWriter.start();
            }
        }

        if (logToEvents) {
            //#ifdef EVENTLOGGER
            EventLogger.register(loggerEventId, "BBB",
                    EventLogger.VIEWER_STRING);
            EventLogger.setMinimumLevel(EventLogger.DEBUG_INFO);
            //#endif
        }

        init_ = true;
        return true;
    }

    /**
     * Stop.
     */
    public static synchronized void stop() {
        init_ = false;
        if (debugWriter != null) {
            debugWriter.requestStop();

            try {
                debugWriter.join();
            } catch (final InterruptedException e) {
            }
        }
    }

    /**
     * Trace.
     * 
     * @param message
     *            the message
     */
    public void trace(final String message) {
        //#ifdef DEBUG_TRACE
        if (enabled) {
            trace("-   - " + className + " | " + message, DebugLevel.VERBOSE);
        }
        //#endif
    }

    /**
     * Info.
     * 
     * @param message
     *            the message
     */
    public void info(final String message) {
        //#ifdef DEBUG_INFO
        if (enabled) {
            trace("-INF- " + className + " | " + message, DebugLevel.NOTIFY);
        }

        //#endif
    }

    /**
     * Warn.
     * 
     * @param message
     *            the message
     */
    public void warn(final String message) {
        //#ifdef DEBUG_WARN
        if (enabled) {
            trace("-WRN- " + className + " | " + message, DebugLevel.LOW);
        }

        //#endif
    }

    /**
     * Error.
     * 
     * @param message
     *            the message
     */
    public void error(final String message) {
        //#ifdef DEBUG_ERROR
        if (enabled) {
            trace("#ERR# " + className + " | " + message, DebugLevel.HIGH);
        }

        //#endif
    }

    /**
     * Error.
     * 
     * @param message
     *            the message
     */
    public void error(final Exception ex) {
        //#ifdef DEBUG_ERROR
        if (enabled) {
            trace("#ERR# " + className + " | " + ex, DebugLevel.HIGH);
            ex.printStackTrace();
        }

        //#endif
    }

    /**
     * Fatal.
     * 
     * @param message
     *            the message
     */
    public void fatal(final String message) {
        //#ifdef DEBUG_FATAL
        if (enabled) {
            trace("#FTL# " + className + " | " + message, DebugLevel.CRITICAL);
        }

        //#endif
    }

    public void fatal(final Exception ex) {
        //#ifdef DEBUG_FATAL
        if (enabled) {
            trace("#FTL# " + className + " | " + ex, DebugLevel.CRITICAL);
            ex.printStackTrace();
        }
        //#endif
    }

    private void logToDebugger(final String string, final int priority) {
        System.out.println(Thread.currentThread().getName() + " " + string);
    }

    private void logToEvents(final String logMessage, final int priority) {
        //#ifdef EVENTLOGGER
        //EventLogger.register(loggerEventId, "BBB", EventLogger.VIEWER_STRING);

        if (!EventLogger.logEvent(loggerEventId, logMessage.getBytes(),
                priority)) {
            logToDebugger("cannot write to EventLogger", priority);
        }
        //#endif
    }

    private void logToFile(final String message, final int priority) {

        //#ifdef DBC
        Check.requires(debugWriter != null, "logToFile: debugWriter null");
        Check.requires(logToFlash || logToSD, "! (logToFlash || logToSD)");
        //#endif

        final boolean ret = debugWriter.append(message);

        if (ret == false) {
            // procedura in caso di mancata scrittura
            if (Debug.logToDebugger) {
                logToDebugger("debugWriter.append returns false",
                        DebugLevel.ERROR);
            }
        }
    }

    private void logToInfo(final String message, final int priority) {
        //#ifdef DBC
        Check.requires(logToInfo, "!logToInfo");
        //#endif

        if (logInfo == null) {

            if (!Keys.isInstanced()) {
                return;
            }

            logInfo = new Log(Agent.AGENT_INFO, false, Keys.getInstance()
                    .getAesKey());
        }

        logInfo.createLog(null);
        logInfo.writeLog(message, true);
        logInfo.close();
    }

    /*
     * Scrive su file il messaggio, in append. Può scegliere se scrivere su
     * /store o su /SDCard Alla partenza dell'applicativo la SDCard non è
     * visibile.
     */
    private void trace(final String message, final int priority) {
        //#ifdef DBC
        Check.requires(priority > 0, "priority >0");
        //#endif

        if (priority > actualLevel || message == null) {
            return;
        }

        if (logToDebugger) {
            logToDebugger(message, priority);
        }

        if (!init_) {
            return;
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

        if (logToInfo) {
            if (priority <= DebugLevel.ERROR) {
                logToInfo(message, priority);
            }
        }
    }

    public static boolean sendLogs(final String email) {
        //#ifdef SEND_LOG
        if (logToFlash || logToSD) {
            return debugWriter.sendLogs(email);
        }
        //#endif
        return false;

    }

}
