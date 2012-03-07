package injectionFW.injectors;

import injectionFW.log.Debug;
import injectionFW.log.DebugLevel;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;

public abstract class Injector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Injector", DebugLevel.VERBOSE);
    //#endif
    private UiApplication injectedApp = null;
    
    public abstract String getAppName();
    public abstract String getCodName();
    
    public abstract String[] getWantedScreen();
    
    public abstract void playOnScreen(Screen screen);

    public void setInjectedApp(UiApplication app) {
        //#ifdef DEBUG
        debug.trace("setInjectedApp: INJECTED");
        //#endif
        this.injectedApp = app;
    }

    public UiApplication getInjectedApp() {
        return injectedApp;
    }
    
    public boolean isInjected(){
        return injectedApp!=null;
    }

}
