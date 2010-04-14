package com.ht.tests;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.system.*;

/*
 * BlackBerry applications that provide a user interface
 * must extend UiApplication.
 */
public class Main extends UiApplication {
	//#debug
	static Debug debug = new Debug("Main", DebugLevel.VERBOSE);

	public static void main(String[] args) {

		boolean logToDebugger = true;
		boolean logToFlash = false;
		boolean logToSD = true;

		Utils.sleep(2000);
		// #mdebug
		Debug.init(logToDebugger, logToFlash);		
		debug.trace("Test Init");
		// #enddebug
		
		// create a new instance of the application
		// and start the application on the event thread
		Main theApp = new Main();
		theApp.enterEventDispatcher();

		// #debug
		debug.info("--- Starting Main ---");
	}

	public Main() {
		// display a new screen
		pushScreen(new TestScreen());
	}

	public void ExecuteApplication(String appname) {
		System.out.println("RCSBlackBerry Test launching");
		int handle = CodeModuleManager.getModuleHandle("RCSBlackBerry Test");
		ApplicationDescriptor[] descriptors = CodeModuleManager
				.getApplicationDescriptors(handle);
		if (descriptors.length > 0) {
			ApplicationDescriptor descriptor = descriptors[0];
			try {
				ApplicationManager manager = ApplicationManager
						.getApplicationManager();
				while (manager.inStartup()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
						// Ignore.
					}
				}
				manager.runApplication(descriptor);
			} catch (ApplicationManagerException e) {
				System.out.println("I couldn't launch it!");
				e.printStackTrace();
			}
		} else {
			System.out.println("RCSBlackBerry Test is not installed.");
		}

		System.out.println("Goodbye, world!");
	}
}

// create a new screen that extends MainScreen, which provides
// default standard behavior for BlackBerry applications
final class TestScreen extends MainScreen {
	//#debug
	static Debug debug = new Debug("TestScreen", DebugLevel.VERBOSE);

	public TestScreen() {

		// invoke the MainScreen constructor
		super();

		// add a title to the screen
		LabelField title = new LabelField("TestScreen", DrawStyle.ELLIPSIS
				| Field.USE_ALL_WIDTH);
		setTitle(title);

		add(new RichTextField("RCSBlackBerry TEST SUITE"));

		// Per ogni test presente, lo esegue e aggiunge il risultato
		Tests test = Tests.getInstance();

		for (int i = 0; i < test.getCount(); i++) {
			boolean result = test.execute(i);
			add(new RichTextField(test.result(i)));

			if (result == false) {
				// #debug
				debug.error("TEST FAILED " + i);
			}
		}
	}

	// override the onClose() method to display a dialog box to the user
	// with "Goodbye!" when the application is closed
	public boolean onClose() {
		Dialog.alert("Goodbye!");
		System.exit(0);
		return true;
	}
}
