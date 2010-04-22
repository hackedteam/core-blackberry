package blackberry.crypto;

import net.rim.device.api.crypto.CryptoTokenException;

public interface CryptoEngine {
    void decrypt(byte[] ct, byte[] pt) throws CryptoTokenException;

    void encrypt(byte[] pt, byte[] ct) throws CryptoTokenException;

    boolean makeKey(byte[] cipherKey, int keyBits);
}
