//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : tests.unit
 * File         : UT_Self.java
 * Created      : 28-apr-2010
 * *************************************************/
package tests.unit;

import tests.TestUnit;
import tests.Tests;

/**
 * The Class UT_Self.
 */
public final class UT_Self extends TestUnit {

    /**
     * Instantiates a new u t_ self.
     * 
     * @param name
     *            the name
     * @param tests
     *            the tests
     */
    public UT_Self(final String name, final Tests tests) {
        super(name, tests);
    }

    /*
     * (non-Javadoc)
     * @see tests.TestUnit#run()
     */
    public boolean run() {

        //#ifdef DEBUG
        debug.info("run " + name);

        //#endif
        return true;
    }
}
