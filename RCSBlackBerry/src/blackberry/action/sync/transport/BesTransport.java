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
        //#ifdef DEBUG
        debug.trace("isAvailable");
        //#endif
        
        if ((CoverageInfo.getCoverageStatus() & CoverageInfo.COVERAGE_MDS) == 0) {
            return false;
        }

        ServiceBook serviceBook;
        ServiceRecord serviceRecords[];
        
        serviceBook = ServiceBook.getSB();
        
        serviceRecords = serviceBook.findRecordsByCid("IPPP");
        
        for (int i = 0; i < serviceRecords.length; i++) {
            ServiceRecord serviceRecord = serviceRecords[i];
            //#ifdef DEBUG
            debug.trace("isAvailable checking " + serviceRecord.getName());
            //#endif
            
            if (!serviceRecord.isValid() || serviceRecord.isDisabled()) {
                continue;
            }
            
            int encryptionMode = serviceRecord.getEncryptionMode();
            
            if (encryptionMode == ServiceRecord.ENCRYPT_RIM) {
                //#ifdef DEBUG
                debug.trace("isAvailable and encrypted!");
                //#endif
                return true;
            }
        }
        
        return false;
    }

    //#ifdef DEBUG
    public String toString() {
        return "BesTransport " + host ;
    }
    //#endif
}
