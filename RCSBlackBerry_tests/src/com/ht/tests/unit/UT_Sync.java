package com.ht.tests.unit;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;

public class UT_Sync extends TestUnit {

	static Debug debug=new Debug("UT_Sync", DebugLevel.VERBOSE );

	public UT_Sync(String name, Tests tests) {
		super(name, tests);		
	}

	public boolean run() throws AssertException {
		
		return true;
	}

}
