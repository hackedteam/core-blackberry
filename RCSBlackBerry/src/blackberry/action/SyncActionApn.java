//#preprocess
package blackberry.action;

import java.io.EOFException;
import java.util.Vector;

import net.rim.device.api.util.DataBuffer;
import blackberry.AgentManager;
import blackberry.event.Event;
import blackberry.log.LogCollector;
import blackberry.transfer.Transfer;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.WChar;

public class SyncActionApn extends SyncAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("SyncActionApn", DebugLevel.VERBOSE);
    //#endif

    String host;
    Vector apns = new Vector();

    public SyncActionApn(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);

        //#ifdef DBC
        Check.requires(actionId == ACTION_SYNC_APN, "Wrong ActionId");
        //#endif

        logCollector = LogCollector.getInstance();
        agentManager = AgentManager.getInstance();
        transfer = Transfer.getInstance();
    }

    protected SyncActionApn(int actionId) {
        super(actionId);
    }

    protected boolean parse(byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        try {
            int hostLen = databuffer.readInt();
            byte[] hostRaw = new byte[hostLen];

            databuffer.readFully(hostRaw);
            host = WChar.getString(hostRaw, true);

            //#ifdef DEBUG_INFO
            debug.info("host: " + host);
            //#endif

            int entries = databuffer.readInt(); // readByte();
            int len;
            byte[] stringRaw;

            for (int i = 0; i < entries; i++) {
                Apn apn = new Apn();
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

}
