package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class PositionAgent extends Agent {
    //#debug
    static Debug debug = new Debug("PositionAgent", DebugLevel.VERBOSE);

    public PositionAgent(boolean agentStatus) {
        super(AGENT_POSITION, agentStatus, true, "PositionAgent");
        
    }

    protected PositionAgent(boolean agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    int loop = 0;

    public void actualRun() {
        // #debug
        debug.trace("loop:" + loop);
        ++loop;
    }

    protected boolean parse(byte[] confParameters) {
        setPeriod(10000);
        
        return false;
    }
}
