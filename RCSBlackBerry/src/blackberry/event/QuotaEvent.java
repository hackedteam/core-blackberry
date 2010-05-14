//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : QuotaEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

// TODO: Auto-generated Javadoc
/**
 * The Class QuotaEvent.
 */
public final class QuotaEvent extends Event {

    /**
     * Instantiates a new quota event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public QuotaEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_QUOTA, actionId, confParams);
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
