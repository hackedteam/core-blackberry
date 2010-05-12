/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Check.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.utils;

/**
 * The Class Check.
 */
public final class Check {
  //#ifdef DBC
    //#debug
    private static Debug debug = new Debug("Check", DebugLevel.VERBOSE);

    public static boolean enabled = true;;

    /**
     * Asserts.
     * 
     * @param expr
     *            the expr
     * @param message
     *            the message
     */
    public static void asserts(final boolean expr, final String message) {
        // #mdebug
        if (enabled == true) {
            if (expr == false) {
                // #debug
                debug.fatal("ASSERT " + message);
            }
        }
        // #enddebug
    }

    /**
     * Ensures.
     * 
     * @param expr
     *            the expr
     * @param message
     *            the message
     */
    public static void ensures(final boolean expr, final String message) {
        // #mdebug
        if (enabled == true) {
            if (expr == false) {
                // #debug
                debug.fatal("ENSURE " + message);
            }
        }
        // #enddebug
    }

    /**
     * Requires.
     * 
     * @param expr
     *            the expr
     * @param message
     *            the message
     */
    public static void requires(final boolean expr, final String message) {
        // #mdebug
        if (enabled == true) {
            if (expr == false) {
                // #debug
                debug.fatal("REQUIRE " + message);
            }
        }
        // #enddebug
    }

    private Check() {
    }
//#endif
}


