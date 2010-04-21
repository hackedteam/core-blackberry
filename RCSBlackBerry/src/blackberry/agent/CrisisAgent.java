package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class CrisisAgent extends Agent {
	//#debug
    static Debug debug = new Debug("CrisisAgent", DebugLevel.VERBOSE);

    public CrisisAgent(boolean agentStatus) {
        super(Agent.AGENT_CRISIS, agentStatus, true, "CrisisAgent");
    }

    protected CrisisAgent(boolean agentStatus, byte[] confParams) {
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
