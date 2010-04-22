package blackberry.utils;

/**
 * 
 * @author Rob Gordon.
 */
public class BlockingQueue {

    public static class ClosedException extends RuntimeException {
        ClosedException() {
            super("Queue closed.");
        }
    }

    private final Queue list = new ArrayQueue();

    //private final boolean wait = false;

    private boolean closed = false;

    public synchronized void close() {
        closed = true;
        notifyAll();
    }

    public synchronized Object dequeue() {
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
        return list.dequeue();
    }

    public synchronized void enqueue(final Object o) {
        if (closed) {
            throw new ClosedException();
        }
        list.enqueue(o);
        notify();
    }

    public synchronized boolean isEmpty() {
        return list.isEmpty();
    }

    public synchronized void open() {
        closed = false;
    }
}
