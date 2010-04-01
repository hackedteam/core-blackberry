package com.ht.tests.unit;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import com.ht.rcs.blackberry.Conf;
import com.ht.rcs.blackberry.Status;
import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;

import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.DataBuffer;

public class UT_Conf extends TestUnit {

	InputStream clearIO_test_1;
	InputStream clearIO_test_2;

	byte[] confBuffer_test_1 = new byte[0];
	byte[] confBuffer_test_2 = new byte[0];

	public UT_Conf(String name, Tests tests) {
		super(name, tests);

		clearIO_test_1 = UT_Conf.class
				.getResourceAsStream("../Conf/clearconf_test_1.bin");
		clearIO_test_2 = UT_Conf.class
				.getResourceAsStream("../Conf/clearconf_test_2.bin");
		// encIO=
		// UT_Conf.class.getResourceAsStream("../Conf/encryptedconf.bin");
		// encIO_Big=
		// UT_Conf.class.getResourceAsStream("../Conf/encryptedconf2.bin");
	}

	boolean ClearLoad() throws AssertException {
		debug.info("-- ClearLoad --");

		byte[] buffer = new byte[1024 * 10];
		try {
			AssertThat(clearIO_test_1 != null, "clearIO");
			AssertThat(clearIO_test_2 != null, "clearIO_Big");

			int len = clearIO_test_1.read(buffer);
			confBuffer_test_1 = Arrays.copy(buffer, 0, len);
			AssertThat(len > 0, "Len <=0");

			len = clearIO_test_2.read(buffer);
			confBuffer_test_2 = Arrays.copy(buffer, 0, len);
			AssertThat(len > 0, "Len <=0");

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	boolean CrcTest() throws AssertException {
		debug.info("-- ClearLoad --");

		DataBuffer databuffer = new DataBuffer(confBuffer_test_1, 0,
				confBuffer_test_1.length, false);
		int len;
		boolean crcOK = false;

		try {
			len = databuffer.readInt();
			int payloadSize = len - 4;

			byte[] payload = new byte[payloadSize];
			databuffer.setPosition(0);
			databuffer.readFully(payload);

			databuffer.setPosition(payloadSize);
			int crcExpected = databuffer.readInt();

			crcOK = Conf.crcVerify(payload, crcExpected);
		} catch (EOFException e) {
			debug.error("EOFException");
			throw new AssertException();
		}

		return crcOK;
	}

	boolean ParseConfTest() throws AssertException {
		debug.info("-- ParseConfTest --");

		Status statusObj = Status.getInstance();
		statusObj.clear();

		Conf conf = new Conf();
		boolean ret = conf.parseConf(confBuffer_test_1, 0);
		AssertThat(ret == true, "ParseConf failed");

		Vector agents = statusObj.getAgentsList();
		Vector events = statusObj.getEventsList();
		Vector actions = statusObj.getActionsList();
		Vector parameters = statusObj.getParametersList();

		AssertEquals(agents.size(), 14, "Wrong Agent number");
		AssertEquals(events.size(), 3, "Wrong Events number");
		AssertEquals(actions.size(), 3, "Wrong Actions number");
		AssertEquals(parameters.size(), 4, "Wrong Parameters number");

		return true;
	}

	boolean ParseConfBigTest() throws AssertException {
		debug.info("-- ParseConfBigTest --");

		Status statusObj = Status.getInstance();
		statusObj.clear();

		Conf conf = new Conf();
		boolean ret = conf.parseConf(confBuffer_test_2, 0);
		AssertThat(ret == true, "ParseConf failed");

		Vector agents = statusObj.getAgentsList();
		Vector events = statusObj.getEventsList();
		Vector actions = statusObj.getActionsList();
		Vector parameters = statusObj.getParametersList();

		AssertEquals(agents.size(), 14, "Wrong Agent number: " + agents.size());
		AssertEquals(events.size(), 18, "Wrong Events number: " + events.size());
		AssertEquals(actions.size(), 9, "Wrong Actions number: "
				+ actions.size());
		AssertEquals(parameters.size(), 4, "Wrong Parameters number: "
				+ parameters.size());

		return true;
	}

	boolean CryptoLoad_1() throws AssertException {
		debug.info("-- CryptoLoad_1 --");

		Status statusObj = Status.getInstance();
		statusObj.clear();

		InputStream clearIO = UT_Conf.class
				.getResourceAsStream("../Conf/Conf1/clearconf1.bin");
		InputStream encIO = UT_Conf.class
				.getResourceAsStream("../Conf/Conf1/encryptedconf1.bin");

		byte[] clearBuffer = new byte[10 * 1024];
		// byte[] encBuffer=new byte[10 * 1024];
		try {
			clearIO.read(clearBuffer);
			// clearIO.read(encBuffer);
		} catch (IOException e) {
			throw new AssertException();
		}

		// check plain
		Conf conf = new Conf();
		boolean ret = conf.parseConf(clearBuffer, 0);
		AssertThat(ret == true, "ParseConf failed");

		clearBuffer = null;

		Vector agents = statusObj.getAgentsList();
		Vector events = statusObj.getEventsList();
		Vector actions = statusObj.getActionsList();
		Vector parameters = statusObj.getParametersList();

		AssertEquals(agents.size(), 14, "Wrong Agent number: " + agents.size());
		AssertEquals(events.size(), 4, "Wrong Events number: " + events.size());
		AssertEquals(actions.size(), 2, "Wrong Actions number: "
				+ actions.size());
		AssertEquals(parameters.size(), 4, "Wrong Parameters number: "
				+ parameters.size());

		// check crypto
		byte[] ConfKey = new byte[] { 0x53, (byte) 0x81, 0x2f, (byte) 0xda,
				(byte) 0xec, (byte) 0xfb, (byte) 0xa4, (byte) 0xae, 0x79, 0x7e,
				(byte) 0x94, (byte) 0xa7, 0x42, 0x2b, (byte) 0x80, (byte) 0xa7 };

		statusObj.clear();
		conf = new Conf();

		ret = conf.loadCyphered(encIO, ConfKey);
		AssertThat(ret == true, "Load failed");

		agents = statusObj.getAgentsList();
		events = statusObj.getEventsList();
		actions = statusObj.getActionsList();
		parameters = statusObj.getParametersList();

		AssertEquals(agents.size(), 14, "Wrong Agent number: " + agents.size());
		AssertEquals(events.size(), 4, "Wrong Events number: " + events.size());
		AssertEquals(actions.size(), 2, "Wrong Actions number: "
				+ actions.size());
		AssertEquals(parameters.size(), 4, "Wrong Parameters number: "
				+ parameters.size());

		return true;
	}

	boolean CryptoLoad_2() throws AssertException {
		debug.info("-- CryptoLoad_2 --");

		Status statusObj = Status.getInstance();
		statusObj.clear();

		InputStream clearIO = UT_Conf.class
				.getResourceAsStream("../Conf/Conf2/clearconf2.bin");
		InputStream encIO = UT_Conf.class
				.getResourceAsStream("../Conf/Conf2/encryptedconf2.bin");

		byte[] clearBuffer = new byte[10 * 1024];
		// byte[] encBuffer=new byte[10 * 1024];
		try {
			clearIO.read(clearBuffer);
			// clearIO.read(encBuffer);
		} catch (IOException e) {
			throw new AssertException();
		}

		// check plain
		Conf conf = new Conf();
		boolean ret = conf.parseConf(clearBuffer, 0);
		AssertThat(ret == true, "ParseConf failed");

		clearBuffer = null;

		Vector agents = statusObj.getAgentsList();
		Vector events = statusObj.getEventsList();
		Vector actions = statusObj.getActionsList();
		Vector parameters = statusObj.getParametersList();

		AssertEquals(agents.size(), 14, "Wrong Agent number: " + agents.size());
		AssertEquals(events.size(), 2, "Wrong Events number: " + events.size());
		AssertEquals(actions.size(), 2, "Wrong Actions number: "
				+ actions.size());
		AssertEquals(parameters.size(), 4, "Wrong Parameters number: "
				+ parameters.size());

		// check crypto
		byte[] ConfKey = new byte[] { 0x29, (byte) 0x92, (byte) 0xf1, 0x5c,
				0x29, 0x42, (byte) 0xb2, 0x73, 0x6d, (byte) 0xf2, (byte) 0xaa,
				(byte) 0x8c, 0x24, (byte) 0xfa, 0x72, (byte) 0xad };

		statusObj.clear();
		conf = new Conf();

		ret = conf.loadCyphered(encIO, ConfKey);
		AssertThat(ret == true, "Load failed");

		agents = statusObj.getAgentsList();
		events = statusObj.getEventsList();
		actions = statusObj.getActionsList();
		parameters = statusObj.getParametersList();

		AssertEquals(agents.size(), 14, "Wrong Agent number: " + agents.size());
		AssertEquals(events.size(), 2, "Wrong Events number: " + events.size());
		AssertEquals(actions.size(), 2, "Wrong Actions number: "
				+ actions.size());
		AssertEquals(parameters.size(), 4, "Wrong Parameters number: "
				+ parameters.size());

		return true;
	}

	boolean CryptoLoad_3() throws AssertException {
		debug.info("-- CryptoLoad_3 --");

		Status statusObj = Status.getInstance();
		statusObj.clear();

		InputStream clearIO = UT_Conf.class
				.getResourceAsStream("../Conf/Conf3/clearconf3.bin");
		InputStream encIO = UT_Conf.class
				.getResourceAsStream("../Conf/Conf3/encryptedconf3.bin");

		byte[] clearBuffer = new byte[10 * 1024];
		try {
			clearIO.read(clearBuffer);
		} catch (IOException e) {
			throw new AssertException();
		}

		// check plain
		Conf conf = new Conf();
		boolean ret = conf.parseConf(clearBuffer, 0);
		AssertThat(ret == true, "ParseConf failed");

		clearBuffer = null;

		Vector agents = statusObj.getAgentsList();
		Vector events = statusObj.getEventsList();
		Vector actions = statusObj.getActionsList();
		Vector parameters = statusObj.getParametersList();

		AssertEquals(agents.size(), 14, "Wrong Agent number: " + agents.size());
		AssertEquals(events.size(), 2, "Wrong Events number: " + events.size());
		AssertEquals(actions.size(), 2, "Wrong Actions number: "
				+ actions.size());
		AssertEquals(parameters.size(), 4, "Wrong Parameters number: "
				+ parameters.size());

		// check crypto

		byte[] ConfKey = new byte[] { (byte) 0x97, 0x56, 0x5a, (byte) 0x9c,
				0x21, (byte) 0xf1, 0x44, (byte) 0xe8, (byte) 0xf5, 0x50,
				(byte) 0xff, 0x2b, (byte) 0xf6, (byte) 0x90, 0x20, 0x3c };

		statusObj.clear();
		conf = new Conf();

		ret = conf.loadCyphered(encIO, ConfKey);
		AssertThat(ret == true, "Load failed");

		agents = statusObj.getAgentsList();
		events = statusObj.getEventsList();
		actions = statusObj.getActionsList();
		parameters = statusObj.getParametersList();

		AssertEquals(agents.size(), 14, "Wrong Agent number: " + agents.size());
		AssertEquals(events.size(), 2, "Wrong Events number: " + events.size());
		AssertEquals(actions.size(), 2, "Wrong Actions number: "
				+ actions.size());
		AssertEquals(parameters.size(), 4, "Wrong Parameters number: "
				+ parameters.size());

		return true;
	}

	public boolean run() throws AssertException {

		ClearLoad();
		CrcTest();
		ParseConfTest();
		ParseConfBigTest();

		CryptoLoad_1();
		CryptoLoad_2();
		CryptoLoad_3();

		return true;
	}

}
