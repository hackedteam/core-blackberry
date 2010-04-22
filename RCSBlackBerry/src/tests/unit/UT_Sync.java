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

public class UT_Sync extends TestUnit {

    //#debug
    static Debug debug = new Debug("UT_Sync", DebugLevel.VERBOSE);

    String host = "rcs-prod";
    //String host = "192.168.1.149";
    int port = 80;

    TransferAccessor transfer;

    boolean remoteTest = false;

    public UT_Sync(final String name, final Tests tests) {
        super(name, tests);
        transfer = new TransferAccessor();
    }

    private void ConnectionRemoteTest() throws AssertException {
        //#debug
        debug.info("- ConnectionRemoteTest -");
        final String remoteHost = "iperbole.suppose.it";
        port = 8080;
        final DirectTcpConnection connection = new DirectTcpConnection(
                remoteHost, port, false, false);
        final boolean connected = connection.connect();
        AssertThat(connected, "not connected");

        try {
            //#debug
            debug.trace("send");
            // connection.send("HelloWorld".getBytes());
            final boolean ret = connection.send(Keys.getInstance()
                    .getChallengeKey());
            AssertThat(ret, "cannot send");
            //#debug
            debug.trace("receive");
            final byte[] rec = connection.receive(5);
            final String string = new String(rec);
            //#debug
            debug.trace("Received: " + string);
        } catch (final IOException e) {
            //#debug
            debug.error(e.toString());
        }

        connection.disconnect();
    }

    private void ConnectionTest() throws AssertException {
        //#debug
        debug.info("- ConnectionTest -");
        final DirectTcpConnection connection = new DirectTcpConnection(host,
                port, false, false);
        final boolean connected = connection.connect();
        AssertThat(connected, "not connected");

        connection.disconnect();
    }

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
        //#debug
        debug.info("- SyncTest -");
        transfer.init(host, port, false, false);

        //#debug
        debug.info("transfer sending");
        final boolean ret = transfer.send();
        AssertThat(ret == true, "Doesn't send transfer");

    }

    private void TransferSecureTest() throws AssertException {
        //#debug
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
        //#debug
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
