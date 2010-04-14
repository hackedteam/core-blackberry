package com.ht.tests.unit;

import com.ht.rcs.blackberry.threadpool.Scheduler;
import com.ht.rcs.blackberry.threadpool.TimerJob;
import com.ht.rcs.blackberry.utils.Utils;
import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;

public class UT_TimerThread extends TestUnit {

	public UT_TimerThread(String name, Tests tests) {
		super(name, tests);

	}

	public boolean run() throws AssertException {
		SchedulerMulti();
		TimerThreadCreate();
		TimerThreadPoolSingle();
		TimerThreadPoolMulti();
		
		return true;
	}

	private void TimerThreadPoolSingle() throws AssertException {
		Scheduler scheduler = new Scheduler(1);

		TimerJob job = new TestTimerThread("TestTimerThread", 1000);
		job.enable(true);
		scheduler.add(job);

		scheduler.start();
		Utils.sleep(5000);

		AssertThat(job.getRunningLoops() > 3, "runningLoop should br > 5");
		AssertThat(job.getRunningLoops() < 7, "runningLoop should br < 7");
		AssertThat(job.isEnabled(), "not enabled");
		AssertThat(job.isRunning(), "not running");

		scheduler.stop();
		Utils.sleep(200);

		AssertThat(!job.isRunning(), "still running");
	}

	private void TimerThreadPoolMulti() throws AssertException {
		Scheduler scheduler = new Scheduler(10);

		TimerJob job = new TestTimerThread("TestTimerThread", 1000);
		job.enable(true);
		scheduler.add(job);

		scheduler.start();
		Utils.sleep(5000);

		AssertThat(job.getRunningLoops() > 3, "runningLoop should br > 5");
		AssertThat(job.getRunningLoops() < 7, "runningLoop should br < 7");
		AssertThat(job.isEnabled(), "not enabled");
		// AssertThat(job.isRunning(), "not running");

		scheduler.stop();
		Utils.sleep(200);

		// AssertThat(!job.isRunning(), "still running");
	}

	private void SchedulerMulti() throws AssertException {
		Scheduler scheduler = new Scheduler(10);

		int NUM_JOBS = 50;
		TimerJob[] jobs = new TimerJob[NUM_JOBS];

		for (int i = 0; i < NUM_JOBS; i++) {
			TimerJob job = new TestTimerThread("TestTimerThread", 1000);
			jobs[i] = job;
			job.enable(true);
			scheduler.add(job);
		}

		scheduler.start();
		Utils.sleep(5000);

		scheduler.stop();
		Utils.sleep(200);
		
		for (int i = 0; i < NUM_JOBS; i++) {
			TimerJob job = jobs[i];
			AssertThat(job.getRunningLoops() > 2, "runningLoop should br > 5");
			AssertThat(job.getRunningLoops() < 8, "runningLoop should br < 7");
			AssertThat(job.isEnabled(), "not enabled");
		}
		// AssertThat(job.isRunning(), "not running");
		// AssertThat(!job.isRunning(), "still running");
	}

	private void TimerThreadCreate() throws AssertException {
		TimerJob job = new TestTimerThread("TestTimerThread", 1000);

		AssertThat(job.getRunningLoops() == 0, "runningLoop shold be 0");
		AssertThat(!job.isEnabled(), "enabled");
		AssertThat(!job.isRunning(), "running");
	}

}

class TestTimerThread extends TimerJob {

	public TestTimerThread(String name, int every) {
		super(name);
		setEvery(every);
	}

	protected void actualRun() {
		System.out.println("actualRun");
	}

}