package com.ht.rcs.blackberry.threadpool;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

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

    public synchronized void execute(final Job job) {
        if (closed) {
            throw new PoolClosedException();
        }
        queue.enqueue(job);
        job.setEnqueued(true);
    }

    private class PooledThread extends Thread {

        int id;

        public PooledThread(int i) {
            id = i;
        }

        public void run() {
            while (true) {
                final Job job = (Job) queue.dequeue();

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
                } finally {
                    job.setEnqueued(false);
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

/**
 * 
 * @author Rob Gordon.
 */
class BlockingQueue {

    private final Queue list = new ArrayQueue();
    private boolean closed = false;

    //private final boolean wait = false;

    public synchronized void enqueue(final Object o) {
        if (closed) {
            throw new ClosedException();
        }
        list.enqueue(o);
        notify();
    }

    public synchronized QueueObject dequeue() {
        while (!closed && list.isEmpty()) {
            try {
                wait();
            } catch (final InterruptedException e) {
                // ignore
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        return (QueueObject) list.dequeue();
    }

    public synchronized boolean isEmpty() {
        return list.isEmpty();
    }

    public synchronized void close() {
        closed = true;
        notifyAll();
    }

    public synchronized void open() {
        closed = false;
    }

    public static class ClosedException extends RuntimeException {
        ClosedException() {
            super("Queue closed.");
        }
    }
}
