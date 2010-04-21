package tests.unit;

import tests.TestUnit;
import tests.Tests;

public class UT_Self extends TestUnit {

	
	
	public UT_Self(String name, Tests tests) {
		super(name, tests);
	}

	public boolean run() {
		
		//#debug
debug.info("run " + name);
		return true;
	}
}
