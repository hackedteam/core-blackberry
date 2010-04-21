package tests.unit;

import java.util.Vector;

import blackberry.fs.Path;
import blackberry.log.LogCollector;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;

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
		AssertThat(vector.size() >= 0, "Wrong file number");
		
		vector = logCollector.scanForDirLogs(Path.SD_PATH);
		AssertThat(vector.size() >= 0, "Wrong dir number");
				
	}
}


