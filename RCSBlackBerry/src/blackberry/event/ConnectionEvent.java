//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : ConnectionEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class ConnectionEvent.
 */
public final class ConnectionEvent extends Event {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ConnectionEvent",
            DebugLevel.VERBOSE);
    //#endif
    
    int actionOnEnter;
    int actionOnExit;
    /**
     * Instantiates a new connection event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public ConnectionEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_CONNECTION, actionId, confParams, "ConnectionEvent");
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
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);
        try {
            actionOnEnter = actionId;
            actionOnExit = databuffer.readInt();
        } catch (final EOFException e) {
            return false;
        }
        return true;
    }

}
