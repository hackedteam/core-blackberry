package blackberry.crypto;

import net.rim.device.api.crypto.CryptoException;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.util.Arrays;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;
import blackberry.utils.Utils;

public class EncryptionPKCS5 extends Encryption {
    //#ifdef DEBUG
    private static Debug debug = new Debug("EncryptionPKCS5",
            DebugLevel.VERBOSE);

    //#endif
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
        final int clen = getNextMultiple(len);
        int value = clen - len;
        //#ifdef DEBUG
        debug.trace("pad " + len + " " + clen + " : " + value);
        //#endif
        final byte[] padplain = new byte[clen];

        Utils.copy(padplain, 0, plain, offset, len);

        for (int i = 1; i <= value; i++) {
            padplain[clen - i] = (byte) value;
        }

        //#ifdef DEBUG
        debug.trace("padded: " + Utils.byteArrayToHex(padplain));
        //#endif
        return padplain;
    }

    public byte[] decryptData(final byte[] cyphered) throws CryptoException {
        return decryptData(cyphered, 0);
    }

    public byte[] decryptData(final byte[] cyphered, final int offset) throws CryptoException {
        //#ifdef DEBUG
        debug.trace("decryptData");
        //#endif

        final int enclen = cyphered.length - offset;
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
            
            if(padlen <= 0 || padlen > 16){
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
        Check.ensures(plain!=null, "null plain");
        Check.ensures(plain.length == plainlen, "wrong plainlen");
        //#endif
        return plain;
    }
}
