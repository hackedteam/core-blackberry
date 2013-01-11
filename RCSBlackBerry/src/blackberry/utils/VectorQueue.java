//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.utils;

import java.util.Vector;

public class VectorQueue implements Queue {

    Vector vector = new Vector();

    public synchronized Object dequeue() {
        if (isEmpty()) {
            throw new UnderflowException("dequeue");
        }
        Object obj = vector.elementAt(0);
        vector.removeElementAt(0);
        return obj;
    }

    public synchronized void enqueue(Object x) {
        vector.addElement(x);
    }

    public synchronized Object getFront() {
        if (isEmpty()) {
            throw new UnderflowException("getFront");
        }

        Object obj = vector.elementAt(0);
        vector.removeElementAt(0);
        return obj;
    }

    public synchronized boolean isEmpty() {
        return vector.isEmpty();
    }

    public synchronized void clear() {
        vector.removeAllElements();
    }

    public void remove(Object x) {
        vector.removeElement(x);
    }

    public int size() {
        return vector.size();
    }

    public boolean contains(Object x) {        
        return vector.contains(x);
    }

}
