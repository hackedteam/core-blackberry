/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Manager.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import java.util.Timer;
import java.util.Vector;

import blackberry.threadpool.TimerJob;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class Manager. Classe astratta che racchiude le funzionalita' di Manager,
 * utili a - far partire un servizio identificato da un id - far partire tutti i
 * servizi registrati - fermare un servizio identificato da un id - fermare
 * tutti i servizi - far fermare e far ripartire un servizio specifico
 */
public abstract class Manager {

    // #debug
    private static Debug debug = new Debug("Manager", DebugLevel.VERBOSE);

    /** The status obj. */
    public Status statusObj = null;

    private Timer timer = new Timer();

    /**
     * Instantiates a new manager.
     */
    protected Manager() {
        statusObj = Status.getInstance();
    }

    /**
     * Enable.
     * 
     * @param id
     *            the id
     */
    public final void enable(final int id) {
        final TimerJob job = getItem(id);
        job.enable(true);
    }

    /*
     * public final boolean isRunning(int id) { return getItem(id).isRunning();
     * }
     */

    /**
     * Gets the all items.
     * 
     * @return the all items
     */
    public abstract Vector getAllItems();

    /**
     * Gets the item.
     * 
     * @param id
     *            the id
     * @return the item
     */
    public abstract TimerJob getItem(int id);

    /**
     * Checks if is enabled.
     * 
     * @param id
     *            the id
     * @return true, if is enabled
     */
    public final boolean isEnabled(final int id) {
        final TimerJob job = getItem(id);
        return job.isEnabled();
    }

    /**
     * Re start.
     * 
     * @param id
     *            the id
     * @return true, if successful
     */
    public final boolean reStart(final int id) {
        // #ifdef DBC
        Check.requires(timer != null, "Timer null");
        // #endif

        // #debug debug
        debug.trace("restart " + id);
        final boolean ret = true;

        final TimerJob job = getItem(id);

        if (job == null) {
            // #debug
            debug.error("Thread unknown: " + id);
            return false;
        }

        if (job.isEnabled() && job.isScheduled()) {
            job.restart();
        } else {
            // #mdebug
            debug.warn("cannot restart: " + id + " enabled:" + job.isEnabled()
                    + " scheduled:" + job.isScheduled());
            // #enddebug
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
    public final synchronized boolean start(final int id) {

        // #ifdef DBC
        Check.requires(timer != null, "Timer null");
        // #endif

        final TimerJob job = getItem(id);
        if (job == null) {
            // #debug
            debug.error("Thread unknown: " + id);
            return false;
        }

        if (!job.isEnabled()) {
            // #debug
            debug.error("Not enabled [0] " + id);
            return false;
        }

        if (job.isScheduled()) {
            // #debug info
            debug.info("Already scheduled" + id);
            return true;
        }

        job.addToTimer(timer);

        // #debug debug
        debug.trace("Start() OK");
        return true;
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

        // #mdebug
        for (int i = 0; i < tsize; ++i) {
            TimerJob thread = (TimerJob) tasks.elementAt(i);
            debug.trace("Thread to start: " + thread);
            thread = null;
        }
        // #enddebug

        try {
            for (int i = 0; i < tsize; ++i) {
                final TimerJob job = (TimerJob) tasks.elementAt(i);

                if (job.isEnabled()) {
                    // #debug debug
                    debug.trace("Starting: " + job);
                    job.addToTimer(timer);

                } else {
                    // #debug debug
                    debug.trace("Not starting because disabled: " + job);
                }

                // Utils.sleep(100);
            }
        } catch (final Exception ex) {
            // #debug
            debug.error(ex.toString());
        }

        // #debug debug
        debug.trace("StartAll() OK");
        return true;
    }

    /**
     * Stop.
     * 
     * @param id
     *            the id
     * @return the int
     */
    public final synchronized boolean stop(final int id) {
        // #ifdef DBC
        Check.requires(timer != null, "Timer null");
        // #endif

        final TimerJob job = getItem(id);

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
            job.stop();
        }

        timer = null;
        return true;
    }
}
