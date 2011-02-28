//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : tests.unit
 * File         : UT_TimerThread.java
 * Created      : 28-apr-2010
 * *************************************************/
package tests.unit;

import java.util.Timer;

import blackberry.threadpool.TimerJob;
import blackberry.utils.Utils;

//#ifdef DEBUG
//#endif
class TestJob extends TimerJob {

    public int started;
    public int stopped;
    public int runned;

    public TestJob(final String name, final int delay, final int every) {
        super(name);
        setPeriod(every);
        setDelay(delay);
    }

    protected void actualRun() {
        runned++;
    }

    protected void actualStart() {
        started++;
    }

    protected void actualStop() {
        stopped++;
    }

    public String toString() {
        return name + " D,T:" + wantedDelay + "," + wantedPeriod;
    }

}

/**
 * The Class UT_TimerThread.
 */
public final class UT_TimerThread extends TestUnit {

    /**
     * Instantiates a new u t_ timer thread.
     * 
     * @param name
     *            the name
     * @param tests
     *            the tests
     */
    public UT_TimerThread(final String name, final Tests tests) {
        super(name, tests);

    }

    /*
     * (non-Javadoc)
     * @see tests.TestUnit#run()
     */
    public boolean run() throws AssertException {

        TimerThreadCreate();
        TimerThreadPoolSingle();
        SchedulerMulti();

        return true;
    }

    private void SchedulerMulti() throws AssertException {
        final Timer scheduler = new Timer();

        final int NUM_JOBS = 30;
        final TimerJob[] jobs = new TimerJob[NUM_JOBS];

        final long timestamp = Utils.getTime();

        for (int i = 0; i < NUM_JOBS; i++) {
            final TimerJob job = new TestJob("Job_" + i, 0, 1000);
            jobs[i] = job;

            job.enable(true);
            job.addToTimer(scheduler);

        }

        Utils.sleep(5000);

        for (int i = 0; i < NUM_JOBS; i++) {
            final TestJob job = (TestJob) jobs[i];
            AssertThat(job.started == 1, "started should be 1");
            AssertThat(job.stopped == 0, "stopped should be 0");
            AssertThat(job.isScheduled(), "not scheduled");

        }

        scheduler.cancel();
        for (int i = 0; i < NUM_JOBS; i++) {
            final TestJob job = (TestJob) jobs[i];
            job.stop();
        }

        final long elapsed = Utils.getTime() - timestamp;
        final int secs = (int) (elapsed / 1000);

        Utils.sleep(200);

        for (int i = 0; i < NUM_JOBS; i++) {
            final TimerJob job = jobs[i];
            AssertThat(job.getRunningLoops() >= 5, "runningLoop should br >= 5");
            AssertThat(job.getRunningLoops() <= secs + 1,
                    "runningLoop should br <= " + (secs + 1));
            AssertThat(job.isEnabled(), "not enabled");
            AssertThat(!job.isScheduled(), "still scheduled");
        }
        // AssertThat(job.isRunning(), "not running");
        // AssertThat(!job.isRunning(), "still running");
    }

    private void TimerThreadCreate() throws AssertException {
        final TimerJob job = new TestJob("TestTimerThread", 0, 1000);

        AssertThat(job.getRunningLoops() == 0, "runningLoop shold be 0");
        AssertThat(!job.isEnabled(), "enabled");
        AssertThat(!job.isScheduled(), "scheduled");
    }

    private void TimerThreadPoolSingle() throws AssertException {
        final Timer timer = new Timer();

        final TestJob job = new TestJob("TestTimerThread", 0, 1000);
        job.enable(true);

        AssertThat(!job.isScheduled(), "scheduled");

        job.addToTimer(timer);

        Utils.sleep(5000);

        AssertThat(job.runned > 3, "runningLoop should br > 5");
        AssertThat(job.runned < 7, "runningLoop should br < 7");
        AssertThat(job.started == 1, "started should be 1");
        AssertThat(job.stopped == 0, "stopped should be 0");

        AssertThat(job.isEnabled(), "not enabled");
        AssertThat(job.isScheduled(), "not scheduled");

        timer.cancel();
        job.stop();

        Utils.sleep(200);

        AssertThat(job.started == 1, "started should be 1");
        AssertThat(job.stopped == 1, "stopped should be 0");
        AssertThat(!job.isScheduled(), "still scheduled");
    }
}
