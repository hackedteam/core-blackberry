package blackberry.threadpool;

import blackberry.utils.ArrayQueue;
import blackberry.utils.BlockingQueue;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Queue;

/**
 * implementation of a thread pool.
 * 
 * @author thanks to: Rob Gordon.
 */
public class ThreadPool {
    //#debug
    static Debug debug = new Debug("ThreadPool", DebugLevel.VERBOSE);

    protected final BlockingQueue queue = new BlockingQueue();
    protected boolean closed = true;

    private int poolSize;

    public ThreadPool(int numThreads) {
        poolSize = numThreads;
    }

    public void setPoolSize(final int poolSize_) {
        this.poolSize = poolSize_;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public synchronized void start() {
        if (!closed) {
            throw new IllegalStateException("Pool already started.");
        }
        closed = false;
        for (int i = 0; i < poolSize; ++i) {
            new PooledThread(i).start();
        }
    }

    public synchronized void execute(final Runnable job) {
        if (closed) {
            throw new PoolClosedException();
        }
        queue.enqueue(job);
    }

    private class PooledThread extends Thread {

        int id;

        public PooledThread(int i) {
            id = i;
        }

        public void run() {
            while (true) {
                final Runnable job = (Runnable) queue.dequeue();

                if (job == null) {
                    break;
                }

                try {
                    //#debug
                    debug.trace("Pool " + id + " run:" + job);
                    job.run();
                    //#debug
                    debug.trace("Pool " + id + " end:" + job);
                } catch (final Throwable t) {
                    // ignore
                } 
            }
        }
    }

    public void close() {
        closed = true;
        queue.close();
    }

    static class PoolClosedException extends RuntimeException {
        PoolClosedException() {
            super("Pool closed.");
        }
    }

}

/*
 * Copyright ? 2004, Rob Gordon.
 */

