package com.rim.samples.device.bbminjectdemo;

import java.util.Vector;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;

public class BrowserMenuItem extends ApplicationMenuItem {
	private static final String BROWSER_MENU = "Zend Menu";
	// #ifdef DEBUG
	private static Debug debug = new Debug("BrowserMenuItem",
			DebugLevel.VERBOSE);
	// #endif
	static BrowserMenuItem instance;

	Screen browserScreen;
	String lastUrl;

	static BrowserMenuItem getInstance() {
		if (instance == null)
			instance = new BrowserMenuItem(20);

		return instance;
	}

	public BrowserMenuItem(int arg0) {
		super(arg0);
	}

	public Object run(Object context) {
		try {

			if (t == null) {
				UiApplication app = UiApplication.getUiApplication();
				browserScreen = app.getActiveScreen();

				pressMenuThread();
			}

			if(context == null){
				debug.trace("NO URL: " + context);
				return null;
			}
			
			String url = (String) context;

			if (!url.equals(lastUrl)) {
				lastUrl = url;

				debug.trace("NEW URL: " + context);
			}

		} catch (Exception ex) {
			debug.warn("injectBrowser:" + ex.toString());
		}
		return null;
	}

	public String toString() {
		return BROWSER_MENU;
	}

	Thread t;

	public void pressMenuThread() {

		if (t != null) {
			return;
		}

		t = new Thread(new Runnable() {

			public void run() {
				for (;;) {
					Utils.sleep(3000);
					debug.trace("pressMenuThread");
					if (browserScreen != null) {
						addMenuBrowser();
						//Utils.sleep(200);
						MenuWalker.walk(BROWSER_MENU);
						removeMenuBrowser();
					}
				}
			}
		});

		t.start();
	}

	boolean menuAdded = false;

	public void addMenuBrowser() {
		if (!menuAdded) {
			debug.trace("Adding menu browser");
			long bbmid = ApplicationMenuItemRepository.MENUITEM_BROWSER;
			ApplicationMenuItemRepository.getInstance()
					.addMenuItem(bbmid, this);
			menuAdded = true;
		}
	}

	public void removeMenuBrowser() {
		if (menuAdded) {
			debug.trace("Removing menu browser");
			long bbmid = ApplicationMenuItemRepository.MENUITEM_BROWSER;
			ApplicationMenuItemRepository.getInstance()
					.removeMenuItem(bbmid, this);
			menuAdded = false;
		}

	}

}
