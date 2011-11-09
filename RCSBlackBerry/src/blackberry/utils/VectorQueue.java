package blackberry.utils;

import java.util.Vector;
//CONSTRUCTION: with no initializer
//
// ******************PUBLIC OPERATIONS*********************
// void enqueue( x )      --> Insert x
// Object getFront( )     --> Return least recently inserted item
// Object dequeue( )      --> Return and remove least recent item
// boolean isEmpty( )     --> Return true if empty; else false
// void makeEmpty( )      --> Remove all items
// ******************ERRORS********************************
// getFront or dequeue on empty queue
public class VectorQueue implements Queue {

    Vector vector = new Vector();
    
    public synchronized Object dequeue() {
        if (isEmpty()) {
            throw new UnderflowException("dequeue");
        }
        Object obj=vector.elementAt(0);
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
        
        Object obj=vector.elementAt(0);
        vector.removeElementAt(0);
        return obj;
    }

    public synchronized boolean isEmpty() {        
        return vector.isEmpty();
    }

    public synchronized void makeEmpty() {
        vector.removeAllElements();
    }

}
