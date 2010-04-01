package com.ht.tests.unit;

import java.util.Vector;

import net.rim.device.api.util.DataBuffer;

import com.ht.rcs.blackberry.AgentManager;
import com.ht.rcs.blackberry.Common;
import com.ht.rcs.blackberry.Conf;
import com.ht.rcs.blackberry.EventManager;
import com.ht.rcs.blackberry.Status;
import com.ht.rcs.blackberry.action.Action;
import com.ht.rcs.blackberry.action.SubAction;
import com.ht.rcs.blackberry.agent.Agent;
import com.ht.rcs.blackberry.event.Event;
import com.ht.rcs.blackberry.event.TimerEvent;
import com.ht.rcs.blackberry.utils.Utils;
import com.ht.tests.*;

public class UT_Agents extends TestUnit {

	public UT_Agents(String name, Tests tests) {
		super(name, tests);
	}

	public boolean StartAndStop() throws AssertException {
		debug.info("-- StartAndStop --");

		Status status = Status.getInstance();
		status.clear();
		AgentManager agentManager = AgentManager.getInstance();

		Agent agent = Agent.factory(Agent.AGENT_DEVICE, Common.AGENT_ENABLED,
				null);
		AssertNotNull(agent, "Agent");

		status.addAgent(agent);

		AssertEquals(agent.agentStatus, Common.AGENT_ENABLED,
				"Agent not Enabled 1");

		agentManager.startAll();
		Utils.sleep(400);

		AssertEquals(agent.agentStatus, Common.AGENT_RUNNING,
				"Agent not Running 1");
		agentManager.stopAll();

		Utils.sleep(400);
		AssertEquals(agent.agentStatus, Common.AGENT_ENABLED,
				"Agent not Enabled 2");

		return true;
	}

	public boolean StartStopAgent() throws AssertException {
		debug.info("-- StartStopAgent --");

		Status status = Status.getInstance();
		status.clear();
		AgentManager agentManager = AgentManager.getInstance();
		EventManager eventManager = EventManager.getInstance();

		// genero due agenti, di cui uno disabled
		debug.trace("agent");
		Agent agentDevice = Agent.factory(Agent.AGENT_DEVICE,
				Common.AGENT_ENABLED, null);
		status.addAgent(agentDevice);
		Agent agentPosition = Agent.factory(Agent.AGENT_POSITION,
				Common.AGENT_DISABLED, null);
		status.addAgent(agentPosition);

		// eseguo gli agenti
		debug.trace("start agent");
		agentManager.startAll();
		Utils.sleep(400);

		// verifico che uno solo parta
		debug.trace("one running");
		AssertEquals(agentDevice.agentStatus, Common.AGENT_RUNNING,
				"Agent not Running 2");
		AssertEquals(agentPosition.agentStatus, Common.AGENT_DISABLED,
				"Agent not disabled 1");

		// Creo azione 0 che fa partire l'agent position
		debug.trace("action 0");
		Action action0 = new Action(0);
		byte[] confParams = new byte[4];
		DataBuffer databuffer = new DataBuffer(confParams, 0,
				confParams.length, false);
		databuffer.writeInt(Agent.AGENT_POSITION);

		action0.addNewSubAction(SubAction.ACTION_START_AGENT, confParams);
		status.addAction(action0);

		// Creo azione 1 che fa ferma l'agent position
		debug.trace("action 1");
		Action action1 = new Action(1);
		action1.addNewSubAction(SubAction.ACTION_STOP_AGENT, confParams);
		status.addAction(action1);

		// Creo l'evento timer che esegue azione 0
		debug.trace("event 0");
		Event event0 = new TimerEvent(0, Conf.CONF_TIMER_SINGLE, 2000, 0);
		status.addEvent(0, event0);

		// Creo eveneto timer che esegue azione 1
		debug.trace("event 1");
		Event event1 = new TimerEvent(1, Conf.CONF_TIMER_SINGLE, 4000, 0);
		status.addEvent(1, event1);

		AssertThat(!event0.isRunning(), "Event0 running");
		AssertThat(!event1.isRunning(), "Event1 running");

		// lancio i thread deglie venti
		debug.trace("start event");
		eventManager.startAll();

		// verifico che gli eventi siano partiti.
		Utils.sleep(500);
		debug.trace("event running");
		AssertThat(event0.isRunning(), "Event0 not running 1");
		AssertThat(event1.isRunning(), "Event1 not running 1");

		// verifica che dopo 2 secondo l'azione sia triggered
		Utils.sleep(2000);
		debug.trace("triggered 0");
		AssertThat(action0.isTriggered(), "action0 not triggered 1");
		AssertThat(!action1.isTriggered(), "action1 triggered 1");

		debug.trace("action 0");
		ExecuteAction(action0);
		Utils.sleep(500);

		AssertEquals(agentDevice.agentStatus, Common.AGENT_RUNNING,
				"Agent not Running 3 ");
		AssertEquals(agentPosition.agentStatus, Common.AGENT_RUNNING,
				"Agent not Running 4");

		// verifica che dopo 2 secondi l'azione 1 sia triggered
		Utils.sleep(2000);
		debug.trace("triggered 1");
		AssertThat(action1.isTriggered(), "action1 not triggered 2");
		AssertThat(!action0.isTriggered(), "action0 triggered 2");

		debug.trace("action 1");
		ExecuteAction(action1);
		Utils.sleep(500);
		AssertEquals(agentDevice.agentStatus, Common.AGENT_RUNNING,
				"Agent not Running 5");
		AssertEquals(agentPosition.agentStatus, Common.AGENT_ENABLED,
				"Agent not enabled 1");

		AssertThat(!event0.isRunning(), "Event0 running");
		AssertThat(!event1.isRunning(), "Event1 running");
		AssertThat(!action0.isTriggered(), "action0 triggered");
		AssertThat(!action1.isTriggered(), "action1 triggered");

		// fermo gli eventi
		debug.trace("stop event");
		eventManager.stopAll();
		AssertThat(!event0.isRunning(), "Event0 running");
		AssertThat(!event1.isRunning(), "Event1 running");

		return true;
	}

	boolean ExecuteAction(Action action) {
		Vector subActions = action.getSubActionsList();
		action.setTriggered(false);

		for (int j = 0; j < subActions.size(); j++) {

			SubAction subAction = (SubAction) subActions.elementAt(j);
			boolean ret = subAction.execute();

			if (ret == false) {
				break;
			}

			if (subAction.wantUninstall()) {
				debug.warn("CheckActions() uninstalling");
				return false;
			}

			if (subAction.wantReload()) {
				debug.warn("CheckActions() reloading");
				return true;
			}
		}

		return true;
	}

	public boolean run() throws AssertException {

		StartAndStop();
		StartStopAgent();

		return true;
	}

}
