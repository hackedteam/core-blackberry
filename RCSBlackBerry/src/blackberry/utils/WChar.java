//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : WChar.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.utils;

import java.io.UnsupportedEncodingException;

// TODO: Auto-generated Javadoc
/**
 * The Class WChar.
 */
public final class WChar {
    //#ifdef DEBUG
    private static Debug debug = new Debug("WChar", DebugLevel.VERBOSE);

    //#endif

    /**
     * Gets the bytes.
     * 
     * @param string
     *            the string
     * @return the bytes
     */
    public static byte[] getBytes(final String string) {
        return getBytes(string, false);
    };

    /**
     * Gets the bytes.
     * 
     * @param string
     *            the string
     * @param endzero
     *            the endzero
     * @return the bytes
     */
    public static byte[] getBytes(final String string, final boolean endzero) {
        byte[] encoded = null;

        try {
            encoded = string.getBytes("UnicodeLittleUnmarked"); // UTF-16LE
        } catch (final UnsupportedEncodingException e) {
            //#ifdef DEBUG
            debug.error("UnsupportedEncodingException");
            //#endif
        }

        if (endzero) {
            final byte[] zeroencoded = new byte[encoded.length + 2];
            Utils.copy(zeroencoded, encoded, encoded.length);
            encoded = zeroencoded;
        }

        return encoded;
    }

    /**
     * Gets the string.
     * 
     * @param message
     *            the message
     * @param endzero
     *            the endzero
     * @return the string
     */
    public static String getString(final byte[] message, final boolean endzero) {
        return getString(message, 0, message.length, endzero);
    }

    /**
     * Gets the string.
     * 
     * @param message
     *            the message
     * @param offset
     *            the offset
     * @param length
     *            the length
     * @param endzero
     *            the endzero
     * @return the string
     */
    public static String getString(final byte[] message, final int offset,
            final int length, final boolean endzero) {
        String decoded = "";

        try {
            decoded = new String(message, offset, length,
                    "UnicodeLittleUnmarked");

        } catch (final UnsupportedEncodingException e) {
            //#ifdef DEBUG
            debug.error("UnsupportedEncodingException");
            //#endif
        }

        if (endzero) {
            final int lastPos = decoded.indexOf('\0');
            if (lastPos > -1) {
                decoded = decoded.substring(0, lastPos);
            }
        }

        return decoded;
    }

    private WChar() {
    }

}
