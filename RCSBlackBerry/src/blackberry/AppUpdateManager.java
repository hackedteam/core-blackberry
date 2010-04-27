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

	private static final int APP_TIMER_DELAY = 2000;
	private static AppUpdateManager instance = null;
	Hashtable appSet = new Hashtable();

	private AppUpdateManager() {

	}

	public synchronized static AppUpdateManager getInstance() {
		if (instance == null) {
			// #debug debug
			debug.trace("creating instance");
			instance = new AppUpdateManager();
		}
		// #debug debug
		debug.trace("returning instance");
		return instance;
	}

	public synchronized void run() {
		// #debug debug
		//debug.trace("run");

		AppListener appListener = AppListener.getInstance();

		Hashtable newSet = new Hashtable();
		Vector startedList = new Vector();
		Vector stoppedList = new Vector();

		// Hashtable changes = new Hashtable();
		boolean haveChanges = false;

		ApplicationManager manager = ApplicationManager.getApplicationManager();

		// Check to see if application is running.
		ApplicationDescriptor descriptors[] = manager.getVisibleApplications();

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

	}

}
