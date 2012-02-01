//#preprocess
package blackberry.event;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import blackberry.Messages;
import blackberry.config.ConfEvent;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Markup;

public class EventAfterinst extends Event {

    //#ifdef DEBUG
    private static Debug debug = new Debug("EventAfterinst", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    private int days;
    private Date date;

    protected boolean parse(ConfEvent conf) {
        try {
            days = conf.getInt(Messages.getString("z.0")); //$NON-NLS-1$
            Markup markup = new Markup(this);
            Date now = new Date();
            if (markup.isMarkup()) {
                date = (Date) markup.readDate();
                //#ifdef DEBUG
                debug.trace("parse, reading markup: " + date); //$NON-NLS-1$
                //#endif
            } else {
                date = now;

                //#ifdef DEBUG
                debug.trace("parse, writing markup: " + now); //$NON-NLS-1$
                //#endif

                markup.write(date);
            }

        } catch (ConfigurationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("parse"); //$NON-NLS-1$
            //#endif
            return false;
        }
        return true;
    }

    protected void actualStart() {

        //#ifdef DEBUG
        debug.trace("actualStart"); //$NON-NLS-1$
        //#endif
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$

        long nowMillis = calendar.getTime().getTime();
        calendar.setTime(date);
        long instMillis = calendar.getTime().getTime();
        int daysInMillis = days * 24 * 60 * 60 * 1000;

        long delay = instMillis + daysInMillis - nowMillis;

        if (delay > 0) {
            //#ifdef DEBUG
            debug.trace("actualStart set delay: " + delay); //$NON-NLS-1$
            //#endif

            setDelay(delay);
            setPeriod(NEVER);
            reschedule();

        } else {
            //#ifdef DEBUG
            debug.trace("actualStart set soon delay"); //$NON-NLS-1$
            //#endif
            setDelay(SOON);
            setPeriod(NEVER);
            reschedule();
        }

    }

    protected void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop"); //$NON-NLS-1$
        //#endif
        onExit(); // di sicurezza
    }

    protected void actualLoop() {
        //#ifdef DEBUG
        debug.trace("actualLoop"); //$NON-NLS-1$
        //#endif
        onEnter();
    }

}
