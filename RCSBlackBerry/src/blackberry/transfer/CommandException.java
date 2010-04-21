package blackberry.transfer;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class CommandException extends Exception {
	//#debug
    static Debug debug = new Debug("CommandException", DebugLevel.VERBOSE);

    public CommandException(String string) {
        super(string);
        // #debug
        debug.error(string);
    }
}
