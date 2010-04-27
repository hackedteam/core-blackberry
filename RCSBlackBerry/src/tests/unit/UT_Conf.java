package tests.unit;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.DataBuffer;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import blackberry.Conf;
import blackberry.Status;
import blackberry.config.InstanceKeys323;

public class UT_Conf extends TestUnit {

    InputStream clearIO_test_1;
    InputStream clearIO_test_2;

    byte[] confBuffer_test_1 = new byte[0];
    byte[] confBuffer_test_2 = new byte[0];

    public UT_Conf(final String name, final Tests tests) {
        super(name, tests);

        clearIO_test_1 = UT_Conf.class
                .getResourceAsStream("../Conf/clearconf_test_1.bin");
        clearIO_test_2 = UT_Conf.class
                .getResourceAsStream("../Conf/clearconf_test_2.bin");
        // encIO=
        // UT_Conf.class.getResourceAsStream("../Conf/encryptedconf.bin");
        // encIO_Big=
        // UT_Conf.class.getResourceAsStream("../Conf/encryptedconf2.bin");
    }

    boolean BellinoTest() throws AssertException {
        //#debug info
	debug.info("-- BellinoTest --");

        final Status statusObj = Status.getInstance();
        statusObj.clear();

        final InputStream encIO = UT_Conf.class
                .getResourceAsStream("../Conf/config_bellino.bin");

        //final byte[] clearBuffer = new byte[10 * 1024];

        // check crypto
        statusObj.clear();
        final Conf conf = new Conf();

        final boolean ret = conf.loadCyphered(encIO, InstanceKeys323.confKey);
        AssertThat(ret == true, "Load failed");

        return true;
    }

