package tests.unit;

import java.util.Date;

import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import blackberry.Conf;
import blackberry.EventManager;
import blackberry.Status;
import blackberry.action.Action;
import blackberry.action.SubAction;
import blackberry.event.TimerEvent;
import blackberry.utils.Utils;

public class UT_Events extends TestUnit {

    public UT_Events(final String name, final Tests tests) {
        super(name, tests);

    }

    public boolean run() throws AssertException {
        TimerEventSingle();
        TimerEventRepeat();
        TimerEventDate();

        return true;
    }

    public boolean TimerEventDate() throws AssertException {
        // #debug
        debug.info("-- TimerEventDate --");

        final Status status = Status.getInstance();
        final EventManager eventManager = EventManager.getInstance();
        status.clear();

        final Action action = new Action(0);
        action.addNewSubAction(SubAction.ACTION_EXECUTE, null);
        status.addAction(action);
        AssertThat(!action.isTriggered(), "action triggered");

        // creo timer che si esegua una volta dopo 1 secondo

        // #debug
        debug.trace("TIMER_DATE");

        final long timestamp = Utils.getTime() + 1000;
        final Date tmpDate = new Date(timestamp);
        // #debug
        debug.trace(tmpDate.toString());

        final int hiDelay = (int) (timestamp >>> 32);
        final int loDelay = (int) (timestamp & 0xffffffff);

        final TimerEvent event = new TimerEvent(0, Conf.CONF_TIMER_DATE,
                loDelay, hiDelay);
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

    public boolean TimerEventRepeat() throws AssertException {
        // #debug
        debug.info("-- TimerEventRepeat --");

        final Status status = Status.getInstance();
        final EventManager eventManager = EventManager.getInstance();
        status.clear();

        final Action action = new Action(0);
        action.addNewSubAction(SubAction.ACTION_EXECUTE, null);
        status.addAction(action);
        AssertThat(!action.isTriggered(), "action triggered");

        // creo timer che si esegua ogni 100 ms
        final TimerEvent event = new TimerEvent(0, Conf.CONF_TIMER_REPEAT, 100,
                0);
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

    public boolean TimerEventSingle() throws AssertException {
        // #debug
        debug.info("-- TimerEventSingle --");

        final Status status = Status.getInstance();
        final EventManager eventManager = EventManager.getInstance();
        status.clear();

        final Action action = new Action(0);
        action.addNewSubAction(SubAction.ACTION_EXECUTE, null);
        status.addAction(action);
        AssertThat(!action.isTriggered(), "action triggered");

        // creo timer che si esegua una volta dopo 100 milli secondi
        final TimerEvent event = new TimerEvent(0, Conf.CONF_TIMER_SINGLE, 100,
                0);
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

}
