//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : TimerEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.io.EOFException;
import java.io.IOException;
import java.util.Date;

import net.rim.device.api.util.DataBuffer;
import blackberry.Status;
import blackberry.config.Conf;
import blackberry.config.Keys;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.log.Markup;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class TimerEvent.
 */
public final class TimerEvent extends Event {
    private static final int SLEEP_TIME = 1000;

    //#ifdef DEBUG
    private static Debug debug = new Debug("TimerEvent", DebugLevel.VERBOSE);

    //#endif

    int type;
    long loDelay;
    long hiDelay;

    Date timestamp;

    Markup markup;

    /**
     * Instantiates a new timer event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public TimerEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_TIMER, actionId, confParams, "TimerEvent");
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

        markup = new Markup(eventType, Keys.getInstance().getAesKey());
        init();
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {
        //#ifdef DEBUG
        debug.trace("actualRun");
        //#endif
        trigger();
    }

    private void init() {
        final long now = Utils.getTime();

        switch (type) {
        case Conf.CONF_TIMER_SINGLE:
            //#ifdef DEBUG
            debug.info("TIMER_SINGLE delay: " + loDelay);
            //#endif
            setDelay(loDelay);
            setPeriod(NEVER);
            break;
        case Conf.CONF_TIMER_REPEAT:
            //#ifdef DEBUG
            debug.info("TIMER_REPEAT period: " + loDelay);
            //#endif
            setPeriod(loDelay);
            setDelay(loDelay);
            break;
        case Conf.CONF_TIMER_DATE:
            long tmpTime = hiDelay << 32;
            tmpTime += loDelay;
            //#ifdef DEBUG
            Date date = new Date(tmpTime);
            debug.info("TIMER_DATE: " + date);
            //#endif

            setPeriod(NEVER);
            setDelay(tmpTime - now);
            break;
        case Conf.CONF_TIMER_DELTA:
            //#ifdef DEBUG
            debug.info("TIMER_DELTA");
            //#endif

            long deltaTime = hiDelay << 32;
            deltaTime += loDelay;

            // se la data di installazione non c'e' si crea.            
            if (!markup.isMarkup()) {
                final Date instTime = Status.getInstance().getStartingDate();
                markup.writeMarkup(Utils.longToByteArray(instTime.getTime()));
            }

            // si legge la data di installazione dal markup
            try {
                final long timeInst = Utils.byteArrayToLong(
                        markup.readMarkup(), 0);

                setPeriod(NEVER);
                final long delay = timeInst + deltaTime - now;
                if (delay > 0) {
                    setDelay(timeInst + deltaTime - now);
                } else {
                    //#ifdef DEBUG
                    debug.info("negative delta");
                    //#endif
                }
                //#ifdef DEBUG
                date = new Date(timeInst + deltaTime - now);
                debug.info("DELTA_DATE: " + date);
                //#endif

            } catch (final IOException e) {
                //#ifdef ERROR
                debug.error(e);
                //#endif
            }

            break;
        default:
            //#ifdef DEBUG
            debug.error("shouldn't be here");
            //#endif
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

            //#ifdef DEBUG
            debug.trace("type: " + type + " lo:" + loDelay + " hi:" + hiDelay);
            //#endif

        } catch (final EOFException e) {
            //#ifdef DEBUG
            debug.error("params FAILED");
            //#endif
            return false;
        }

        init();
        return true;
    }

}
