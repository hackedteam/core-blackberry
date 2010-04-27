package tests.unit;

import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import blackberry.Device;
import blackberry.Status;
import blackberry.agent.Agent;
import blackberry.fs.AutoFlashFile;
import blackberry.fs.Path;
import blackberry.log.Log;
import blackberry.log.LogCollector;
import blackberry.utils.Check;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

public class UT_Log extends TestUnit {

    public UT_Log(final String name, final Tests tests) {
        super(name, tests);
    }

    private void CreateDeviceAgent() {

        // per la 296, logKey = s06El1fQksievo4rtX3XjHWe4lqgxBpZ
        // md5(logKey) = 4e400a3552be73aedb88077cef404314

        /*
         * byte[] logKey = Utils
         * .hexStringToByteArray("4e400a3552be73aedb88077cef404314");
         * Keys.byteAesKey = logKey; Check.asserts(logKey.length == 16,
         * "Wrong md5");
         */

        final Status status = Status.getInstance();
        status.clear();

        final Agent agent = Agent.factory(Agent.AGENT_DEVICE, true, null);

        final Thread thread = new Thread(agent);
        thread.start();
        agent.stop();

        //#debug debug
	debug.trace("Agent Device ok");
    }

    private void CreateEncDeviceLog() {

        final Status status = Status.getInstance();
        status.clear();

        final Agent agent = Agent.factory(Agent.AGENT_DEVICE, true, null);
        final Log agentLog = LogCollector.getInstance().factory(agent, true);

        agentLog.createLog(null);

        final String content = "BlackBerry 8300\n128Kb Ram installed";
        agentLog.writeLog(content, true);

        agentLog.close();

    }

    private void CreatePlainDeviceLog() {
        final Status status = Status.getInstance();
        status.clear();

        final Device device = Device.getInstance();
        device.clear();

        final Agent agent = Agent.factory(Agent.AGENT_DEVICE, true, null);

        final Log agentLog = LogCollector.getInstance().factory(agent, true);

        // agent device vuoto
        final byte[] additionalData = null;
        byte[] plain = agentLog.makeDescription(additionalData);

        //#ifdef DBC
        Check.asserts(plain.length == 32, "Wrong len 1 ");
        //#endif

        AutoFlashFile file = new AutoFlashFile(Path.SD_PATH + "LOG_test1.log",
                false);
        if (file.exists()) {
            file.delete();
        }
        file.create();

        file.append(plain.length);
        file.append(plain);

        // agent device con imsi ecc
        device.refreshData();

        plain = agentLog.makeDescription(additionalData);
        //#ifdef DBC
        Check.asserts(plain.length > 32, "Wrong len 2");
        //#endif

        file = new AutoFlashFile(Path.SD_PATH + "LOG_test2.log", false);
        if (file.exists()) {
            file.delete();
        }
        file.create();

        file.append(plain.length);
        file.append(plain);

        final String chunk = "Processore: Cray\nMemoria: a paccazzi\nOS: BB\nKiodo: gay\n";
        final byte[] bc = WChar.getBytes(chunk);

        // chunk, a pezzi
        file.append(bc);

        // resto del chunk: 4*2 + 2
        final byte[] picche = new byte[] { 0x60, 0x26 };
        final byte[] fiori = new byte[] { 0x61, 0x26 };
        final byte[] cuori = new byte[] { 0x62, 0x26 };
        final byte[] quadri = new byte[] { 0x63, 0x26 };
        file.append(picche);
        file.append(fiori);
        file.append(cuori);
        file.append(quadri);

        file.append(WChar.getBytes("\n"));

        // secondo chunk in arabo
        final String arabicText = "44062706200023064E062A064E0643064E0644064E06510645064F06200027064406520639064E0631064E0628064A064E06510629064E06";
        final String arabicTraslitteration = "\nTraslitterazione: a atakallamu al-'arabi'yah";
        final String arabicTranslation = "\nmettete la salsa bianca nel kebab\n";

        final byte[] arabic = Utils.hexStringToByteArray(arabicText);

        file.append(arabic);
        file.append(WChar.getBytes(arabicTraslitteration));
        file.append(WChar.getBytes(arabicTranslation));
        file.append(0); // string null terminated
    }

    public boolean run() throws AssertException {

        Path.makeDirs(true);

        CreatePlainDeviceLog();
        CreateEncDeviceLog();
        CreateDeviceAgent();
        return true;
    }
}
