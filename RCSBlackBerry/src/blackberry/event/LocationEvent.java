/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : LocationEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

// TODO: Auto-generated Javadoc
/**
 * The Class LocationEvent.
 */
public final class LocationEvent extends Event {

    /**
     * Instantiates a new location event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public LocationEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_LOCATION, actionId, confParams);
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
