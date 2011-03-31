//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	

package blackberry.interfaces;

import javax.wireless.messaging.Message;

public interface SmsObserver extends Observer{

    void onNewSms(Message message, boolean incoming);

}
