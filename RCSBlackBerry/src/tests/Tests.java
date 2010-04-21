package tests;

import java.util.Vector;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.DebugWriter;
import tests.unit.*;

public class Tests {
	//#debug
	static Debug debug = new Debug("Tests", DebugLevel.VERBOSE);

	static boolean full = true;

	private static Tests instance = null;

	public synchronized static Tests getInstance() {
		if (instance == null)
			instance = new Tests();

		return instance;
	}

	private Tests() {
		
		if (full) {
			addTest(new UT_Self("Self", this));
			addTest(new UT_Utils("Utils", this));
			addTest(new UT_Crypto("Crypto", this));
			addTest(new UT_Conf("Conf", this));
			
			addTest(new UT_File("File", this));
			addTest(new UT_Markup("Markup", this));

			addTest(new UT_Path("Path", this));
			
			addTest(new UT_Events("Events", this));
			addTest(new UT_Agents("Agents", this));
			
			addTest(new UT_TimerThread("TimerThread", this));
			
			addTest(new UT_Log("Log", this));
			addTest(new UT_LogCollector("LogCollector", this));
			addTest(new UT_Sync("Sync", this));

			addTest(new UT_IMAgent("IMAgent", this));
		}
		
		addTest(new UT_SmsAgent("SmsAgent", this));
				
	}

	private void addTest(TestUnit unitTest) {
		testUnits.addElement(unitTest);
	}

	protected Vector testUnits = new Vector();

	public int getCount() {

		return testUnits.size();
	}

	public boolean execute(int i) {

		TestUnit unit = (TestUnit) testUnits.elementAt(i);
		//#debug
debug.info("--== Executing: " + unit.name + " ==--");

		boolean ret;

		try {
			ret = unit.execute();
		} catch (Exception ex) {
			//#debug
debug.error("Exception: " + ex);
			unit.result += " EXCPT";
			ret = false;
		}

		return ret;
	}

	public String result(int i) {
		TestUnit unit = (TestUnit) testUnits.elementAt(i);
		String resUnit = "OK";
		if (!unit.passed)
			resUnit = "NOT OK";
		String ret = unit.name + ":" + resUnit + ":" + unit.result;
		return ret;
	}

}
