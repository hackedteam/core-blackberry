//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.transfer
 * File         : CommandException.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.action.sync.protocol;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;


/**
 * The Class CommandException.
 */
public class CommandException extends Exception {
    //#ifdef DEBUG
    static Debug debug = new Debug("CommandException", DebugLevel.VERBOSE);

    //#endif
}
