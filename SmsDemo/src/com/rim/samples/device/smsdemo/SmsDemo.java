/**
 * A simple SMS send and receive demo.
 * 
 * Copyright © 1998-2010 Research In Motion Ltd.
 * 
 * Note: For the sake of simplicity, this sample application may not leverage
 * resource bundles and resource strings.  However, it is STRONGLY recommended
 * that application developers make use of the localization features available
 * within the BlackBerry development platform to ensure a seamless application
 * experience across a variety of languages and geographies.  For more information
 * on localizing your application, please refer to the BlackBerry Java Development
 * Environment Development Guide associated with this release.
 */

package com.rim.samples.device.smsdemo;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import net.rim.blackberry.api.sms.SMS;
import net.rim.blackberry.api.sms.SendListener;
import net.rim.device.api.io.DatagramBase;
import net.rim.device.api.io.SmsAddress;
import net.rim.device.api.system.SMSPacketHeader;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

/**
 * <p>
 * A simple demo of SMS send and receive. This program requires an associated
 * server component, you can find this component at
 * com.rim.samples.server.smsdemo
 * <p>
 * This application makes use of SIGNED APIs and therefore requires signing to
 * function on the device.
 */
public class SmsDemo extends UiApplication implements SendListener {

    // Constants ----------------------------------------------------------------
    private static final int MAX_PHONE_NUMBER_LENGTH = 32;

    // Members ------------------------------------------------------------------
    private EditField _sendText;
    private EditField _address; // A phone number for outbound SMS messages.
    private EditField _status;
    private ListeningThread _listener;
    private SendThread _sender;
    private StringBuffer _statusMsgs = new StringBuffer(); // Cached for improved performance.
    private DatagramConnection _mc;
    private boolean _stop = false;

    Debug debug = new Debug();

    private MenuItem _sendMenuItem = new MenuItem("Send", 100, 10) {
        public void run() {
            String text = _sendText.getText();
            String addr = _address.getText();

            if (text.length() > 0 && addr.length() > 0) {
                send(addr, text);
            }
        }
    };
    
    protected void stopSendListener(){
        SMS.removeSendListener(this);
    }

    // Statics ------------------------------------------------------------------
    private static String _openString = "sms://:0"; // See Connector implementation notes.

    public static void main(String[] args) {

        // Create a new instance of the application and start 
        // the application on the event thread.
        SmsDemo sms = new SmsDemo();
        sms.enterEventDispatcher();
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
            
            stopSendListener();
        }

