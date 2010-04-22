package tests.unit;

import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import blackberry.fs.Path;

public class UT_Path extends TestUnit {
    String dir1 = "test1Path/";
    String dir2 = "test2Path/";

    public UT_Path(final String name, final Tests tests) {
        super(name, tests);

    }

    private void CreateDirTest() throws AssertException {
        boolean ret;
        Path.removeDirectory(Path.SD_PATH + dir1 + dir2);
        Path.removeDirectory(Path.SD_PATH + dir1);

        ret = Path.createDirectory(Path.SD_PATH + dir1);
        AssertThat(ret, "Cannot create dir1 ");
        ret = Path.createDirectory(Path.SD_PATH + dir1 + dir2);
        AssertThat(ret, "Cannot create dir2 ");
    }

    private void RemoveDirTest() throws AssertException {
        boolean ret;
        ret = Path.removeDirectory(Path.SD_PATH + dir1);
        AssertThat(!ret, "shouldn't delete dir1 ");
        ret = Path.removeDirectory(Path.SD_PATH + dir1 + dir2);
        AssertThat(ret, "Cannot delete dir2 ");
        ret = Path.removeDirectory(Path.SD_PATH + dir1);
        AssertThat(ret, "Cannot delete dir1 ");
    }

    public boolean run() throws AssertException {
        CreateDirTest();
        RemoveDirTest();
        return true;
    }

}
