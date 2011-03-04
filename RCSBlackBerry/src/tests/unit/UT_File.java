//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : tests.unit
 * File         : UT_File.java
 * Created      : 28-apr-2010
 * *************************************************/
package tests.unit;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import blackberry.fs.AutoFlashFile;
import blackberry.fs.Path;
import blackberry.utils.Utils;

//#ifdef DEBUG
//#endif
/**
 * The Class UT_File.
 */
public final class UT_File extends TestUnit {

    /**
     * Instantiates a new u t_ file.
     * 
     * @param name
     *            the name
     * @param tests
     *            the tests
     */
    public UT_File(final String name, final Tests tests) {
        super(name, tests);
    }

    private void FileAppendTest() throws AssertException {
        final AutoFlashFile file = new AutoFlashFile(Path.SD()
                + "testAppend.txt", false);
        boolean ret = file.create();
        AssertThat(ret == true, "Cannot create");

        byte[] read = file.read();
        AssertThat(read.length == 0, "read more than 0");

        file.write(42);
        read = file.read();
        AssertThat(read.length == 4, "read something wrong 1");

        file.append(100);
        read = file.read();
        AssertThat(read.length == 8, "read something wrong 2");

        int value = Utils.byteArrayToInt(read, 0);
        AssertThat(value == 42, "read something different 1 ");
        value = Utils.byteArrayToInt(read, 4);
        AssertThat(value == 100, "read something different 2");

        file.delete();
        ret = file.exists();
        AssertThat(ret == false, "still exists");

    }

    private void FileCreateHiddenTest() throws AssertException {
        final AutoFlashFile file = new AutoFlashFile(Path.SD()
                + "testHidden.txt", true);
        boolean ret = file.create();
        AssertThat(ret == true, "Cannot create");

        ret = file.exists();
        AssertThat(ret == true, "don't exists");

        file.delete();
        ret = file.exists();
        AssertThat(ret == false, "still exists");
    }

    private void FileCreateTest() throws AssertException {

        final AutoFlashFile file = new AutoFlashFile(Path.SD()
                + "testCreate.txt", false);
        boolean ret = file.create();
        AssertThat(ret == true, "Cannot create");

        ret = file.exists();
        AssertThat(ret == true, "don't exists");

        file.delete();
        ret = file.exists();
        AssertThat(ret == false, "still exists");
    }

    private void FileReadWriteTest() throws AssertException {
        final AutoFlashFile file = new AutoFlashFile(Path.SD() + "testRW.txt",
                false);
        boolean ret = file.create();
        AssertThat(ret == true, "Cannot create");

        byte[] read = file.read();
        AssertThat(read.length == 0, "read more than 0");

        file.write(42);
        read = file.read();
        AssertThat(read.length == 4, "read something wrong");

        final int value = Utils.byteArrayToInt(read, 0);
        AssertThat(value == 42, "read something different");

        file.delete();
        ret = file.exists();
        AssertThat(ret == false, "still exists");

    }

    private void PathSDPresentTest() {

        Path.isSDAvailable();

    }

    /*
     * (non-Javadoc)
     * @see tests.TestUnit#run()
     */
    public boolean run() throws AssertException {
        Path.makeDirs(Path.SD);
        Path.makeDirs(Path.USER);

        FileCreateTest();
        FileCreateHiddenTest();
        FileReadWriteTest();
        FileAppendTest();
        PathSDPresentTest();

        return true;
    }

}