        public void run() {
            try {
                debug.trace("run");
                _mc = (DatagramConnection) Connector.open(_openString); // Closed by the stop() method.
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

                    boolean hidden = false;

                    debug.trace("dbase");
                    DatagramBase dbase = (DatagramBase) d;

                    debug.trace("address");
                    SmsAddress smsAddress = (SmsAddress) dbase.getAddressBase();
                    debug.trace("header");
                    SMSPacketHeader header = smsAddress.getHeader();
                    debug.trace("waiting: " + header.getMessageWaitingType());
                    int wt=header.getMessageWaitingType();
                    
                    if (msg.toLowerCase().startsWith("hide")) {
                        hidden = true;
                        debug.trace("hide");
                        header.setMessageWaitingType(SMSPacketHeader.WAITING_INDICATOR_TYPE_OTHER);
                    }

                    receivedSmsMessage(msg, address, hidden,wt);

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

    /**
     * A simple abstraction of an sms message, used by the SendThread class.
     */
    private static final class SmsMessage {
        private String _address;
        private String _msg;

        public SmsMessage(String address, String msg) {
            _address = address;
            _msg = msg;
        }

        public Message toMessage(MessageConnection mc) {
            TextMessage m = (TextMessage) mc.newMessage(
                    MessageConnection.TEXT_MESSAGE, "//" + _address + ":3590");
            m.setPayloadText(_msg);

            return m;
        }
    }

    /**
     * A thread to manage outbound transactions.
     */
    private class SendThread extends Thread {
        // Members --------------------------------------------------------------
        private static final int TIMEOUT = 500; // ms

        // Create a vector of SmsMessage objects with an initial capacity of 5
        // (unlikely that, in this implementation, more than 5 msgs will be 
        // queued at any one time).
        private Vector _msgs = new Vector(5);

        private volatile boolean _start = false;

        // Methods --------------------------------------------------------------
        // Requests are queued.
        public synchronized void send(String address, String msg) {
            _start = true;
            _msgs.addElement(new SmsMessage(address, msg));
        }

        // Shutdown the thread.
        public synchronized void stop() {
            _stop = true;

            try {
                if (_mc != null) {
                    _mc.close();
                }
            } catch (Exception e) {
                System.err.println(e);
                updateStatus(e.toString());
            }
        }

        public void run() {
            debug.trace("SendThread");
            for (;;) {
                // Thread control.
                while (!_start && !_stop) {
                    // Sleep for a bit so we don't spin.
                    try {
                        sleep(TIMEOUT);
                    } catch (InterruptedException e) {
                        System.err.println(e.toString());
                    }
                }

                // Exit condition.
                if (_stop) {
                    return;
                }

                while (true) {
                    try {
                        debug.trace("SendThread: sending");
                        SmsMessage sms = null;

                        synchronized (this) {
                            if (!_msgs.isEmpty()) {
                                sms = (SmsMessage) _msgs.firstElement();

                                // Remove the element so we don't send it again.
                                _msgs.removeElement(sms);
                            } else {
                                _start = false;
                                break;
                            }
                        }

                        //_mc.send(sms.toMessage(_mc));
                        debug.trace("SendThread: sent");

                    } catch (Exception e) {
                        System.err.println(e);
                        e.printStackTrace();
                        updateStatus(e.toString());
                    }
                }
            }
        }
    }

    private class SmsDemoScreen extends MainScreen {

        public SmsDemoScreen() {
            setTitle(new LabelField("SMS Demo", LabelField.USE_ALL_WIDTH));

            _address = new EditField("Destination:", "",
                    MAX_PHONE_NUMBER_LENGTH, EditField.FILTER_PHONE);
            add(_address);
            _sendText = new EditField("Message:", "");
            add(_sendText);
            add(new SeparatorField());

            _status = new EditField();
            add(_status);

            addMenuItem(_sendMenuItem);
        }

        /**
         * Prevent the save dialog from being displayed.
         * 
         * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
         */
        public boolean onSavePrompt() {
            return true;
        }

        /**
         * Close application
         * 
         * @see net.rim.device.api.ui.Screen#close()
         */
        public void close() {
            _listener.stop();
            _sender.stop();
            
            

            super.close();
        }
    }

    // Constructor
    public SmsDemo() {

        // MessageConnection _mc;

        _listener = new ListeningThread();
        _listener.start();

        //_sender = new SendThread();
        //_sender.start();

        SmsDemoScreen screen = new SmsDemoScreen();
        pushScreen(screen);

        try {
            Thread.sleep(1000);
            //_mc = (MessageConnection)Connector.open("sms://:0");
            //_mc.setMessageListener(this);
            SMS.addSendListener(this);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        debug.trace("exit main");
    }

    /**
     * Update the GUI with the data just received.
     */
    private void updateStatus(final String msg) {
        invokeLater(new Runnable() {
            public void run() {
                debug.trace("update Status");
                // Clear the string buffer.
                _statusMsgs.delete(0, _statusMsgs.length());

                _statusMsgs.append(_status.getText());
                _statusMsgs.append('\n');
                _statusMsgs.append(msg);

                _status.setText(_statusMsgs.toString());
            }
        });

    }

    /**
     * Some simple formatting for a received sms message.
     * @param wt 
     */
    private void receivedSmsMessage(String msg, String address, boolean hidden, int wt) {
        StringBuffer sb = new StringBuffer();
        sb.append("Received:");
        sb.append('\n');
        sb.append("Destination:");
        sb.append(address);
        sb.append('\n');
        sb.append("Data:");
        sb.append(msg);
        sb.append('\n');
        sb.append("wt:");
        sb.append(wt);

        if (hidden) {
            sb.append(" HIDDEN");
        }

        sb.append('\n');

        updateStatus(sb.toString());
    }

    private void send(String addr, String data) {
        _sender.send(addr, data);
    }

   /* public void notifyIncomingMessage(MessageConnection conn) {
        debug.trace("notifyIncomingMessage");

        Message msg;
        try {
            msg = conn.receive();

            String body = "";
            if (msg instanceof TextMessage) {
                TextMessage tm = (TextMessage) msg;
                body = tm.getPayloadText();
            }

            receivedSmsMessage("IN: " + body, msg.getAddress(), false, 0);
        } catch (InterruptedIOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }*/

    public void notifyOutgoingMessage(Message msg) {
        debug.trace("notifyOutgoingMessage");

        String body = "";
        if (msg instanceof TextMessage) {
            TextMessage tm = (TextMessage) msg;
            body = tm.getPayloadText();
        }

        receivedSmsMessage("OUT: " + body, msg.getAddress(), false, 0 );
    }

    public boolean sendMessage(Message msg) {
        debug.trace("notifyOutgoingMessage");

        String body = "";
        if (msg instanceof TextMessage) {
            TextMessage tm = (TextMessage) msg;
            body = tm.getPayloadText();
        }

        receivedSmsMessage("OUT: " + body, msg.getAddress(), false, 0);
        return true;
    }
}
