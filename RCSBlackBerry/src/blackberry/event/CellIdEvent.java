/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : CellIdEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

// TODO: Auto-generated Javadoc
/**
 * The Class CellIdEvent.
 */
public final class CellIdEvent extends Event {

    /**
     * Instantiates a new cell id event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public CellIdEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_CELLID, actionId, confParams);
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
