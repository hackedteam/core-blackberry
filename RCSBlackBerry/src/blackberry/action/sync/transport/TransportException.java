//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.action.sync.transport;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class TransportException extends Exception {
    //#ifdef DEBUG
    private static Debug debug = new Debug("TransportEx",
            DebugLevel.VERBOSE);
    //#endif
    private int value;

    public TransportException(int i) {
        //#ifdef DEBUG
        debug.trace("TransportException: " + i);
        //#endif
        this.value=i;
    }

    //#ifdef DEBUG
    public String toString(){
        return "TransportException: " + value;
    }
    //#endif
}
