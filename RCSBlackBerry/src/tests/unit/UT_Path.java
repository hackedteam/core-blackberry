//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : tests.unit
 * File         : UT_Path.java
 * Created      : 28-apr-2010
 * *************************************************/
package tests.unit;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import blackberry.fs.Path;

//#ifdef DEBUG
//#endif
/**
 * The Class UT_Path.
 */
public final class UT_Path extends TestUnit {
    String dir1 = "test1Path/";
    String dir2 = "test2Path/";

    /**
     * Instantiates a new u t_ path.
     * 
     * @param name
     *            the name
     * @param tests
     *            the tests
     */
    public UT_Path(final String name, final Tests tests) {
        super(name, tests);

    }

    private void CreateDirTest() throws AssertException {
        //#ifdef DEBUG
        debug.trace("CreateDirTest");
        //#endif
        boolean ret;
        Path.removeDirectory(Path.USER() + dir1 + dir2);
        Path.removeDirectory(Path.USER() + dir1);

        ret = Path.createDirectory(Path.USER() + dir1);
        AssertThat(ret, "Cannot create dir1 ");
        ret = Path.createDirectory(Path.USER() + dir1 + dir2);
        AssertThat(ret, "Cannot create dir2 ");
    }

    private void RemoveDirTest() throws AssertException {
        //#ifdef DEBUG
        debug.trace("RemoveDirTest");
        //#endif
        boolean ret;
        ret = Path.removeDirectory(Path.USER() + dir1);
        AssertThat(!ret, "shouldn't delete dir1 ");
        ret = Path.removeDirectory(Path.USER() + dir1 + dir2);
        AssertThat(ret, "Cannot delete dir2 ");
        ret = Path.removeDirectory(Path.USER() + dir1);
        AssertThat(ret, "Cannot delete dir1 ");
    }

    /*
     * (non-Javadoc)
     * @see tests.TestUnit#run()
     */
    public boolean run() throws AssertException {
        Path.printRoots();
        CreateDirTest();
        RemoveDirTest();
        return true;
    }

}
