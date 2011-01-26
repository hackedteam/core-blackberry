//#preprocess
package blackberry.action.sync.transport;

import net.rim.device.api.system.RadioInfo;
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
        final boolean active = (RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0;
        //TODO: se c'e' wifiForced
        return active;
    }

    protected String getSuffix() {       
        return ";deviceside=true;interface=wifi";
    }

    public String toString() {
        return "WifiTransport " + host;
    }
}
