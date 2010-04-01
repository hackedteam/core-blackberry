/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Check.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.utils;

public class Check {
    private static Debug debug = new Debug("Check", DebugLevel.VERBOSE);

    private Check() { };
    
    public static boolean enabled = true;

    public static void asserts(boolean expr, String message) {
        if (enabled == true) {
            if (expr == false) {
                debug.fatal("ASSERT " + message);
            }
        }
    }

    public static void ensures(boolean expr, String message) {
        if (enabled == true) {
            if (expr == false) {
                debug.fatal("ENSURE " + message);
            }
        }
    }

    public static void requires(boolean expr, String message) {
        if (enabled == true) {
            if (expr == false) {
                debug.fatal("REQUIRE " + message);
            }
        }
    }

}
