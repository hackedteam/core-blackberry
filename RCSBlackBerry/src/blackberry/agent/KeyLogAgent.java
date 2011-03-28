//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : KeyLogAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;


/**
 * The Class KeyLogAgent.
 */
public final class KeyLogAgent extends Agent {
    //#ifdef DEBUG
    static Debug debug = new Debug("KeyLogAgent", DebugLevel.VERBOSE);
    //#endif

    /**
     * Instantiates a new key log agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public KeyLogAgent(final boolean agentEnabled) {
        super(Agent.AGENT_KEYLOG, agentEnabled, Conf.AGENT_KEYLOG_ON_SD, "KeyLogAgent");

    }

    /**
     * Instantiates a new key log agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected KeyLogAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        
        return false;
    }

}
