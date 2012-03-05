//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : ProtocolException.java
 * Created      : 6-apr-2010
 * *************************************************/
package blackberry.action.sync.protocol;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;


/**
 * The Class ProtocolException. Viene lanciato in caso di eccezione durante
 * l'esecuzione di un protocollo. Come effetto fa cadere la comunicazione.
 */
public class ProtocolException extends Exception {
    //#ifdef DEBUG
    static Debug debug = new Debug("ProtocolEx", DebugLevel.VERBOSE);
    //#endif

    public boolean bye;

    private int value;

    /**
     * Instantiates a new protocol exception.
     * 
     * @param string
     *            the string
     * @param bye_
     *            the bye_
     */
    public ProtocolException(final boolean bye_) {
        bye = bye_;
    }

    public ProtocolException() {
        this(false);
    }

    public ProtocolException(int i) {
        this(false);
        this.value=i;
    }
}
