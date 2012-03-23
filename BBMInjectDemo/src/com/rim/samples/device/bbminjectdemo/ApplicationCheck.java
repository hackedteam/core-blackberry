package com.rim.samples.device.bbminjectdemo;

import java.util.TimerTask;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;

public class ApplicationCheck extends TimerTask {
	//#ifdef DEBUG
	private static Debug debug = new Debug("ApplicationCheck",
			DebugLevel.VERBOSE);
	// #endif

	// boolean exitBBM = false;
	boolean exitBrowser = false;

	public void run() {
	    debug.trace("ApplicationCheck run");
		ApplicationManager manager = ApplicationManager.getApplicationManager();

		final int foregroundProcess = manager.getForegroundProcessId();

		if (manager.isSystemLocked()) {
			debug.trace("system locked");
			return;
		}

		BBMMenuItem bbmMenu = BBMMenuItem.getInstance();

		// debug.trace("searching Messenger or Browser");
		ApplicationDescriptor[] apps = manager.getVisibleApplications();
		for (int i = 0; i < apps.length; i++) {
		    debug.trace(apps[i].getName());
			if (apps[i].getName().indexOf("Messenger") >= 0 && ! bbmMenu.isInjected()) {

				if (!Backlight.isEnabled()) {
					Backlight.enable(true);
				}

				if (manager.getProcessId(apps[i]) == foregroundProcess) {
					debug.info("messenger foreground");

					bbmMenu.addMenuBBM();

					Utils.sleep(500);
					KeyInjector.pressKey(Keypad.KEY_MENU);
					Utils.sleep(500);

					debug.trace("  messenger active screen: "
							+ UiApplication.getUiApplication()
									.getActiveScreen().getClass().getName());

					KeyInjector.trackBallUp(20);
					
					char key = BBMMenuItem.getKey();
					//KeyInjector.pressRawKey(key);	
					//Utils.sleep(500);
					//KeyInjector.pressKey(key);
					//Utils.sleep(500);
					//KeyInjector.trackBallUp(20);
					
					Utils.sleep(1000);
					debug.info("pressing menu y");
					KeyInjector.trackBallClick();

					Utils.sleep(500);
					bbmMenu.removeMenuBBM();

					// exitBBM = true;

				} else {
					debug.trace("Messenger to foreground: "
							+ apps[i].getModuleName());

					int pid = manager.getProcessId(apps[i]);
					manager.requestForeground(pid);
					// manager.runApplication(apps[i]);

				}

			} else if (apps[i].getName().indexOf("BrowserNOTNOW") >= 0
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
