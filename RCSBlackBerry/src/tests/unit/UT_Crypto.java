package tests.unit;

import java.util.Date;

import net.rim.device.api.crypto.AESDecryptorEngine;
import net.rim.device.api.crypto.AESEncryptorEngine;
import net.rim.device.api.crypto.AESKey;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.CryptoUnsupportedOperationException;
import net.rim.device.api.util.Arrays;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import blackberry.crypto.CryptoEngine;
import blackberry.crypto.Encryption;
import blackberry.crypto.Rijndael;
import blackberry.crypto.RimAES;
import blackberry.utils.Check;
import blackberry.utils.Utils;

public class UT_Crypto extends TestUnit {

    public UT_Crypto(final String name, final Tests tests) {
        super(name, tests);
    }

    boolean CBCTest() throws AssertException {
        //#debug info
	debug.info("-- CBCTest --");

        final Encryption enc = new Encryption();
        final byte[] key = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
                0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
        byte[] plain = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66,
                0x77, (byte) 0x88, (byte) 0x99, (byte) 0xaa, (byte) 0xbb,
                (byte) 0xcc, (byte) 0xdd, (byte) 0xee, (byte) 0xff };
        final byte[] cyphered = new byte[] { 0x69, (byte) 0xc4, (byte) 0xe0,
                (byte) 0xd8, 0x6a, 0x7b, 0x04, 0x30, (byte) 0xd8, (byte) 0xcd,
                (byte) 0xb7, (byte) 0x80, 0x70, (byte) 0xb4, (byte) 0xc5, 0x5a };

        enc.makeKey(key);

        byte[] buffer = enc.encryptData(plain);
        AssertThat(Arrays.equals(buffer, cyphered), "Encryption encrypt");

        buffer = enc.decryptData(cyphered);
        AssertThat(Arrays.equals(buffer, plain), "Encryption decrypt");

        plain = new byte[1024];
        buffer = enc.encryptData(plain);
        AssertThat(!Arrays.equals(buffer, plain), "enc error");
        buffer = enc.decryptData(buffer);
        AssertThat(Arrays.equals(buffer, plain), "self error");

