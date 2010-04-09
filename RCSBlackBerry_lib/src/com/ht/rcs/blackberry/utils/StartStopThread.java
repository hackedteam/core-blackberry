package com.ht.rcs.blackberry.utils;

import java.util.Date;

public abstract class StartStopThread extends Thread {

    /** The debug instance. */
    private static Debug debug = new Debug("StartStopThread",
            DebugLevel.VERBOSE);

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
    public void stop() {
        // #debug
        debug.info("Stopping... " + this);
        needToStop = true;
    }

    public void restart() {
        // #debug
        debug.info("Restarting... " + this);
        needToRestart = true;
        needToStop = true;
    }

    /**
     * Smart sleep.
     * 
     * @param millisec
     *            the millisec
     * @return true, if successful
     */
    protected boolean smartSleep(int millisec) {
        int loops = 0;

        if (millisec <= sleepTime) {
            simpleSleep(millisec);

            if (needToStop) {
                needToStop = false;
                return true;
            }

            return false;
        }

        loops = millisec / sleepTime;

        Date now = new Date();

        long timeUntil = now.getTime() + millisec;

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

            simpleSleep(millisec);
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

    private void simpleSleep(int millisec) {
        try {
            sleep(millisec);
            yield();
        } catch (InterruptedException e) {
            // #debug
            debug.error("Interrupted");
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
