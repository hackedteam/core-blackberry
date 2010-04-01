/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : SmsAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.action;

public class SmsAction extends SubAction {

    public SmsAction(int actionId_, byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    public boolean execute() {
        // TODO Auto-generated method stub
        return false;
    }

    protected boolean parse(byte[] confParams) {
        // TODO Auto-generated method stub
        return false;
    }

}
