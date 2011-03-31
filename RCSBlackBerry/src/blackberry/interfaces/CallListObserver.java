//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	

package blackberry.interfaces;

import java.util.Date;

public interface CallListObserver extends Observer {
    public void callLogAdded(String number, String name, Date date,
            int duration, boolean outgoing, boolean missed);

}
