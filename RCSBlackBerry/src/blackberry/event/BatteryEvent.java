/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : BatteryEvent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

public class BatteryEvent extends Event {

    public BatteryEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_BATTERY, actionId, confParams);
    }

    protected void actualRun() {
        // TODO Auto-generated method stub

    }

    protected boolean parse(final byte[] confParams) {
        // TODO Auto-generated method stub
        return false;
    }

}
