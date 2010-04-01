package com.ht.rcs.blackberry.transfer;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class CommandException extends Exception {
    static Debug debug = new Debug("CommandException", DebugLevel.VERBOSE);

    public CommandException(String string) {
        super(string);
        debug.error(string);
    }
}
