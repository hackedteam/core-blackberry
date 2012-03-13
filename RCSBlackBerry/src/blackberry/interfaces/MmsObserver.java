//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.interfaces;

public interface MmsObserver extends Observer {
    void onNewMms(final byte[] byteMessage, String address,
            final boolean incomin);

}
