package tests.unit;

import java.util.Timer;

import blackberry.threadpool.TimerJob;
import blackberry.utils.Utils;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;

public class UT_TimerThread extends TestUnit {

	public UT_TimerThread(String name, Tests tests) {
		super(name, tests);

	}

	public boolean run() throws AssertException {
		
		TimerThreadCreate();
		TimerThreadPoolSingle();
		SchedulerMulti();
		
		return true;
	}

	private void TimerThreadPoolSingle() throws AssertException {
		Timer timer = new Timer();

		TestJob job = new TestJob("TestTimerThread",0, 1000);
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
	

	private void SchedulerMulti() throws AssertException {
		Timer scheduler = new Timer();
		
		int NUM_JOBS = 30;
		TimerJob[] jobs = new TimerJob[NUM_JOBS];

		long timestamp = Utils.getTime();
		
		for (int i = 0; i < NUM_JOBS; i++) {
			TimerJob job = new TestJob("Job_"+i,0, 1000);
			jobs[i] = job;

			job.enable(true);
			job.addToTimer(scheduler);
			
		}
		
		Utils.sleep(5000);

		for (int i = 0; i < NUM_JOBS; i++) {
			TestJob job = (TestJob)jobs[i];
			AssertThat(job.started == 1, "started should be 1");
			AssertThat(job.stopped == 0, "stopped should be 0");
			AssertThat(job.isScheduled(), "not scheduled");
			
		}
		
		scheduler.cancel();
		for (int i = 0; i < NUM_JOBS; i++) {
			TestJob job = (TestJob)jobs[i];
			job.stop();
		}
		
		long elapsed = Utils.getTime() - timestamp;
		int secs = (int)( elapsed / 1000 );
		
		Utils.sleep(200);
		
		for (int i = 0; i < NUM_JOBS; i++) {
			TimerJob job = jobs[i];
			AssertThat(job.getRunningLoops() >= 5, "runningLoop should br >= 5");
			AssertThat(job.getRunningLoops() <= secs + 1, "runningLoop should br <= " + (secs + 1));
			AssertThat(job.isEnabled(), "not enabled");
			AssertThat(!job.isScheduled(), "still scheduled");
		}
		// AssertThat(job.isRunning(), "not running");
		// AssertThat(!job.isRunning(), "still running");
	}

	private void TimerThreadCreate() throws AssertException {
		TimerJob job = new TestJob("TestTimerThread",0, 1000);

		AssertThat(job.getRunningLoops() == 0, "runningLoop shold be 0");
		AssertThat(!job.isEnabled(), "enabled");
		AssertThat(!job.isScheduled(), "scheduled");
	}
}

class TestJob extends TimerJob {

	public int started;
	public int stopped;
	public int runned;
	
	public TestJob(String name, int delay, int every) {
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

}