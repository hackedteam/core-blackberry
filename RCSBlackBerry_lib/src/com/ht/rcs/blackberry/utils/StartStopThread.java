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

    public void restart() {
        // #debug
        debug.info("Restarting... " + this);
        needToRestart = true;
        needToStop = true;
    }
    
    protected boolean smartSleep(int millis) {
        boolean ret = simpleSleep(millis);
        return ret;
    }

    /**
     * Smart sleep.
     * 
     * @param millisec
     *            the millisec
     * @return true, if successful
     */
    protected boolean smartSleepWait(int millis) {
        int loops = 0;

        if (millis <= sleepTime) {
            simpleSleep(millis);

            if (needToStop) {
                needToStop = false;
                return true;
            }

            return false;
        }

        loops = millis / sleepTime;

        Date now = new Date();

        long timeUntil = now.getTime() + millis;
      
        // #debug
        debug.trace("smartSleep start: " + this.getName() + " for:" + loops);
        while (loops > 0) {

            now = new Date();
            long timestamp = now.getTime();
            if (timestamp > timeUntil) {
                // #mdebug
                debug.info("Exiting at loop:" + loops + " error: "
                        + (timestamp - timeUntil));
                // #enddebug
                break;
            }

            simpleSleep(millis);
            loops--;

            if (needToStop) {
                needToStop = false;
                return true;
            }
        }

        // #debug
        debug.trace("smartSleep end: " + this.getName());
        return false;
    }

    private synchronized boolean simpleSleep(int millis) {
       
        try {
            wait(millis);
            //sleep(millisec);
            //yield();
            return false;
        } catch (InterruptedException e) {
            // #debug
            debug.error("Interrupted");
            return true;
        }
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
