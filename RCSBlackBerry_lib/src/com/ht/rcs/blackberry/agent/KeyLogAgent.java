package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class KeyLogAgent extends Agent {
    static Debug debug = new Debug("KeyLogAgent", DebugLevel.VERBOSE);

    public KeyLogAgent(int agentStatus) {
        super(Agent.AGENT_KEYLOG, agentStatus, true);

    }

    protected KeyLogAgent(int agentStatus, byte[] confParams) {
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
