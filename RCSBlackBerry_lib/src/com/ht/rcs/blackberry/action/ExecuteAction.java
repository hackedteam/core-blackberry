/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : ExecuteAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.action;

public class ExecuteAction extends SubAction {
    public ExecuteAction(int actionId_, byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    public boolean execute() {
        // #debug
        debug.info("Execute");
        return true;
    }

    protected boolean parse(byte[] confParams) {

        return true;
    }

}
