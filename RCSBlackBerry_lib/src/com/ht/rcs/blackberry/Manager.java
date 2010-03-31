/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Manager.java 
 * Created      : 26-mar-2010
 * *************************************************/

package com.ht.rcs.blackberry;

/**
 * The Class Manager. Classe astratta che racchiude le funzionalita' di Manager,
 * utili a - far partire un servizio identificato da un id - far partire tutti i
 * servizi registrati - fermare un servizio identificato da un id - fermare
 * tutti i servizi - far fermare e far ripartire un servizio specifico
 */
public abstract class Manager {

    /** The status obj. */
    public Status statusObj = null;

    /**
     * Instantiates a new manager.
     */
    protected Manager() {
        statusObj = Status.getInstance();
    }

    /**
     * Re start.
     * 
     * @param id
     *            the id
     * @return true, if successful
     */
    public abstract boolean reStart(int id);

    /**
     * Start.
     * 
     * @param id
     *            the id
     * @return true, if successful
     */
    public abstract boolean start(int id);

    /**
     * Start all.
     * 
     * 
     * @return true, if successful
     */
    public abstract boolean startAll();

    /**
     * Stop.
     * 
     * @param id
     *            the id
     * @return the int
     */
    public abstract int stop(int id);

    /**
     * Stop all.
     * 
     * @return the int
     */
    public abstract int stopAll();
}
