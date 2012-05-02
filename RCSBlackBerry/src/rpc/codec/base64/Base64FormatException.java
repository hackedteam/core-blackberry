//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

// Base64FormatException.java
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package rpc.codec.base64;

/** Exception for invalid BASE64 streams. */

public class Base64FormatException extends Exception {

    /**
     * Create that kind of exception
     * 
     * @param msg
     *            The associated error message
     */

    public Base64FormatException(String msg) {
        super(msg);
    }

}
