package blackberry.utils;

import blackberry.Trigger;


public class BlockingQueueTrigger {

    public BlockingQueueTrigger(){
        open();
    }
    
    /**
     * The Class ClosedException.
     */
    public static class ClosedException extends RuntimeException {

        /**
         * Instantiates a new closed exception.
         */
        ClosedException() {
            super("Queue closed.");
        }
    }

    private final Queue list = new VectorQueue();

    //private final boolean wait = false;

    private boolean closed = false;

    /**
     * Close.
     */
    public synchronized void close() {
        closed = true;
        notifyAll();
    }

    Object blockedLock = new Object();

    /**
     * Dequeue.
     * 
     * @return the object
     */
    private synchronized Trigger dequeue() {
        while (!closed && list.isEmpty()) {
            try {
                wait();
            } catch (final InterruptedException e) {
                
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        return (Trigger)list.dequeue();
    }

    /**
     * Enqueue.
     * 
     * @param o
     *            the o
     */
    public synchronized void enqueue(final Trigger o) {
        if (closed) {
            throw new ClosedException();
        }
        list.enqueue(o);
        notifyAll();
    }

    /**
     * Checks if is empty.
     * 
     * @return true, if is empty
     */
    public synchronized boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Open.
     */
    public synchronized void open() {
        closed = false;
    }

    public synchronized Trigger getTriggeredAction() {
        return dequeue();
    }

    public synchronized void unTriggerAll() {
           list.makeEmpty();
    }

    public synchronized void unTrigger(int actionId) {
        
        list.remove(new Trigger(actionId,null));
    }

    public void clear() {
        close();
        Utils.sleep(1000);
        open();        
    }

}