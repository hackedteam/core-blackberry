//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : CamAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.module;

import blackberry.Status;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;


/**
 * The Class CamAgent.
 */
public final class ModuleCamera extends BaseModule {
    //#ifdef DEBUG
    static Debug debug = new Debug("CamAgent", DebugLevel.VERBOSE);
    //#endif

    /**
     * Instantiates a new cam agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public ModuleCamera(final boolean agentEnabled) {
        super(BaseModule.AGENT_CAM, agentEnabled, Conf.AGENT_CAM_ON_SD, "CamAgent");
    }

    /**
     * Instantiates a new cam agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected ModuleCamera(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualGo() {
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
        
        return false;
    }

}
