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

import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;


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
    public ClipBoardAgent(final boolean agentEnabled) {
        super(Agent.AGENT_CLIPBOARD, agentEnabled, Conf.AGENT_CLIPBOARD_ON_SD, "ClipBoardAgent");
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
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        //#ifdef DEBUG
        debug.trace("parse");
        //#endif
        return false;
    }
}
