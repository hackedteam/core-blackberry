package com.ht.tests.unit;

import java.util.Date;

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

	public boolean TimerEventSingle() throws AssertException {
		// #debug
		debug.info("-- TimerEventSingle --");

		Status status = Status.getInstance();
		EventManager eventManager = EventManager.getInstance();
		status.clear();

		Action action = new Action(0);
		action.addNewSubAction(SubAction.ACTION_EXECUTE, null);
		status.addAction(action);
		AssertThat(!action.isTriggered(), "action triggered");

		// creo timer che si esegua una volta dopo 100 milli secondi
		TimerEvent event = new TimerEvent(0, Conf.CONF_TIMER_SINGLE, 100, 0);
		status.addEvent(0, event);
		AssertThat(!event.isRunning(), "event running");
		eventManager.startAll();
		AssertThat(event.isScheduled(), "event not running");

		Utils.sleep(1000);

		AssertThat(action.isTriggered(), "action not triggered");
		eventManager.stopAll();

		AssertThat(event.getRunningLoops() == 1, "not exactly one loop");

		return true;

	}

	public boolean TimerEventRepeat() throws AssertException {
		// #debug
		debug.info("-- TimerEventRepeat --");

		Status status = Status.getInstance();
		EventManager eventManager = EventManager.getInstance();
		status.clear();

		Action action = new Action(0);
		action.addNewSubAction(SubAction.ACTION_EXECUTE, null);
		status.addAction(action);
		AssertThat(!action.isTriggered(), "action triggered");

		// creo timer che si esegua ogni 100 ms
		TimerEvent event = new TimerEvent(0, Conf.CONF_TIMER_REPEAT, 100, 0);
		status.addEvent(0, event);
		AssertThat(!event.isRunning(), "event running");
		eventManager.startAll();
		AssertThat(event.isScheduled(), "event not running");

		Utils.sleep(1000);

		AssertThat(action.isTriggered(), "action not triggered");
		eventManager.stopAll();

		AssertThat(event.getRunningLoops() > 5, "not enough loops");
		AssertThat(event.getRunningLoops() < 15, "too many loops");

		return true;

	}

	public boolean TimerEventDate() throws AssertException {
		// #debug
		debug.info("-- TimerEventDate --");

		Status status = Status.getInstance();
		EventManager eventManager = EventManager.getInstance();
		status.clear();

		Action action = new Action(0);
		action.addNewSubAction(SubAction.ACTION_EXECUTE, null);
		status.addAction(action);
		AssertThat(!action.isTriggered(), "action triggered");

		// creo timer che si esegua una volta dopo 1 secondo

		// #debug
		debug.trace("TIMER_DATE");

		long timestamp = Utils.getTime() + 1000;
		Date tmpDate = new Date(timestamp);
		// #debug
		debug.trace(tmpDate.toString());

		int hiDelay = (int) (timestamp >>> 32);
		int loDelay = (int) (timestamp & 0xffffffff);

		TimerEvent event = new TimerEvent(0, Conf.CONF_TIMER_DATE, loDelay,
				hiDelay);
		status.addEvent(0, event);
		AssertThat(!event.isRunning(), "event running");
		eventManager.startAll();
		AssertThat(event.isScheduled(), "event not running");

		Utils.sleep(2000);

		AssertThat(action.isTriggered(), "action not triggered");
		eventManager.stopAll();

		AssertThat(event.getRunningLoops() == 1, "not exactly one loop");

		return true;

	}

	public boolean run() throws AssertException {
		TimerEventSingle();
		TimerEventRepeat();
		TimerEventDate();

		return true;
	}

}
