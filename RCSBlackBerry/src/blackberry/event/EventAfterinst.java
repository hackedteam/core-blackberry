//#preprocess
package blackberry.event;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import blackberry.config.ConfEvent;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Markup;


public class EventAfterinst extends Event {

    //#ifdef DEBUG
    private static Debug debug = new Debug("EventAfterinst", DebugLevel.VERBOSE);
    //#endif
    
	private int days;
	private Date date;

	protected boolean parse(ConfEvent conf) {
		try {
			days = conf.getInt("days");
			Markup markup = new Markup(this);
			Date now = new Date();
			if (markup.isMarkup()) {			   
				date = (Date) markup.readDate();
				 //#ifdef DEBUG
                debug.trace("parse, reading markup: " + date);
                //#endif
			} else {
			    //#ifdef DEBUG
                debug.trace("parse, writing markup");
                //#endif
				date = now;
				markup.write(date);
			}

		} catch (ConfigurationException e) {
		    //#ifdef DEBUG
            debug.error(e);
            debug.error("parse");
            //#endif
			return false;
		}
		return true;
	}

	protected void actualStart() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		
		long nowMillis = calendar.getTime().getTime();		
		calendar.setTime(date);
		long instMillis = calendar.getTime().getTime();
		int daysInMillis = days * 24 * 60 * 60 * 1000;
				
		long delay = instMillis + daysInMillis - nowMillis;

		if(delay>0){
		    //#ifdef DEBUG
            debug.trace("actualStart set delay: " + delay);
            //#endif
			setDelay(delay);
			setPeriod(NEVER);
		}else{
		    //#ifdef DEBUG
            debug.trace("actualStart set soon delay");
            //#endif
			setDelay(SOON);
		}	
	}

	protected void actualStop() {
	    //#ifdef DEBUG
        debug.trace("actualStop");
        //#endif
		onExit(); // di sicurezza
	}

	protected void actualGo() {
	    //#ifdef DEBUG
        debug.trace("actualGo");
        //#endif
		onEnter();
	}

}
