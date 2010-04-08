package com.ht.rcs.blackberry.crypto;

import net.rim.device.api.crypto.CryptoTokenException;

public interface CryptoEngine {
    void encrypt(byte[] pt, byte[] ct) throws CryptoTokenException;
    void decrypt(byte[] ct, byte[] pt) throws CryptoTokenException;
    boolean makeKey(byte[] cipherKey, int keyBits);
}
