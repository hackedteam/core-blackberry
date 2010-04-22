package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class TaskAgent extends Agent {
    //#debug
    static Debug debug = new Debug("TaskAgent", DebugLevel.VERBOSE);

    public TaskAgent(final boolean agentStatus) {
        super(Agent.AGENT_TASK, agentStatus, true, "TaskAgent");

    }

    protected TaskAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        // #debug
        debug.trace("run");

    }

    protected boolean parse(final byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }
}
