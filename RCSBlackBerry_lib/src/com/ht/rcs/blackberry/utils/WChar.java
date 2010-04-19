/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : WChar.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.utils;

import java.io.UnsupportedEncodingException;

public class WChar {
    //#debug
    private static Debug debug = new Debug("WChar", DebugLevel.VERBOSE);

    private WChar() {
    };

    public static byte[] getBytes(String string) {
        return getBytes(string, false);
    }

    public static byte[] getBytes(String string, boolean endzero) {
        byte[] encoded = null;

        try {
            encoded = string.getBytes("UnicodeLittleUnmarked");
        } catch (UnsupportedEncodingException e) {
            // #debug
            debug.error("UnsupportedEncodingException");
        }

        if (endzero) {
            byte[] zeroencoded = new byte[encoded.length + 4];
            Utils.copy(zeroencoded, encoded, encoded.length);
            encoded = zeroencoded;
        }

        return encoded;
    }

    public static String getString(byte[] message, int offset, int length,
            boolean endzero) {
        String decoded = "";

        try {
            decoded = new String(message, offset, length,
                    "UnicodeLittleUnmarked");

        } catch (UnsupportedEncodingException e) {
            // #debug
            debug.error("UnsupportedEncodingException");
        }

        if (endzero) {
            int lastPos = decoded.length() - 1;
            if (decoded.charAt(lastPos) == '\0'
                    || decoded.charAt(lastPos) == '\u0000') {
                decoded = decoded.substring(0, lastPos);

            }
        }

        return decoded;
    }

    public static String getString(byte[] message, boolean endzero) {
        return getString(message, 0, message.length, endzero);
    }

}
