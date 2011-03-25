package com.rim.samples.device.bbminjectdemo;

import java.util.Date;

public class Line {
	User user;
	String message;
	static String subject = "";
	static String program = "bbm";
	Date timestamp;

	public Line(User user, String message) {
		this.user = user;
		this.message = message;
		timestamp = new Date();
	}

	public int hashCode() {
		return user.hashCode() ^ message.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof Line)) {
			return false;
		}
		
		Line line = (Line) obj;
		return user.equals(line.user) && message.equals(line.message);

	}

}
