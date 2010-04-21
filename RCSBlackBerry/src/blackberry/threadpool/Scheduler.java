package blackberry.threadpool;

import java.util.Vector;

import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.SimpleSortingVector;

import blackberry.threadpool.ThreadPool.PoolClosedException;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;
/*
public class Scheduler extends Thread {
    //#debug
    static Debug debug = new Debug("Scheduler", DebugLevel.VERBOSE);

    
     * public class JobComparator implements Comparator {
     * 
     * public int compare(Object arg0, Object arg1) { long ex1 = ((Job)
     * arg0).getNextExecution(); long ex2 = ((Job) arg1).getNextExecution();
     * return (int) (ex1 - ex2); } }
     

    ThreadPool threadPool;
    //SimpleSortingVector jobs = new SimpleSortingVector();
    Vector jobs = new Vector();

    private boolean toStop;

    public Scheduler(int numThreads) {
        threadPool = new ThreadPool(numThreads);
        
         * jobs.setSort(false); jobs.setSortComparator(new JobComparator());
         
    }

    public synchronized void run() {
        //#debug
        debug.trace("Run");

        threadPool.start();

        while (!toStop) {

            long timetowait = Long.MAX_VALUE;
            boolean standardWait = true;
          
            long now = Utils.getTime();

            synchronized (jobs) {

                for (int i = 0; i < jobs.size(); i++) {
                    Job job = (Job) jobs.elementAt(i);
                    if (job.isEnqueued()) {
                        //#debug
                        debug.trace("job.isEnqueued:" + job);
                        continue;
                    }
                    
                    long next = job.getNextExecution();

                    //#debug
                    debug.trace("next job:" + job);

                    long jobWait = (next - now);
                    if (jobWait <= 0) {
                        //#debug
                        debug.trace("executing: " + job);
                        threadPool.execute(job);
                    } else {
                        timetowait = Math.min(jobWait, timetowait);
                        standardWait = false;
                    }
                }
            }
            if (standardWait) {               
                //#debug
                debug.trace("all enqueued or executed");
                timetowait = 500;
            }

            //#ifdef DBC
            Check.asserts(timetowait != Long.MAX_VALUE, "I don't want to wait forever");
            //#endif
            
            try {
                
                //#debug
                debug.trace("waiting: " + timetowait);
                wait(timetowait);
            } catch (InterruptedException e) {
            }
        }
        
        //#debug
        debug.info("toStop");
    }

    public synchronized void stop() {
        //#debug
        debug.trace("stop: " + this);
        toStop = true;
        notifyAll();
        
        threadPool.close();
        synchronized (jobs) {

            for (int i = 0; i < jobs.size(); i++) {
                Job job = (Job) jobs.elementAt(i);
                job.stop();
            }
        }
    }

    public void add(final Job job) {
        //#debug
        synchronized (jobs) {
            debug.trace("add: " + job);
            jobs.addElement(job);
        }
    }
}
*/
