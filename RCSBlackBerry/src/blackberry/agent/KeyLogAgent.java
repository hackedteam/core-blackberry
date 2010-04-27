package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class KeyLogAgent extends Agent {
    //#debug
    static Debug debug = new Debug("KeyLogAgent", DebugLevel.VERBOSE);

    public KeyLogAgent(final boolean agentStatus) {
        super(Agent.AGENT_KEYLOG, agentStatus, true, "KeyLogAgent");

    }

    protected KeyLogAgent(final boolean agentStatus, final byte[] confParams) {
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
