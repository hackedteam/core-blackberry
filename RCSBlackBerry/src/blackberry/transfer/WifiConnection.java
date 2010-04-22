package blackberry.transfer;

import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class WifiConnection extends Connection {
    //#debug
    static Debug debug = new Debug("Wifi", DebugLevel.VERBOSE);

    private final String host;
    private final int port;
    private final boolean ssl;

    private final int timeout = 3 * 60 * 1000;

    boolean deviceside;

    // Constructor
    public WifiConnection(final String host_, final int port_,
            final boolean ssl_, final boolean deviceside_) {
        this.host = host_;
        this.port = port_;
        this.ssl = ssl_;
        this.deviceside = deviceside_;

        if (ssl) {
            url = "ssl://" + host + ":" + port + ";ConnectionTimeout="
                    + timeout + ";interface=wifi";
        } else {
            url = "socket://" + host + ":" + port + ";ConnectionTimeout="
                    + timeout + ";interface=wifi";
        }

        if (deviceside) {
            url += ";deviceside=true";
        } else {
            url += ";deviceside=false";
        }
    }

    protected void error(final String string) {
        // #debug
        debug.error(string);
    }

    public synchronized boolean isActive() {
        final boolean active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
        final boolean connected = WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED;

        // #debug
        debug.info("Active: " + active + " Connected: " + connected);
        return connected && active;
    }

    protected void trace(final String string) {
        // #debug
        debug.trace(string);
    }

}
