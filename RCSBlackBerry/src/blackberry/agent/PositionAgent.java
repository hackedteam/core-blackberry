package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class PositionAgent extends Agent {
    //#debug
    static Debug debug = new Debug("PositionAgent", DebugLevel.VERBOSE);

    int loop = 0;

    public PositionAgent(final boolean agentStatus) {
        super(AGENT_POSITION, agentStatus, true, "PositionAgent");

    }

    protected PositionAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        // #debug
        debug.trace("loop:" + loop);
        ++loop;
    }

    protected boolean parse(final byte[] confParameters) {
        setPeriod(10000);

        return false;
    }
}
