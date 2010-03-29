package com.ht.tests.unit;

import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;


public class UT_IMAgent extends TestUnit {

	public UT_IMAgent(String name, Tests tests) {
		super(name, tests);
	}

	public boolean run() throws AssertException {
		
		debug.info("Eccomi!");
		
		return true;
	}

}
