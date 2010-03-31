/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : SimChangeEvent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.event;

public class SimChangeEvent extends Event {

    public SimChangeEvent(int actionId, byte[] confParams) {
        super(Event.EVENT_SIM_CHANGE, actionId, confParams);
    }

    protected void EventRun() {
        // TODO Auto-generated method stub

    }

    protected boolean Parse(byte[] confParams) {
        // TODO Auto-generated method stub
        return false;
    }

}