    boolean ClearLoad() throws AssertException {
        //#debug info
	debug.info("-- ClearLoad --");

        final byte[] buffer = new byte[1024 * 10];
        try {
            AssertThat(clearIO_test_1 != null, "clearIO");
            AssertThat(clearIO_test_2 != null, "clearIO_Big");

            int len = clearIO_test_1.read(buffer);
            confBuffer_test_1 = Arrays.copy(buffer, 0, len);
            AssertThat(len > 0, "Len <=0");

            len = clearIO_test_2.read(buffer);
            confBuffer_test_2 = Arrays.copy(buffer, 0, len);
            AssertThat(len > 0, "Len <=0");

        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    boolean CrcTest() throws AssertException {
        //#debug info
	debug.info("-- ClearLoad --");

        final DataBuffer databuffer = new DataBuffer(confBuffer_test_1, 0,
                confBuffer_test_1.length, false);
        int len;
        boolean crcOK = false;

        try {
            len = databuffer.readInt();
            final int payloadSize = len - 4;

            final byte[] payload = new byte[payloadSize];
            databuffer.setPosition(0);
            databuffer.readFully(payload);

            databuffer.setPosition(payloadSize);
            final int crcExpected = databuffer.readInt();

            crcOK = Conf.crcVerify(payload, crcExpected);
        } catch (final EOFException e) {
            //#debug
            debug.error("EOFException");
            throw new AssertException();
        }

        return crcOK;
    }

    boolean CryptoLoad_1() throws AssertException {
        //#debug info
	debug.info("-- CryptoLoad_1 --");

        final Status statusObj = Status.getInstance();
        statusObj.clear();

        final InputStream clearIO = UT_Conf.class
                .getResourceAsStream("../Conf/Conf1/clearconf1.bin");
        final InputStream encIO = UT_Conf.class
                .getResourceAsStream("../Conf/Conf1/encryptedconf1.bin");

        byte[] clearBuffer = new byte[10 * 1024];
        // byte[] encBuffer=new byte[10 * 1024];
        try {
            clearIO.read(clearBuffer);
            // clearIO.read(encBuffer);
        } catch (final IOException e) {
            throw new AssertException();
        }

        // check plain
        Conf conf = new Conf();
        boolean ret = conf.parseConf(clearBuffer, 0);
        AssertThat(ret == true, "ParseConf failed");

        clearBuffer = null;

        Vector agents = statusObj.getAgentsList();
        Vector events = statusObj.getEventsList();
        Vector actions = statusObj.getActionsList();
        Vector parameters = statusObj.getParametersList();

        AssertEquals(agents.size(), 14, "Wrong Agent number: " + agents.size());
        AssertEquals(events.size(), 4, "Wrong Events number: " + events.size());
        AssertEquals(actions.size(), 2, "Wrong Actions number: "
                + actions.size());
        AssertEquals(parameters.size(), 4, "Wrong Parameters number: "
                + parameters.size());

        // check crypto
        final byte[] ConfKey = new byte[] { 0x53, (byte) 0x81, 0x2f,
                (byte) 0xda, (byte) 0xec, (byte) 0xfb, (byte) 0xa4,
                (byte) 0xae, 0x79, 0x7e, (byte) 0x94, (byte) 0xa7, 0x42, 0x2b,
                (byte) 0x80, (byte) 0xa7 };

        statusObj.clear();
        conf = new Conf();

        ret = conf.loadCyphered(encIO, ConfKey);
        AssertThat(ret == true, "Load failed");

        agents = statusObj.getAgentsList();
        events = statusObj.getEventsList();
        actions = statusObj.getActionsList();
        parameters = statusObj.getParametersList();

        AssertEquals(agents.size(), 14, "Wrong Agent number: " + agents.size());
        AssertEquals(events.size(), 4, "Wrong Events number: " + events.size());
        AssertEquals(actions.size(), 2, "Wrong Actions number: "
                + actions.size());
        AssertEquals(parameters.size(), 4, "Wrong Parameters number: "
                + parameters.size());

        return true;
    }

    boolean CryptoLoad_2() throws AssertException {
        //#debug info
	debug.info("-- CryptoLoad_2 --");

        final Status statusObj = Status.getInstance();
        statusObj.clear();

        final InputStream clearIO = UT_Conf.class
                .getResourceAsStream("../Conf/Conf2/clearconf2.bin");
        final InputStream encIO = UT_Conf.class
                .getResourceAsStream("../Conf/Conf2/encryptedconf2.bin");

        byte[] clearBuffer = new byte[10 * 1024];
        // byte[] encBuffer=new byte[10 * 1024];
        try {
            clearIO.read(clearBuffer);
            // clearIO.read(encBuffer);
        } catch (final IOException e) {
            throw new AssertException();
        }

        // check plain
        Conf conf = new Conf();
        boolean ret = conf.parseConf(clearBuffer, 0);
        AssertThat(ret == true, "ParseConf failed");

        clearBuffer = null;

        Vector agents = statusObj.getAgentsList();
        Vector events = statusObj.getEventsList();
        Vector actions = statusObj.getActionsList();
        Vector parameters = statusObj.getParametersList();

        AssertEquals(agents.size(), 14, "Wrong Agent number: " + agents.size());
        AssertEquals(events.size(), 2, "Wrong Events number: " + events.size());
        AssertEquals(actions.size(), 2, "Wrong Actions number: "
                + actions.size());
        AssertEquals(parameters.size(), 4, "Wrong Parameters number: "
                + parameters.size());

        // check crypto
        final byte[] ConfKey = new byte[] { 0x29, (byte) 0x92, (byte) 0xf1,
                0x5c, 0x29, 0x42, (byte) 0xb2, 0x73, 0x6d, (byte) 0xf2,
                (byte) 0xaa, (byte) 0x8c, 0x24, (byte) 0xfa, 0x72, (byte) 0xad };

        statusObj.clear();
        conf = new Conf();

        ret = conf.loadCyphered(encIO, ConfKey);
        AssertThat(ret == true, "Load failed");

        agents = statusObj.getAgentsList();
        events = statusObj.getEventsList();
        actions = statusObj.getActionsList();
        parameters = statusObj.getParametersList();

        AssertEquals(agents.size(), 14, "Wrong Agent number: " + agents.size());
        AssertEquals(events.size(), 2, "Wrong Events number: " + events.size());
        AssertEquals(actions.size(), 2, "Wrong Actions number: "
                + actions.size());
        AssertEquals(parameters.size(), 4, "Wrong Parameters number: "
                + parameters.size());

        return true;
    }

    boolean CryptoLoad_3() throws AssertException {
        //#debug info
	debug.info("-- CryptoLoad_3 --");

        final Status statusObj = Status.getInstance();
        statusObj.clear();

        final InputStream clearIO = UT_Conf.class
                .getResourceAsStream("../Conf/Conf3/clearconf3.bin");
        final InputStream encIO = UT_Conf.class
                .getResourceAsStream("../Conf/Conf3/encryptedconf3.bin");

        byte[] clearBuffer = new byte[10 * 1024];
        try {
            clearIO.read(clearBuffer);
        } catch (final IOException e) {
            throw new AssertException();
        }

        // check plain
        Conf conf = new Conf();
        boolean ret = conf.parseConf(clearBuffer, 0);
        AssertThat(ret == true, "ParseConf failed");

        clearBuffer = null;

        Vector agents = statusObj.getAgentsList();
        Vector events = statusObj.getEventsList();
        Vector actions = statusObj.getActionsList();
        Vector parameters = statusObj.getParametersList();

        AssertEquals(agents.size(), 14, "Wrong Agent number: " + agents.size());
        AssertEquals(events.size(), 2, "Wrong Events number: " + events.size());
        AssertEquals(actions.size(), 2, "Wrong Actions number: "
                + actions.size());
        AssertEquals(parameters.size(), 4, "Wrong Parameters number: "
                + parameters.size());

        // check crypto

        final byte[] ConfKey = new byte[] { (byte) 0x97, 0x56, 0x5a,
                (byte) 0x9c, 0x21, (byte) 0xf1, 0x44, (byte) 0xe8, (byte) 0xf5,
                0x50, (byte) 0xff, 0x2b, (byte) 0xf6, (byte) 0x90, 0x20, 0x3c };

        statusObj.clear();
        conf = new Conf();

        ret = conf.loadCyphered(encIO, ConfKey);
        AssertThat(ret == true, "Load failed");

        agents = statusObj.getAgentsList();
        events = statusObj.getEventsList();
        actions = statusObj.getActionsList();
        parameters = statusObj.getParametersList();

        AssertEquals(agents.size(), 14, "Wrong Agent number: " + agents.size());
        AssertEquals(events.size(), 2, "Wrong Events number: " + events.size());
        AssertEquals(actions.size(), 2, "Wrong Actions number: "
                + actions.size());
        AssertEquals(parameters.size(), 4, "Wrong Parameters number: "
                + parameters.size());

        return true;
    }

    boolean ParseConfBigTest() throws AssertException {
        //#debug info
	debug.info("-- ParseConfBigTest --");

        final Status statusObj = Status.getInstance();
        statusObj.clear();

        final Conf conf = new Conf();
        final boolean ret = conf.parseConf(confBuffer_test_2, 0);
        AssertThat(ret == true, "ParseConf failed");

        final Vector agents = statusObj.getAgentsList();
        final Vector events = statusObj.getEventsList();
        final Vector actions = statusObj.getActionsList();
        final Vector parameters = statusObj.getParametersList();

        AssertEquals(agents.size(), 14, "Wrong Agent number: " + agents.size());
        AssertEquals(events.size(), 18, "Wrong Events number: " + events.size());
        AssertEquals(actions.size(), 9, "Wrong Actions number: "
                + actions.size());
        AssertEquals(parameters.size(), 4, "Wrong Parameters number: "
                + parameters.size());

        return true;
    }

    boolean ParseConfTest() throws AssertException {
        //#debug info
	debug.info("-- ParseConfTest --");

        final Status statusObj = Status.getInstance();
        statusObj.clear();

        final Conf conf = new Conf();
        final boolean ret = conf.parseConf(confBuffer_test_1, 0);
        AssertThat(ret == true, "ParseConf failed");

        final Vector agents = statusObj.getAgentsList();
        final Vector events = statusObj.getEventsList();
        final Vector actions = statusObj.getActionsList();
        final Vector parameters = statusObj.getParametersList();

        AssertEquals(agents.size(), 14, "Wrong Agent number");
        AssertEquals(events.size(), 3, "Wrong Events number");
        AssertEquals(actions.size(), 3, "Wrong Actions number");
        AssertEquals(parameters.size(), 4, "Wrong Parameters number");

        return true;
    }

    public boolean run() throws AssertException {

        BellinoTest();

        ClearLoad();
        CrcTest();
        ParseConfTest();
        ParseConfBigTest();

        CryptoLoad_1();
        CryptoLoad_2();
        CryptoLoad_3();

        return true;
    }

}
