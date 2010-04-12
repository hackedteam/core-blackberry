/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : ToothingAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.action;

import com.ht.rcs.blackberry.event.Event;

public class ToothingAction extends SubAction {
    public ToothingAction(int actionId_, byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    public boolean execute(Event triggeringEvent) {
        // TODO Auto-generated method stub
        return false;
    }

    protected boolean parse(byte[] confParams) {
        // TODO Auto-generated method stub
        return false;
    }

}
