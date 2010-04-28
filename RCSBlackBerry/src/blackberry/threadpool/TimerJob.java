/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.threadpool
 * File         : TimerJob.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.threadpool;

import java.util.Timer;
import java.util.TimerTask;

import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class TimerJob.
 */
public abstract class TimerJob extends TimerTask {

    protected static final long SOON = 0;

    protected static final long NEVER = Integer.MAX_VALUE;

    //#mdebug
    private static Debug debug = new Debug("TimerJob", DebugLevel.NOTIFY);
    //#enddebug

    protected boolean running = false;
    protected boolean enabled = false;

    protected boolean stopped;
    protected boolean scheduled;

    private int runningLoops = 0;
    protected String name;

    /* private boolean enqueued; */
    //private static int numThreads = 0;

    protected long wantedPeriod;
    protected long wantedDelay;

    /**
     * Instantiates a new timer job.
     * 
     * @param name_
     *            the name_
     */
    public TimerJob(final String name_) {
        name = name_;

        wantedPeriod = NEVER;
        wantedDelay = SOON;

        stopped = true;

        //#ifdef DBC
        Check.requires(wantedPeriod >= 0, "Every has to be >=0");
        Check.requires(wantedDelay >= SOON, "Every has to be >=0");
        //#endif
    }

    /**
     * Instantiates a new timer job.
     * 
     * @param name_
     *            the name_
     * @param delay_
     *            the delay_
     * @param period_
     *            the period_
     */
    public TimerJob(final String name_, final long delay_, final long period_) {
        this(name_);
        setPeriod(period_);
        setDelay(delay_);
    }

    /**
     * Ogni volta che il timer richiede l'esecuzione del task viene invocato
     * questo metodo.
     */
    protected abstract void actualRun();

    /**
     * La prima volta che viene lanciata l'esecuzione del task, oppure dopo una
     * stop, viene chiamato questo metodo, prima della actualRun. Serve per
     * inizializzare l'ambiente, aprire i file e le connessioni.
     */
    protected void actualStart() {

    }

    /**
     * Questo metodo viene invocato alla stop. Si usa per chiudere i file aperti
     * nella actualStart
     */
    protected void actualStop() {

    }

    /**
     * Adds the to timer.
     * 
     * @param timer
     *            the timer
     */
    public final void addToTimer(final Timer timer) {
        //#debug debug
        debug.trace("adding timer");
        timer.schedule(this, getDelay(), getPeriod());
        scheduled = true;
    }

    /**
     * Enable.
     * 
     * @param enabled_
     *            the enabled_
     */
    public final void enable(final boolean enabled_) {
        enabled = enabled_;
    }

    /**
     * Gets the delay.
     * 
     * @return the delay
     */
    public final long getDelay() {
        return wantedDelay;
    }

    /**
     * Gets the period.
     * 
     * @return the period
     */
    public final long getPeriod() {
        return wantedPeriod;
    }

    /**
     * Gets the running loops.
     * 
     * @return the running loops
     */
    public final int getRunningLoops() {

        return runningLoops;
    }

    /**
     * Checks if is enabled.
     * 
     * @return true, if is enabled
     */
    public final boolean isEnabled() {
        return enabled;
    }

    private boolean isOneshot() {
        return wantedPeriod == NEVER;
    }

    /**
     * Checks if is running.
     * 
     * @return true, if is running
     */
    public final synchronized boolean isRunning() {
        return !stopped;
    }

    /**
     * Checks if is scheduled.
     * 
     * @return true, if is scheduled
     */
    public final synchronized boolean isScheduled() {

        return scheduled;

    }

    /**
     * Restart.
     */
    public final void restart() {
        //if (isOneshot()) {
        run();
        //}
    }

    /*
     * (non-Javadoc)
     * @see java.util.TimerTask#run()
     */
    public final synchronized void run() {
        // #debug debug
        debug.trace("Run " + this);

        if (stopped) {
            stopped = false;
            actualStart();
        }

        runningLoops++;

        try {
            // #debug debug
            debug.trace("actualRun " + this);
            running = true;
            actualRun();
        } finally {
            running = false;
        }

        // #debug debug
        debug.trace("End " + this);
    }

    /**
     * Sets the delay.
     * 
     * @param delay_
     *            the new delay
     */
    protected final void setDelay(final long delay_) {
        if (delay_ < 0) {
            //#debug
            debug.error("negative delay");
            wantedDelay = 0;
        } else {

            wantedDelay = delay_;
        }
        //#debug debug
        debug.trace("setDelay: " + wantedDelay);
    }

    /**
     * Sets the period.
     * 
     * @param period_
     *            the new period
     */
    protected final void setPeriod(final long period) {
        if (period < 0) {
            //#debug
            debug.error("negative period");
            wantedPeriod = 0;
        } else {
            wantedPeriod = period;
        }
        //#debug debug
        debug.trace("setPeriod: " + wantedPeriod);
    }

    /**
     *Stop.
     */
    public final synchronized void stop() {
        // #debug info
        debug.info("Stopping... " + this);

        stopped = true;
        cancel();
        scheduled = false;
        actualStop();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public abstract String toString();

}
