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
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.StartStopThread;
import com.ht.rcs.blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class EventManager.
 */
public final class EventManager extends Manager implements Singleton {

    /** The debug. */
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

   
    /*public boolean enabled(int id)
    {
        return statusObj.isValidEvent(id);
    }
    */
    public Vector getAllItems() {
        Vector events = statusObj.getEventsList();
        return events;
    }

    public StartStopThread getItem(int id) {
        Event event = statusObj.getEvent(id);
        Check.ensures(event.eventId == id, "Wrong id");
        return event;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.ht.rcs.blackberry.Manager#Start(int)
     */
    /*public synchronized boolean startOld(int eventId) {
        if (!enabled(eventId)) {
            debug.error("EventManager Start FAILED [0] " + eventId);
            return false;
        }

        Event event = statusObj.getEvent(eventId);

        if (event == null) {
            debug.error("event unknown: " + eventId);
            return false;
        }

        event.start();
        debug.trace("Start() OK: " + event);
        return true;
    }
*/
    /*
     * (non-Javadoc)
     * 
     * @see com.ht.rcs.blackberry.Manager#StartAll()
     */
   /* public synchronized boolean startAllOld() {
        Vector events = statusObj.getEventsList();

        for (int i = 0; i < events.size(); i++) {            
            Event event = (Event) events.elementAt(i);
            Check.asserts(event.eventId == i, "Wrong eventId");
            event.start();     
            Utils.sleep(100);
        }

        debug.trace("StartAll() OK");
        return true;
    }*/

    /*
     * (non-Javadoc)
     * 
     * @see com.ht.rcs.blackberry.Manager#Stop(int)
     */
   /* public synchronized int stopOld(int eventId) {
        if (statusObj.stopEvent(eventId) == false) {
            debug.trace("StopEvent() Event already stopped");
            return Common.EVENT_STOPPED;
        }

        Event event = statusObj.getEvent(eventId);

        try {
            event.join();
        } catch (InterruptedException e) {
            debug.error("Interrupted " + eventId);
        }

        boolean ret = statusObj.reEnableAgent(eventId);
        return ret ? 1 : 0;
    }*/

    // CRITICAL - L'handle del thread dell'agente verra' chiuso soltanto dalla
    // StopAgents() al primo
    // reload della backdoor. Da qui non abbiamo modo di sapere quale sia
    // l'handle del thread associato
    // a questo agente, quindi possiamo soltanto comandargli lo stop.
    /*
     * (non-Javadoc)
     * 
     * @see com.ht.rcs.blackberry.Manager#StopAll()
     */
  /*  public synchronized int stopAllOld() {
        Vector events = statusObj.getEventsList();

        for (int i = 0; i < events.size(); i++) {
            stop(i);
        }

        debug.trace("StopAll() OK\n");
        return 0;
    }*/



}
