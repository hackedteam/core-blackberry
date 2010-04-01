package com.ht.tests.unit;

import java.util.Vector;

import com.ht.rcs.blackberry.fs.Path;
import com.ht.rcs.blackberry.log.LogCollector;
import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;

public class UT_LogCollector extends TestUnit {

	LogCollector logCollector = LogCollector.getInstance();
	
	public UT_LogCollector(String name, Tests tests) {
		super(name, tests);
	}

	public boolean run() throws AssertException {
		scanTests();
		return true;
	}
	
	public void scanTests() throws AssertException
	{
		Vector vector;
		
		vector = logCollector.scanForLogs(Path.SD_PATH , "1_0");
		AssertThat(vector.size() > 0, "Wrong file number");
		
		vector = logCollector.scanForDirLogs(Path.SD_PATH);
		AssertThat(vector.size() > 0, "Wrong dir number");
				
	}
}


