//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : ConnectionEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

// TODO: Auto-generated Javadoc
/**
 * The Class ConnectionEvent.
 */
public final class ConnectionEvent extends Event {

    /**
     * Instantiates a new connection event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public ConnectionEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_CONNECTION, actionId, confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see blackberry.event.Event#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        // TODO Auto-generated method stub
        return false;
    }

}