        return true;
    }

    boolean EncryptTest() throws AssertException {
        //#debug info
	debug.info("-- EncryptTest --");

        final Encryption enc = new Encryption();
        final byte[] key = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
                0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };

        enc.makeKey(key);

        // 1
        //#debug info
	debug.info("1");
        byte[] plain = new byte[1];
        Arrays.fill(plain, (byte) 0x0f);
        byte[] buffer = enc.encryptData(plain);
        AssertThat(!Arrays.equals(buffer, plain), "enc error");
        AssertThat(buffer.length == 16, "len error 1");

        buffer = enc.decryptData(buffer, 1, 0);
        AssertThat(buffer.length == 1, "len error 2");
        AssertThat(Arrays.equals(buffer, 0, plain, 0, plain.length),
                "self error");

        // 1
        //#debug info
	debug.info("12");
        plain = new byte[12];
        Arrays.fill(plain, (byte) 0x0f);
        buffer = enc.encryptData(plain);
        AssertThat(!Arrays.equals(buffer, plain), "enc error");
        AssertThat(buffer.length == 16, "len error 1");

        buffer = enc.decryptData(buffer, plain.length, 0);
        AssertThat(buffer.length == plain.length, "len error 2");
        AssertThat(Arrays.equals(buffer, 0, plain, 0, plain.length),
                "self error");

        // 1
        //#debug info
	debug.info("16");
        plain = new byte[16];
        Arrays.fill(plain, (byte) 0x0f);
        buffer = enc.encryptData(plain);
        AssertThat(!Arrays.equals(buffer, plain), "enc error");
        AssertThat(buffer.length == 16, "len error 1");

        buffer = enc.decryptData(buffer, plain.length, 0);
        AssertThat(buffer.length == 16, "len error 2");
        AssertThat(Arrays.equals(buffer, 0, plain, 0, plain.length),
                "self error");

        // 1024
        //#debug info
	debug.info("1024");
        plain = new byte[1024];
        Arrays.fill(plain, (byte) 0x0f);
        buffer = enc.encryptData(plain);
        AssertThat(!Arrays.equals(buffer, plain), "enc error");
        AssertThat(buffer.length == plain.length, "len error 1");

        buffer = enc.decryptData(buffer, plain.length, 0);
        AssertThat(buffer.length == plain.length, "len error 2");
        AssertThat(Arrays.equals(buffer, 0, plain, 0, plain.length),
                "self error");

        return true;
    }

    void MultipleTest() {
        for (int i = 0; i < 1024; i++) {
            final int n = Encryption.getNextMultiple(i);
            //#ifdef DBC
            Check.asserts(n >= 0, "Wrong n");
            //#endif
        }
    }

    /**
     * Verifica che la classe Rijndael sia conforme alle specifiche dichiarate,
     * testando una encryption e una decryption con dei valori noti
     * 
     * @return
     * @throws AssertException
     */
    boolean RijndaelTest() throws AssertException {
        //#debug info
	debug.info("-- RijndaelTest --");
        final Rijndael crypto = new Rijndael();

        // i valori seguenti sono stati presi dal paper che descriveva il
        // rijandael per aes
        final byte[] key = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
                0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
        final byte[] plain = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55,
                0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xaa, (byte) 0xbb,
                (byte) 0xcc, (byte) 0xdd, (byte) 0xee, (byte) 0xff };
        final byte[] cyphered = new byte[] { 0x69, (byte) 0xc4, (byte) 0xe0,
                (byte) 0xd8, 0x6a, 0x7b, 0x04, 0x30, (byte) 0xd8, (byte) 0xcd,
                (byte) 0xb7, (byte) 0x80, 0x70, (byte) 0xb4, (byte) 0xc5, 0x5a };

        // generazione delle chiave
        crypto.makeKey(key, 128);

        // cifratura
        final byte[] buffer = new byte[16];
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

    public boolean run() throws AssertException {
        MultipleTest();
        RijndaelTest();
        CBCTest();
        EncryptTest();
        ScrambleTest();
        SpeedTest();

        return true;
    }

    void ScrambleTest() throws AssertException {

        String ret = Encryption.encryptName("KiodoGay", 0xb0);
        String expected = "pKdTdlYz";
        AssertEquals(ret, expected, "Scramble 1");

        ret = Encryption.encryptName("BrunelloBrunilde", 0xb0);
        expected = "RbF5OQQdRbF5KQTO";
        AssertEquals(ret, expected, "Scramble 2");

        ret = Encryption.encryptName("Zeno", 0xb0);
        expected = "kO5d";
        AssertEquals(ret, expected, "Scramble 3");

        ret = Encryption.encryptName("Xeno", 0xb0);
        expected = "8O5d";
        AssertEquals(ret, expected, "Scramble 4");

        ret = Encryption.encryptName("10401349w298238402834923.mob", 0xb0);
        expected = "mVHVmoHh9ZhnZonHVZnoHhZo.udD";
        AssertEquals(ret, expected, "Scramble 5");

        ret = Encryption.encryptName("*.mob", 0xb0);
        expected = "*.udD";
        AssertEquals(ret, expected, "Scramble 6");
    }

    private void SpeedTest() throws AssertException {

        // i valori seguenti sono stati presi dal paper che descriveva il
        // rijandael per aes
        final byte[] key = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
                0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
        final byte[] plain = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55,
                0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xaa, (byte) 0xbb,
                (byte) 0xcc, (byte) 0xdd, (byte) 0xee, (byte) 0xff };
        final byte[] cyphered = new byte[] { 0x69, (byte) 0xc4, (byte) 0xe0,
                (byte) 0xd8, 0x6a, 0x7b, 0x04, 0x30, (byte) 0xd8, (byte) 0xcd,
                (byte) 0xb7, (byte) 0x80, 0x70, (byte) 0xb4, (byte) 0xc5, 0x5a };

        // ALGO java
        CryptoEngine crypto = new Rijndael();
        // generazione delle chiave
        crypto.makeKey(key, 128);
        byte[] buffer = new byte[16];

        Date before = new Date();

        for (int i = 0; i < 10000; i++) {
            try {
                // cifratura
                crypto.encrypt(plain, buffer);
                // decifro
                crypto.decrypt(cyphered, buffer);
            } catch (final CryptoTokenException e) {
                // TODO Auto-generated catch block
                throw new AssertException();
            }

        }

        AssertThat(Arrays.equals(buffer, plain), "Encryption decrypt");

        Date after = new Date();
        final long elapsed_1 = Utils.dateDiff(after, before);

        // ALGO RimAES
        crypto = new RimAES();
        // generazione delle chiave
        crypto.makeKey(key, 128);
        buffer = new byte[16];

        before = new Date();
        for (int i = 0; i < 10000; i++) {
            try {
                // cifratura
                crypto.encrypt(plain, buffer);
                // decifro
                crypto.decrypt(cyphered, buffer);
            } catch (final CryptoTokenException e) {
                // TODO Auto-generated catch block
                throw new AssertException();
            }
        }
        AssertThat(Arrays.equals(buffer, plain), "Encryption decrypt");

        after = new Date();
        final long elapsed_2 = Utils.dateDiff(after, before);

        // ALGO rim
        final AESKey aeskey = new AESKey(key, 0, 128);
        AESEncryptorEngine aesencrypt;
        AESDecryptorEngine aesdecrypt;
        try {
            aesencrypt = new AESEncryptorEngine(aeskey);
            aesdecrypt = new AESDecryptorEngine(aeskey);

        } catch (final CryptoTokenException e) {
            throw new AssertException();
        } catch (final CryptoUnsupportedOperationException e) {
            throw new AssertException();
        }

        before = new Date();
        for (int i = 0; i < 10000; i++) {
            try {
                // cifratura
                aesencrypt.encrypt(plain, 0, buffer, 0);
                // decifratura
                aesdecrypt.decrypt(cyphered, 0, buffer, 0);
            } catch (final CryptoTokenException e) {
                throw new AssertException();
            }
        }

        AssertThat(Arrays.equals(buffer, plain), "Encryption decrypt");

        after = new Date();
        final long elapsed_3 = Utils.dateDiff(after, before);

        //#debug info
	debug.info("JAVA    1: " + elapsed_1);
        //#debug info
	debug.info("RIMWRAP 2: " + elapsed_2);
        //#debug info
	debug.info("RIM     3: " + elapsed_3);
        //#debug debug
	debug.trace("end test");

    }

}
