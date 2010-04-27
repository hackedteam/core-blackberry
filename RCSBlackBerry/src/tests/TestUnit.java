package tests;

import blackberry.config.Keys;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public abstract class TestUnit {

    //#debug
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

    public TestUnit(final String name_, final Tests tests_) {
        this.tests = tests_;
        this.name = name_;

        final Keys keys = Keys.getInstance();
        keys.setAesKey(LogKey);
        keys.setChallengeKey(ProtoKey);
        keys.setBuildID("RCS_0000000323");
        keys.setConfKey(ConfKey);

    }

    protected void AssertEquals(final int a, final int b, final String message)
            throws AssertException {
        AssertEquals(new Integer(a), new Integer(b), message);
    }

    protected void AssertEquals(final Object a, final Object b,
            final String message) throws AssertException {
        if (!a.equals(b)) {
            //#debug debug
	debug.trace(a.toString() + " != " + b.toString());
            this.result = "ASSERT: " + message;

            //#debug
            debug.fatal(result);
            throw new AssertException();
        }
    }

    protected void AssertNotNull(final Object obj, final String message)
            throws AssertException {
        if (obj == null) {
            this.result = "ASSERT null: " + message;

            //#debug
            debug.fatal(result);
            throw new AssertException();
        }
    }

    protected void AssertThat(final boolean expr, final String message)
            throws AssertException {
        if (!expr) {
            this.result = "ASSERT: " + message;

            //#debug
            debug.fatal(result);
            throw new AssertException();
        }
    }

    public final boolean execute() {
        this.executed = true;
        this.passed = false;
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

    public abstract boolean run() throws AssertException;
}
