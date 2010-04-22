package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class MicAgent extends Agent {
    //#debug
    static Debug debug = new Debug("MicAgent", DebugLevel.VERBOSE);

    public MicAgent(final boolean agentStatus) {
        super(Agent.AGENT_MIC, agentStatus, true, "MicAgent");

    }

    protected MicAgent(final boolean agentStatus, final byte[] confParams) {
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
