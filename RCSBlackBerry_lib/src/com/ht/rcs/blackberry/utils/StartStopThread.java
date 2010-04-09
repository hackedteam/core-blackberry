package com.ht.rcs.blackberry.utils;

import java.util.Date;

public abstract class StartStopThread extends Thread {

    /** The debug instance. */
    //#mdebug
    private static Debug debug = new Debug("StartStopThread",
            DebugLevel.VERBOSE);
    //#enddebug

    /** The Need to stop. */
    protected boolean needToStop = false;
    protected boolean needToRestart = false;

    /** The Running. */
    // protected boolean running = false;
    protected boolean enabled = false;

    int sleepTime = 1000;

    private int runningLoops = 0;

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
        return isAlive();
    }

    /*
     * @see java.lang.Thread#run()
     */
    public void run() {
        // #debug
        debug.info("Run " + this);
        needToStop = false;
        // running = true;

        do {

            needToRestart = false;
            runningLoops++;
            // #mdebug
            debug.info("Run innerloop: " + this + " runningLoops: "
                    + runningLoops);
            // #enddebug
            actualRun();

        } while (needToRestart);

        // #debug
        debug.info("End " + this);
    }

    /**
     * Stop.
     */
    public synchronized void stop() {
        // #debug
        debug.info("Stopping... " + this);
        needToStop = true;
        notifyAll();
    }

    public synchronized void restart() {
        // #debug
        debug.info("Restarting... " + this);
        needToRestart = true;
        needToStop = true;
        notifyAll();
    }
    
    protected boolean smartSleep(int millis) {
        boolean ret = simpleSleep(millis);
        return ret;
    }

    private synchronized boolean simpleSleep(int millis) {
       boolean ret;
        try {
            wait(millis);           
            ret = false;
        } catch (InterruptedException e) {            
            ret = true;
        }
        
        if (needToStop) {
            needToStop = false;
            ret = true;
        }
        
        return ret;
    }

    protected boolean sleepUntilStopped() {
        // #debug
        debug.info("Going forever sleep");
        for (;;) {
            if (smartSleep(sleepTime)) {
                // #debug
                debug.trace("CleanStop " + this);
                return true;
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable(boolean enabled_) {
        enabled = enabled_;
    }

    /**
     * @return the runningLoops
     */
    public int getRunningLoops() {
        return runningLoops;
    }
}
