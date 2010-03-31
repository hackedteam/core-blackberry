package com.ht.tests.unit;

import com.ht.rcs.blackberry.Conf;
import com.ht.rcs.blackberry.EventManager;
import com.ht.rcs.blackberry.Status;
import com.ht.rcs.blackberry.action.Action;
import com.ht.rcs.blackberry.action.SubAction;
import com.ht.rcs.blackberry.event.TimerEvent;
import com.ht.rcs.blackberry.utils.Utils;
import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;

public class UT_Events extends TestUnit {

	public UT_Events(String name, Tests tests) {
		super(name, tests);

	}

	public boolean TimerEventTest() throws AssertException {
		debug.info("-- TimerEventTest --");

		Status status = Status.getInstance();
		EventManager eventManager = EventManager.getInstance();
		status.Clear();

		Action action = new Action(0);
		action.addNewSubAction(SubAction.ACTION_EXECUTE, null);
		status.AddAction(action);
		AssertThat(!action.isTriggered(), "action triggered");

		// creo timer che si esegua una volta dopo 1 secondo
		TimerEvent event = new TimerEvent(0, Conf.CONF_TIMER_SINGLE, 1000, 0);
		status.AddEvent(0, event);
		AssertThat(!event.isRunning(), "event running");
		eventManager.startAll();

		Utils.Sleep(1000);
		// verifico che l'evento sia partito
		AssertThat(event.isRunning(), "event not running");

		Utils.Sleep(1500);
		AssertThat(action.isTriggered(), "action not triggered");

		return true;

	}

	public boolean run() throws AssertException {
		TimerEventTest();

		return true;
	}

}
