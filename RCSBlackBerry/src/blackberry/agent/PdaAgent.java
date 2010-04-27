package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class PdaAgent extends Agent {
    //#debug
    static Debug debug = new Debug("PdaAgent", DebugLevel.VERBOSE);

    public PdaAgent(final boolean agentStatus) {
        super(Agent.AGENT_PDA, agentStatus, true, "PdaAgent");
    }

    protected PdaAgent(final boolean agentStatus, final byte[] confParams) {
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
