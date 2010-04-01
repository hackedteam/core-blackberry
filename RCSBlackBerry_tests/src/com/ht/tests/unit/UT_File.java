package com.ht.tests.unit;

import com.ht.rcs.blackberry.fs.AutoFlashFile;
import com.ht.rcs.blackberry.fs.Path;
import com.ht.rcs.blackberry.utils.Utils;
import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;

public class UT_File extends TestUnit {

	public UT_File(String name, Tests tests) {
		super(name, tests);
	}

	private void FileCreateTest() throws AssertException {
		AutoFlashFile file = new AutoFlashFile(Path.SD_PATH + "testCreate.txt",
				false);
		boolean ret = file.create();
		AssertThat(ret == true, "Cannot create");

		ret = file.exists();
		AssertThat(ret == true, "don't exists");

		file.delete();
		ret = file.exists();
		AssertThat(ret == false, "still exists");
	}

	private void FileCreateHiddenTest() throws AssertException {
		AutoFlashFile file = new AutoFlashFile(Path.SD_PATH + "testHidden.txt",
				true);
		boolean ret = file.create();
		AssertThat(ret == true, "Cannot create");

		ret = file.exists();
		AssertThat(ret == true, "don't exists");

		file.delete();
		ret = file.exists();
		AssertThat(ret == false, "still exists");
	}

	private void PathSDPresentTest() {

		Path.isSDPresent();

	}

	private void FileReadWriteTest() throws AssertException {
		AutoFlashFile file = new AutoFlashFile(Path.SD_PATH + "testRW.txt",
				false);
		boolean ret = file.create();
		AssertThat(ret == true, "Cannot create");

		byte[] read = file.read();
		AssertThat(read.length == 0, "read more than 0");

		file.write(42);
		read = file.read();
		AssertThat(read.length == 4, "read something wrong");

		int value = Utils.byteArrayToInt(read, 0);
		AssertThat(value == 42, "read something different");

		file.delete();
		ret = file.exists();
		AssertThat(ret == false, "still exists");

	}

	private void FileAppendTest() throws AssertException {
		AutoFlashFile file = new AutoFlashFile(Path.SD_PATH + "testAppend.txt",
				false);
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

	public boolean run() throws AssertException {
		FileCreateTest();
		FileCreateHiddenTest();
		FileReadWriteTest();
		FileAppendTest();
		PathSDPresentTest();

		return true;
	}

}
