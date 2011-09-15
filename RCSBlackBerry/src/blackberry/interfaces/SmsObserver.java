//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	

package blackberry.interfaces;


public interface SmsObserver extends Observer{

    void onNewSms(byte[] dataMsg, String address, boolean incoming);

}
