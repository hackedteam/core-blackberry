//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	

package blackberry.interfaces;

public interface PhoneCallObserver extends Observer {
    public void onCallIncoming(int callId, String phoneNumber);

    public void onCallDisconnected(int callId, String phoneNumber);

    public void onCallConnected(int callId, String phoneNumber);

    public void onCallAnswered(int callId, String phoneNumber);

    public void onCallInitiated(int callId, String phoneNumber);
}
