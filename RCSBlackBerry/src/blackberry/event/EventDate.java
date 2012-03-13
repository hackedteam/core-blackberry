//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.event;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import blackberry.Messages;
import blackberry.config.ConfEvent;
import blackberry.config.ConfigurationException;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class EventDate extends Event {
    //#ifdef DEBUG
    private static Debug debug = new Debug("EventDate", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    private Date dateFrom;
    private Date dateTo;

    Calendar start;
    Calendar stop;

    private boolean nextDailyIn;
    private boolean needExit = false;

    protected boolean parse(ConfEvent event) {

        try {
            needExit = false;
            //x.1=datefrom
            dateFrom = conf.getDate(Messages.getString("x.1")); //$NON-NLS-1$

            // x.2=dateto
            if (conf.has(Messages.getString("x.2"))) { //$NON-NLS-1$
                //x.3=dateto
                dateTo = conf.getDate(Messages.getString("x.3")); //$NON-NLS-1$
            } else {
                dateTo = new Date(Long.MAX_VALUE);
            }
        } catch (ConfigurationException e) {
            return false;
        }

        return true;
    }

    public void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart"); //$NON-NLS-1$
        //#endif
        start = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
        start.setTime(dateFrom);
        stop = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
        stop.setTime(dateTo);

        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
        //#ifdef DEBUG
        debug.trace("actualStart from: " + start.getTime() + " to: " //$NON-NLS-1$ //$NON-NLS-2$
                + stop.getTime() + " now: " + now.getTime()); //$NON-NLS-1$
        //#endif

        if (now.before(start)) {
            //#ifdef DEBUG
            debug.trace(" (actualStart): not yet in the brackets"); //$NON-NLS-1$
            //#endif
            nextDailyIn = setDailyDelay();
            //#ifdef DBC
            Check.asserts(nextDailyIn == true, "nextDailyIn should be true"); //$NON-NLS-1$
            //#endif
        } else if (now.before(stop)) {
            //#ifdef DEBUG
            debug.trace(" (actualStart): already in the brackets, don't reschedule, let's go"); //$NON-NLS-1$
            //#endif
            nextDailyIn = true;

        } else {
            //#ifdef DEBUG
            debug.trace(" (actualStart): nothing to do"); //$NON-NLS-1$
            //#endif
            //#ifdef DBC
            Check.asserts(nextDailyIn == false, "nextDailyIn should be false"); //$NON-NLS-1$
            //#endif
        }
    }

    public void actualLoop() {
        //#ifdef DEBUG
        debug.trace("actualLoop"); //$NON-NLS-1$
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
        //#ifdef DEBUG
        debug.trace("actualStop"); //$NON-NLS-1$
        //#endif

        if (needExit) {
            onExit(); // di sicurezza
        }
    }

    private boolean setDailyDelay() {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
    
        //#ifdef DEBUG
        debug.trace("setDailyDelay start: " + start.getTime().getTime()); //$NON-NLS-1$
        debug.trace("setDailyDelay stop: " + start.getTime().getTime()); //$NON-NLS-1$
        //#endif
        long period;
        if (now.before(start)) {
            period = (start.getTime().getTime() - now.getTime().getTime());
            //#ifdef DEBUG
            debug.trace("setDailyDelay (now before start) new period:" + period); //$NON-NLS-1$
            //#endif
            setDelay(period);
            reschedule();
            return true;
        } else if (now.before(stop)) {
            period = (stop.getTime().getTime() - now.getTime().getTime());
            //#ifdef DEBUG
            debug.trace("setDailyDelay (now before stop) new period:" + period); //$NON-NLS-1$
            //#endif
            setDelay(period);
            reschedule();
            return false;
        } else {
            this.onExit();
            setDelay(NEVER);
            reschedule();
            return false;
        }
    }

}
