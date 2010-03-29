package com.ht.tests.unit;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;

public class UT_Self extends TestUnit {
	
	public UT_Self(String name, Tests tests) {
		super(name,tests);				
	}

	public boolean run() {		
		debug.info("run " + name);		
		return true;
	}
}