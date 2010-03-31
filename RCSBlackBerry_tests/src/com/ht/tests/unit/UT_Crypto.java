package com.ht.tests.unit;

import net.rim.device.api.util.Arrays;

import com.ht.rcs.blackberry.crypto.Encryption;
import com.ht.rcs.blackberry.crypto.Rijndael;
import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;

public class UT_Crypto extends TestUnit {

	public UT_Crypto(String name, Tests tests) {
		super(name, tests);
	}

	/**
	 * Verifica che la classe Rijndael sia conforme alle specifiche dichiarate,
	 * testando una encryption e una decryption con dei valori noti
	 * 
	 * @return
	 * @throws AssertException
	 */
	boolean RijndaelTest() throws AssertException {
		debug.info("-- RijndaelTest --");
		Rijndael crypto = new Rijndael();

		// i valori seguenti sono stati presi dal paper che descriveva il
		// rijandael per aes
		byte[] key = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
				0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
		byte[] plain = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66,
				0x77, (byte) 0x88, (byte) 0x99, (byte) 0xaa, (byte) 0xbb,
				(byte) 0xcc, (byte) 0xdd, (byte) 0xee, (byte) 0xff };
		byte[] cyphered = new byte[] { 0x69, (byte) 0xc4, (byte) 0xe0,
				(byte) 0xd8, 0x6a, 0x7b, 0x04, 0x30, (byte) 0xd8, (byte) 0xcd,
				(byte) 0xb7, (byte) 0x80, 0x70, (byte) 0xb4, (byte) 0xc5, 0x5a };

		// generazione delle chiave
		crypto.makeKey(key, 128);

		// cifratura
		byte[] buffer = new byte[16];
		crypto.encrypt(plain, buffer);

		// verifico che la cifratura sia conforme a quanto atteso
		AssertThat(Arrays.equals(buffer, cyphered), "Rijndael encrypt");

		// decifro
		crypto.decrypt(cyphered, buffer);

		// verifico che la decifratura sia conforme a quanto atteso
		AssertThat(Arrays.equals(buffer, plain), "Rijndael decrypt");

		// se arrivo qui e- perche- le assert non sono fallite, quindi
		// restituisco true
		return true;

	}

	boolean CBCTest() throws AssertException {
		debug.info("-- CBCTest --");

		Encryption enc = new Encryption();
		byte[] key = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
				0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
		byte[] plain = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66,
				0x77, (byte) 0x88, (byte) 0x99, (byte) 0xaa, (byte) 0xbb,
				(byte) 0xcc, (byte) 0xdd, (byte) 0xee, (byte) 0xff };
		byte[] cyphered = new byte[] { 0x69, (byte) 0xc4, (byte) 0xe0,
				(byte) 0xd8, 0x6a, 0x7b, 0x04, 0x30, (byte) 0xd8, (byte) 0xcd,
				(byte) 0xb7, (byte) 0x80, 0x70, (byte) 0xb4, (byte) 0xc5, 0x5a };

		enc.makeKey(key);

		byte[] buffer = enc.EncryptData(plain);
		AssertThat(Arrays.equals(buffer, cyphered), "Encryption encrypt");

		buffer = enc.DecryptData(cyphered);
		AssertThat(Arrays.equals(buffer, plain), "Encryption decrypt");

		plain = new byte[1024];
		buffer = enc.EncryptData(plain);
		AssertThat(!Arrays.equals(buffer, plain), "enc error");
		buffer = enc.DecryptData(buffer);
		AssertThat(Arrays.equals(buffer, plain), "self error");

		return true;
	}

	boolean EncryptTest() throws AssertException {
		debug.info("-- EncryptTest --");

		Encryption enc = new Encryption();
		byte[] key = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
				0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };

		enc.makeKey(key);

		// 1
		debug.info("1");
		byte[] plain = new byte[1];
		Arrays.fill(plain, (byte) 0x0f);
		byte[] buffer = enc.EncryptData(plain);
		AssertThat(!Arrays.equals(buffer, plain), "enc error");
		AssertThat(buffer.length == 16, "len error 1");

		buffer = enc.DecryptData(buffer, 1, 0);
		AssertThat(buffer.length == 1, "len error 2");
		AssertThat(Arrays.equals(buffer, 0, plain, 0, plain.length),
				"self error");

		// 1
		debug.info("12");
		plain = new byte[12];
		Arrays.fill(plain, (byte) 0x0f);
		buffer = enc.EncryptData(plain);
		AssertThat(!Arrays.equals(buffer, plain), "enc error");
		AssertThat(buffer.length == 16, "len error 1");

		buffer = enc.DecryptData(buffer, plain.length, 0);
		AssertThat(buffer.length == plain.length, "len error 2");
		AssertThat(Arrays.equals(buffer, 0, plain, 0, plain.length),
				"self error");

		// 1
		debug.info("16");
		plain = new byte[16];
		Arrays.fill(plain, (byte) 0x0f);
		buffer = enc.EncryptData(plain);
		AssertThat(!Arrays.equals(buffer, plain), "enc error");
		AssertThat(buffer.length == 16, "len error 1");

		buffer = enc.DecryptData(buffer, plain.length, 0);
		AssertThat(buffer.length == 16, "len error 2");
		AssertThat(Arrays.equals(buffer, 0, plain, 0, plain.length),
				"self error");

		// 1024
		debug.info("1024");
		plain = new byte[1024];
		Arrays.fill(plain, (byte) 0x0f);
		buffer = enc.EncryptData(plain);
		AssertThat(!Arrays.equals(buffer, plain), "enc error");
		AssertThat(buffer.length == plain.length, "len error 1");

		buffer = enc.DecryptData(buffer, plain.length, 0);
		AssertThat(buffer.length == plain.length, "len error 2");
		AssertThat(Arrays.equals(buffer, 0, plain, 0, plain.length),
				"self error");

		return true;
	}

	void ScrambleTest() throws AssertException {

		Encryption crypto = new Encryption();

		String ret = Encryption.EncryptName("KiodoGay", 0xb0);
		String expected = "pKdTdlYz";
		AssertEquals(ret, expected, "Scramble 1");

		ret = Encryption.EncryptName("BrunelloBrunilde", 0xb0);
		expected = "RbF5OQQdRbF5KQTO";
		AssertEquals(ret, expected, "Scramble 2");

		ret = Encryption.EncryptName("Zeno", 0xb0);
		expected = "kO5d";
		AssertEquals(ret, expected, "Scramble 3");

		ret = Encryption.EncryptName("Xeno", 0xb0);
		expected = "8O5d";
		AssertEquals(ret, expected, "Scramble 4");

		ret = Encryption.EncryptName("10401349w298238402834923.mob", 0xb0);
		expected = "mVHVmoHh9ZhnZonHVZnoHhZo.udD";
		AssertEquals(ret, expected, "Scramble 5");

		ret = Encryption.EncryptName("*.mob", 0xb0);
		expected = "*.udD";
		AssertEquals(ret, expected, "Scramble 6");
	}

	void MultipleTest() {
		for (int i = 0; i < 1024; i++) {
			int n = Encryption.GetNextMultiple(i);
		}
	}

	public boolean run() throws AssertException {
		MultipleTest();
		RijndaelTest();
		CBCTest();
		EncryptTest();
		ScrambleTest();
		return true;
	}

}
