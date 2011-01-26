package blackberry.action.sync.transport;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class BesTransport extends HttpTransport {

    //#ifdef DEBUG
    private static Debug debug = new Debug("BesTransport", DebugLevel.VERBOSE);
    //#endif
    
    public BesTransport(String host) {
        super(host);
    }

    protected String getSuffix() {
        return ";deviceside=false;";
    }

    public boolean isAvailable() {
        return true;
    }

    public String toString() {
        return "BesTransport " + host ;
    }
}
