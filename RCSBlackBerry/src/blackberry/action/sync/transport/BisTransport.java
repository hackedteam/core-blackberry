package blackberry.action.sync.transport;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import net.rim.device.api.system.RadioInfo;

public class BisTransport extends HttpTransport {
    
    //#ifdef DEBUG
    private static Debug debug = new Debug("BisTransport", DebugLevel.VERBOSE);
    //#endif
    
    public BisTransport(String host) {
        super(host);
    }

    public boolean isAvailable() {
        boolean gprs = (RadioInfo.getNetworkService() & RadioInfo.NETWORK_SERVICE_DATA) > 0;

        return gprs;
    }

    protected String getSuffix() {
        return ";deviceside=false;ConnectionType=mds-public";
    }
    
    public String toString() {
        return "BisTransport " + host ;
    }
}
