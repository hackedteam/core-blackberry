package com.ht.tests;

import java.util.Vector;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.tests.unit.*;

public class Tests {
	static Debug debug=new Debug("Tests", DebugLevel.VERBOSE );

	private static Tests instance=null;
	public synchronized static Tests getInstance() {
		if(instance == null)
			instance = new Tests();
		
		return instance;
	}
	
	private Tests()
	{			
		addTest(new UT_Self("Self", this));
		addTest(new UT_Utils("Utils",this));
		
		addTest(new UT_IMAgent("IMAgent", this));
		addTest(new UT_Log("Log",this));
		addTest(new UT_File("File",this));
		addTest(new UT_Markup("Markup",this));
				
		addTest(new UT_Crypto("Crypto",this));
		addTest(new UT_Conf("Conf",this));
		addTest(new UT_Events("Events",this));
		addTest(new UT_Agents("Agents",this));
						
	}
	
	private void addTest(TestUnit unitTest) {
		testUnits.addElement(unitTest);		
	}

	protected Vector testUnits= new Vector();
	
	public int getCount() {
		
		return testUnits.size();
	}

	public boolean execute(int i) {
		
		TestUnit unit= (TestUnit) testUnits.elementAt(i);
		debug.info("--== Executing: "+unit.name+" ==--");
		boolean ret=unit.execute();
			
		return ret;
	}

	public String result(int i) {
		TestUnit unit= (TestUnit) testUnits.elementAt(i);
		String ret = unit.name +":"+ (unit.passed?"OK":"KO") +":"+unit.result;
		return ret;
	}
		
}
