//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	

package blackberry.debug;

public class LogLine {
    public LogLine(String message, int level, boolean error) {
        this.message=message;
        this.level=level;
        this.error=error;
    }
    String message;
    int level;
    boolean error;
}
