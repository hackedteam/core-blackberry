//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : tests.unit
 * File         : UT_Events.java
 * Created      : 28-apr-2010
 * *************************************************/
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

// TODO: Auto-generated Javadoc
/**
 * The Class UT_Events.
 */
public final class UT_Events extends TestUnit {

    /**
     * Instantiates a new u t_ events.
     * 
     * @param name
     *            the name
     * @param tests
     *            the tests
     */
    public UT_Events(final String name, final Tests tests) {
        super(name, tests);

    }

    /*
     * (non-Javadoc)
     * @see tests.TestUnit#run()
     */
    public boolean run() throws AssertException {
        TimerEventSingle();
        TimerEventRepeat();
        TimerEventDate();

        return true;
    }

    /**
     * Timer event date.
     * 
     * @return true, if successful
     * @throws AssertException
     *             the assert exception
     */
    public boolean TimerEventDate() throws AssertException {
        //#ifdef DEBUG_INFO
        debug.info("-- TimerEventDate --");
        //#endif

        final Status status = Status.getInstance();
        final EventManager eventManager = EventManager.getInstance();
        status.clear();

        final Action action = new Action(0);
        action.addNewSubAction(SubAction.ACTION_EXECUTE, null);
        status.addAction(action);
        AssertThat(!action.isTriggered(), "action triggered");

        // creo timer che si esegua una volta dopo 1 secondo

        //#ifdef DEBUG_TRACE
        debug.trace("TIMER_DATE");

        //#endif

        final long timestamp = Utils.getTime() + 1000;
        final Date tmpDate = new Date(timestamp);
        //#ifdef DEBUG_TRACE
        debug.trace(tmpDate.toString());
        //#endif

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

    /**
     * Timer event repeat.
     * 
     * @return true, if successful
     * @throws AssertException
     *             the assert exception
     */
    public boolean TimerEventRepeat() throws AssertException {
        //#ifdef DEBUG_INFO
        debug.info("-- TimerEventRepeat --");
        //#endif

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

    /**
     * Timer event single.
     * 
     * @return true, if successful
     * @throws AssertException
     *             the assert exception
     */
    public boolean TimerEventSingle() throws AssertException {
        //#ifdef DEBUG_INFO
        debug.info("-- TimerEventSingle --");
        //#endif

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
