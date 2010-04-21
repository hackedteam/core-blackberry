package com.ht.rcs.blackberry.crypto;

import net.rim.device.api.crypto.AESDecryptorEngine;
import net.rim.device.api.crypto.AESEncryptorEngine;
import net.rim.device.api.crypto.AESKey;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.CryptoUnsupportedOperationException;

public class RimAES implements CryptoEngine {

    AESKey aeskey;
    AESEncryptorEngine aesencrypt;
    AESDecryptorEngine aesdecrypt;

    public static boolean isSupported() {
        AESKey aeskey = new AESKey();

        try {
            AESEncryptorEngine aesencrypt = new AESEncryptorEngine(aeskey);
        } catch (CryptoTokenException e) {
            return false;
        } catch (CryptoUnsupportedOperationException e) {
            return false;
        }

        return true;
    }

    public void decrypt(byte[] ct, byte[] pt) throws CryptoTokenException {
        aesdecrypt.decrypt(ct, 0, pt, 0);
    }

    public void encrypt(byte[] pt, byte[] ct) throws CryptoTokenException {

        aesencrypt.encrypt(pt, 0, ct, 0);

    }

    public boolean makeKey(byte[] cipherKey, int keyBits) {
        aeskey = new AESKey(cipherKey, 0, keyBits);
        try {
            aesencrypt = new AESEncryptorEngine(aeskey);
            aesdecrypt = new AESDecryptorEngine(aeskey);

        } catch (CryptoTokenException e) {
            return false;
        } catch (CryptoUnsupportedOperationException e) {
            return false;
        }
        return true;
    }

}
