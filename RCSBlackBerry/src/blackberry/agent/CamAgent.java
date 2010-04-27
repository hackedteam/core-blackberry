package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class CamAgent extends Agent {
    //#debug
    static Debug debug = new Debug("CamAgent", DebugLevel.VERBOSE);

    public CamAgent(final boolean agentStatus) {
        super(Agent.AGENT_CAM, agentStatus, true, "CamAgent");
    }

    protected CamAgent(final boolean agentStatus, final byte[] confParams) {
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
