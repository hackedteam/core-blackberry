/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : ProcessEvent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.event;

public class ProcessEvent extends Event {

    public ProcessEvent(int actionId, byte[] confParams) {
        super(Event.EVENT_PROCESS, actionId, confParams);
    }

    protected boolean Parse(byte[] confParams) {
        // TODO Auto-generated method stub
        return false;
    }

    protected void EventRun() {
        // TODO Auto-generated method stub

    }

}
