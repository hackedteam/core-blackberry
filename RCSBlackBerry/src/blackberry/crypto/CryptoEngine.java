//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.crypto
 * File         : CryptoEngine.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.crypto;

import net.rim.device.api.crypto.CryptoTokenException;


/**
 * The Interface CryptoEngine.
 */
public interface CryptoEngine {
    //#ifdef DEBUG
    //#endif
    /**
     * Decrypt.
     * 
     * @param ct
     *            the ct
     * @param pt
     *            the pt
     * @throws CryptoTokenException
     *             the crypto token exception
     */
    void decrypt(byte[] ct, byte[] pt) throws CryptoTokenException;

    /**
     * Encrypt.
     * 
     * @param pt
     *            the pt
     * @param ct
     *            the ct
     * @throws CryptoTokenException
     *             the crypto token exception
     */
    void encrypt(byte[] pt, byte[] ct) throws CryptoTokenException;

    /**
     * Make key.
     * 
     * @param cipherKey
     *            the cipher key
     * @param keyBits
     *            the key bits
     * @return true, if successful
     */
    boolean makeKey(byte[] cipherKey, int keyBits);
}
