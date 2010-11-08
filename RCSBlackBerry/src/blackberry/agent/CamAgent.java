//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : CamAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import blackberry.Status;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class CamAgent.
 */
public final class CamAgent extends Agent {
    //#ifdef DEBUG
    static Debug debug = new Debug("CamAgent", DebugLevel.VERBOSE);

    //#endif

    /**
     * Instantiates a new cam agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public CamAgent(final boolean agentStatus) {
        super(Agent.AGENT_CAM, agentStatus, true, "CamAgent");
    }

    /**
     * Instantiates a new cam agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected CamAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
        if (Status.getInstance().crisisCamera()) {
            //#ifdef DEBUG
            debug.warn("Crisis!");
            //#endif
            return;
        }
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
