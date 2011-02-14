package com.rim.samples.device.bbminjectdemo;

import java.util.TimerTask;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.system.EventInjector.TrackwheelEvent;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;

public class ApplicationCheck extends TimerTask {
	// #ifdef DEBUG
	private static Debug debug = new Debug("ApplicationCheck",
			DebugLevel.VERBOSE);
	// #endif

	// boolean exitBBM = false;
	boolean exitBrowser = false;

	public void run() {
		ApplicationManager manager = ApplicationManager.getApplicationManager();

		final int foregroundProcess = manager.getForegroundProcessId();

		if (manager.isSystemLocked()) {
			debug.trace("system locked");
			// return;
		}

		BBMMenuItem bbmMenu = BBMMenuItem.getInstance();

		// debug.trace("searching Messenger or Browser");
		ApplicationDescriptor[] apps = manager.getVisibleApplications();
		for (int i = 0; i < apps.length; i++) {
			if (apps[i].getName().indexOf("Messenger") >= 0 && ! bbmMenu.bbmInjected) {

				if (Backlight.isEnabled()) {
					continue;
				}

				if (manager.getProcessId(apps[i]) == foregroundProcess) {
					debug.info("messenger foreground");

					bbmMenu.addMenuBBM();

					Utils.sleep(100);
					KeyInjector.pressKey(Keypad.KEY_MENU);
					Utils.sleep(100);

					debug.trace("  messenger active screen: "
							+ UiApplication.getUiApplication()
									.getActiveScreen().getClass().getName());

					KeyInjector.pressRawKey('y');

					Utils.sleep(100);
					debug.info("pressing menu y");
					KeyInjector.trackBallClick();

					Utils.sleep(100);
					bbmMenu.removeMenuBBM();

					// exitBBM = true;

				} else {
					debug.trace("Messenger to foreground: "
							+ apps[i].getModuleName());

					int pid = manager.getProcessId(apps[i]);
					manager.requestForeground(pid);
					// manager.runApplication(apps[i]);

				}

			} else if (apps[i].getName().indexOf("Browser") >= 0
					&& !exitBrowser) {
				if (manager.getProcessId(apps[i]) == foregroundProcess) {
					debug.info("Browser foreground");

					UiApplication.getUiApplication().getActiveScreen()
							.getUiEngine().suspendPainting(true);

					KeyInjector.pressRawKeyCode(Keypad.KEY_MENU);
					// Utils.sleep(100);
					KeyInjector.pressRawKey('z');
					// Utils.sleep(500);
					KeyInjector.trackBallClick();

					UiApplication.getUiApplication().getActiveScreen()
							.getUiEngine().suspendPainting(false);
					exitBrowser = true;

				}
			}

		}
	}

}
