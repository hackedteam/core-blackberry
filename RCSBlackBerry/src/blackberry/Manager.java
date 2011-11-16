//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Manager.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;


/**
 * The Class Manager. Classe astratta che racchiude le funzionalita' di Manager,
 * utili a - far partire un servizio identificato da un id - far partire tutti i
 * servizi registrati - fermare un servizio identificato da un id - fermare
 * tutti i servizi - far fermare e far ripartire un servizio specifico
 */
public abstract class Manager {

    //#ifdef DEBUG
    private static Debug debug = new Debug("Manager", DebugLevel.VERBOSE);
    //#endif

    /** The status obj. */
    protected Status status = null;

 
    protected Hashtable hashtable;

    /**
     * Instantiates a new manager.
     */
    protected Manager() {
        status = Status.getInstance();
        hashtable=new Hashtable();
    }
    

    /*
     * public final boolean isRunning(int id) { return getItem(id).isRunning();
     * }
     */
    
    /**
     * Gets the item.
     * 
     * @param id
     *            the id
     * @return the item
     */
    /*
     * (non-Javadoc)
     * @see blackberry.Manager#getItem(int)
     */
    public final synchronized Managed get(final String id) {
        //#ifdef DBC
        Check.requires(status != null, "Null status");
        //#endif
        final Managed managed = (Managed) hashtable.get(id);
        //#ifdef DBC
        if(managed!=null){
            Check.ensures(managed.getId().equals(id), "Wrong id");
        }
        //#endif

        return managed;
    }


    public synchronized final void add(final Managed managed){
        //#ifdef DBC
        Check.requires(hashtable != null, "Null Agents");
        Check.requires(managed != null, "Null Agent");
        Check.requires(managed.getId() != null, "Id == " + managed.getId());
        Check.asserts(hashtable.containsKey(managed.getId()) == false,
                "Agent already present: " + managed);
        //#endif
        hashtable.put(managed.getId(), managed);
    }

    /*
     * public final boolean isRunning(int id) { return getItem(id).isRunning();
     * }
     */
    
    /**
     * Gets the all the TimerJob items.
     * 
     * @return the TimerJob items
     */
    public synchronized final Vector getAllItems() {
        //#ifdef DBC
        Check.requires(hashtable != null, "Null Agents");
        //#endif
    
        final Enumeration e = hashtable.elements();
        final Vector vect = new Vector();
    
        while (e.hasMoreElements()) {
            vect.addElement(e.nextElement());
        }
    
        //#ifdef DBC
        Check.ensures(hashtable.size() == vect.size(), "agents not equal to vect");
        //#endif
        return vect;
    }


    //#ifdef DEBUG
    public String toString(){
        StringBuffer buf = new StringBuffer();
    
    Vector vector = this.getAllItems();
    for (int i = 0; i < vector.size(); i++) {
        Managed managed = (Managed) vector.elementAt(i);
        buf.append("    " + managed);
        
    }
       return buf.toString();
    
    }
    //#endif
   

}
