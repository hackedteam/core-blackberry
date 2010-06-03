//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.transfer
 * File         : CommandException.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.transfer;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandException.
 */
public class CommandException extends Exception {
    //#ifdef DEBUG
    static Debug debug = new Debug("CommandException", DebugLevel.VERBOSE);

    //#endif

    /**
     * Instantiates a new command exception.
     * 
     * @param string
     *            the string
     */
    public CommandException(final String string) {
        super(string);
        //#ifdef DEBUG
        debug.error(string);
        //#endif
    }
}
