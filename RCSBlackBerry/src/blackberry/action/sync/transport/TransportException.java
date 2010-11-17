//#preprocess
package blackberry.action.sync.transport;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class TransportException extends Exception {
    //#ifdef DEBUG
    private static Debug debug = new Debug("TransportEx",
            DebugLevel.VERBOSE);
    //#endif

    public TransportException(int i) {
        //#ifdef DEBUG
        debug.trace("TransportException: " + i);
        //#endif
    }

}
