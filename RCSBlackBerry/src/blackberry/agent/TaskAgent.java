package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

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

    }

    protected boolean parse(byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }
}
