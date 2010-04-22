/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : QuotaEvent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

public class QuotaEvent extends Event {

    public QuotaEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_QUOTA, actionId, confParams);
    }

    protected void actualRun() {
        // TODO Auto-generated method stub

    }

    protected boolean parse(final byte[] confParams) {
        // TODO Auto-generated method stub
        return false;
    }

}
