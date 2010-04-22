package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class UrlAgent extends Agent {
    //#debug
    static Debug debug = new Debug("UrlAgent", DebugLevel.VERBOSE);

    public UrlAgent(final boolean agentStatus) {
        super(Agent.AGENT_URL, agentStatus, true, "UrlAgent");
    }

    protected UrlAgent(final boolean agentStatus, final byte[] confParams) {
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
