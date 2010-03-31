/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : CellIdEvent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.event;

public class CellIdEvent extends Event {

    public CellIdEvent(int actionId, byte[] confParams) {
        super(Event.EVENT_CELLID, actionId, confParams);
    }

    protected void EventRun() {
        // TODO Auto-generated method stub

    }

    protected boolean Parse(byte[] confParams) {
        // TODO Auto-generated method stub
        return false;
    }

}
