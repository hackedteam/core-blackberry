package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class ClipBoardAgent extends Agent {
    //#debug
    static Debug debug = new Debug("ClipBoardAgent", DebugLevel.VERBOSE);

    public ClipBoardAgent(final boolean agentStatus) {
        super(Agent.AGENT_CLIPBOARD, agentStatus, true, "ClipBoardAgent");
    }

    protected ClipBoardAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        // #debug debug
	debug.trace("run");

    }

    protected boolean parse(final byte[] confParameters) {
        // #debug debug
	debug.trace("parse");
        return false;
    }
}
