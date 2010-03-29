package com.ht.tests;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;


public abstract class TestUnit {

	static protected Debug debug=new Debug("TestUnit", DebugLevel.VERBOSE );
	
	Tests tests=null;
	public String name = "test";
	
	public String result = "none";
	public String error = "";
	public boolean executed = false;
	public boolean passed = false;

		
	public TestUnit(String name,Tests tests) {
		this.tests = tests;	
		this.name = name;
	}
		
	public final boolean execute()
	{
		this.executed=true;	
		this.passed=false;
		result="";
		try{
			passed= run();
		}catch(AssertException ex)
		{
			ex.printStackTrace();
			error= ex.toString();			
		}
		return passed;
	}
	
	public abstract boolean run() throws AssertException;
	
	protected void AssertEquals(Object a, Object b, String message) throws AssertException 
	{
		if(!a.equals(b))
		{
			debug.trace(a.toString()+" !+ "+b.toString());			
			this.result="ASSERT: "+message;
			
			debug.fatal(result);
			throw new AssertException();
		}		
	}
	
	protected void AssertEquals(int a, int b, String message) throws AssertException
	{
		AssertEquals(new Integer(a),new Integer(b),message);		
	}
	
	protected void AssertThat(boolean expr, String message) throws AssertException 
	{
		if(!expr)
		{					
			this.result="ASSERT: "+message;

			debug.fatal(result);
			throw new AssertException();
		}		
	}
	
	protected void AssertNotNull(Object obj,String message) throws AssertException
	{
		if(obj == null)
		{
			this.result="ASSERT null: "+message;
			
			debug.fatal(result);
			throw new AssertException();
		}
	}
}
