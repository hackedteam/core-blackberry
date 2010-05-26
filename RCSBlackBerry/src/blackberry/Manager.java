//#preprocess
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

    //#ifdef DEBUG
    private static Debug debug = new Debug("Manager", DebugLevel.VERBOSE);

    //#endif

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
     * Gets the all the TimerJob items.
     * 
     * @return the TimerJob items
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
        if(timer==null){
            timer = new Timer();
        }
        
        //#ifdef DBC
        Check.requires(timer != null, "Timer null");
        //#endif

        //#ifdef DEBUG_TRACE
        debug.trace("restart " + id);

        //#endif
        final boolean ret = true;

        final TimerJob job = getItem(id);

        if (job == null) {
            //#ifdef DEBUG
            debug.error("Thread unknown: " + id);
            //#endif
            return false;
        }

        if (job.isEnabled() && job.isScheduled()) {
            job.restart(timer);
        } else {
            //#ifdef DEBUG
            debug.warn("cannot restart: " + id + " enabled:" + job.isEnabled()
                    + " scheduled:" + job.isScheduled());
            //#endif
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

        if(timer==null){
            timer = new Timer();
        }
        
        //#ifdef DBC
        Check.requires(timer != null, "Timer null");
        //#endif

        final TimerJob job = getItem(id);
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
            //#ifdef DEBUG_INFO
            debug.info("Already scheduled" + id);
            //#endif
            return true;
        }

        boolean ret;
        try {
            job.addToTimer(timer);
            ret = true;
            //#ifdef DEBUG_TRACE
            debug.trace("Start() OK");
            //#endif

        } catch (IllegalStateException ex) {
            //#ifdef DEBUG_ERROR
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
                    //#ifdef DEBUG_TRACE
                    debug.trace("Starting: " + job);
                    //#endif
                    job.addToTimer(timer);

                } else {
                    //#ifdef DEBUG_TRACE
                    debug.trace("Not starting because disabled: " + job);
                    //#endif
                }

                // Utils.sleep(100);
            }
        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error(ex.toString());
            //#endif
        }

        //#ifdef DEBUG_TRACE
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
    public final synchronized boolean stop(final int id) {
        //#ifdef DBC
        Check.requires(timer != null, "Timer null");
        //#endif

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
