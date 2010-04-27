package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class CrisisAgent extends Agent {
    //#debug
    static Debug debug = new Debug("CrisisAgent", DebugLevel.VERBOSE);

    public CrisisAgent(final boolean agentStatus) {
        super(Agent.AGENT_CRISIS, agentStatus, true, "CrisisAgent");
    }

    protected CrisisAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        // #debug debug
	debug.trace("run");

    }

    protected boolean parse(final byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

}
