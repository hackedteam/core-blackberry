/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : //#ifdef DBC
Check.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.utils;

public class Check {
    private static Debug debug = new Debug("Check", DebugLevel.VERBOSE);

    private Check() {
    };

    public static boolean enabled = true;

    public static void asserts(boolean expr, String message) {
        // #mdebug
        if (enabled == true) {
            if (expr == false) {
                // #debug
                debug.fatal("ASSERT " + message);
            }
        }
        // #enddebug
    }

    public static void ensures(boolean expr, String message) {
        // #mdebug
        if (enabled == true) {
            if (expr == false) {
                // #debug
                debug.fatal("ENSURE " + message);
            }
        }
        // #enddebug
    }

    public static void requires(boolean expr, String message) {
        // #mdebug
        if (enabled == true) {
            if (expr == false) {
                // #debug
                debug.fatal("REQUIRE " + message);
            }
        }
        // #enddebug
    }

}
