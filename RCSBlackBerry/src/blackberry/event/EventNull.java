//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.event;

import blackberry.config.ConfEvent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class EventNull extends Event {
    //#ifdef DEBUG
    private static Debug debug = new Debug("EventNull", DebugLevel.VERBOSE);

    //#endif

    protected boolean parse(ConfEvent event) {
        //#ifdef DEBUG
        debug.trace("parse");
        //#endif
        return true;
    }

    protected void actualLoop() {
        //#ifdef DEBUG
        debug.trace("actualLoop");
        //#endif
    }

}
