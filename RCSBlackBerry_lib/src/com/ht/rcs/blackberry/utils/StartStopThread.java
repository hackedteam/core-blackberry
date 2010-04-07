package com.ht.rcs.blackberry.utils;

public abstract class StartStopThread extends Thread {

    /** The debug. */
    private static Debug debug = new Debug("StartStopThread",
            DebugLevel.VERBOSE);

    /** The Need to stop. */
    protected boolean needToStop = false;
    protected boolean needToRestart = false;

    /** The Running. */
    protected boolean running = false;

    protected boolean enabled = false;

    int sleepTime = 1000;

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
        return running;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    public void run() {
        debug.info("Run " + this);
        needToStop = false;
        running = true;

        do {
            needToRestart = false;
            actualRun();
        } while (!needToRestart);

        running = false;
        debug.info("End " + this);
    }

    /**
     * Stop.
     */
    public void stop() {
        debug.info("Stopping... " + this);
        needToStop = true;
    }

    public void restart() {
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
            Utils.sleep(millisec);

            if (needToStop) {
                needToStop = false;
                return true;
            }         

            return false;
        } else {
            loops = millisec / sleepTime;
        }

        while (loops > 0) {
            Utils.sleep(millisec);
            loops--;

            if (needToStop) {
                needToStop = false;
                return true;
            }
        }

        return false;
    }

    protected boolean sleepUntilStopped() {
        for (;;) {
            if (smartSleep(sleepTime)) {
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
}
