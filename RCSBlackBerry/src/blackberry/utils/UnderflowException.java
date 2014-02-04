//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.utils
 * File         : UnderflowException.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.utils;

//#ifdef DEBUG
//#endif
/**
 * Exception class for access in empty containers such as stacks, queues, and
 * priority queues.
 * 
 * @author Mark Allen Weiss
 */
public class UnderflowException extends RuntimeException {
    /**
     * Construct this exception object.
     * 
     * @param message
     *            the error message.
     */
    public UnderflowException(final String message) {
        super(message);
    }
}
