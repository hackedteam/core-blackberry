package tests.unit;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import net.rim.device.api.util.Arrays;
import tests.AssertException;
import tests.Data;
import tests.TestUnit;
import tests.Tests;
import blackberry.Conf;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;

public class UT_Utils extends TestUnit {

    public UT_Utils(final String name, final Tests tests) {
        super(name, tests);
    }

    boolean AsciiTest() throws AssertException {
        AssertEquals(new Character(Utils.ascii(0)), new Character('0'),
                "ASCII(0)");
        AssertEquals(new Character(Utils.ascii(0xa)), new Character('A'),
                "ASCII(A)");
        AssertEquals(new Character(Utils.ascii(0xf)), new Character('F'),
                "ASCII(F)");

        return true;
    }

    public boolean CopyTest() throws AssertException {
        // copia di uguali
        byte[] dest = new byte[123];
        final byte[] src = new byte[123];
        Arrays.fill(src, (byte) 0x0f);

        Utils.copy(dest, src, src.length);
        AssertThat(Arrays.equals(src, dest), "not equal 1");
        AssertThat(dest[0] == (byte) 0x0f, "not 0x0f");

        // copia da grande a piccolo
        dest = new byte[10];
        Utils.copy(dest, 0, src, 0, dest.length);

        final byte[] buffer = new byte[10];
        Arrays.fill(buffer, (byte) 0x0f);

        AssertThat(Arrays.equals(buffer, dest), "not equal 2");

        // copia di parte
        for (int i = 0; i < 10; i++) {
            src[i] = (byte) i;
        }

        Arrays.fill(dest, (byte) 0x0f);
        Utils.copy(dest, 3, src, 5, 2);

        for (int i = 0; i < 10; i++) {
            if (i == 3) {
                AssertThat(dest[i] == (byte) 5, "not 5");
            } else if (i == 4) {
                AssertThat(dest[i] == (byte) 6, "not 6");
            } else {
                AssertThat(dest[i] == 0x0f, "not 0x0f");
            }
        }

        return true;
    }

    boolean CrcTest() throws AssertException {
        byte[] buffer;
        int result, expected;

        // 1
        buffer = new byte[0];
        expected = 0x00;
        result = Utils.crc(buffer);
        AssertEquals(expected, result, "CRC1");

        // 2
        buffer = new byte[100];
        for (int i = 0; i < 100; i++) {
            buffer[i] = (byte) (i + 1);
        }

        final int[] results = new int[10];
        int[] expects = new int[] { 0x0, 0x1, 0x4, 0xf, 0x40, 0x145, 0x7a4,
                0x3583, 0x1ac20, 0xf0d29 };
        for (int i = 0; i < 10; i++) {
            results[i] = Utils.crc(buffer, 0, i);
            AssertEquals(expects[i], results[i], "CRC2 " + i);
        }

        expects = new int[] { 0x0, 0x9683a4, 0xfb58214c, 0x598075bf,
                0x9d9667b9, 0x8ed1cd81, 0x7493338, 0x7f3e6d8f, 0xc318e3b3,
                0x77617634 };
        for (int i = 0; i < 10; i++) {
            results[i] = Utils.crc(buffer, 0, i * 10);
        }

        for (int i = 0; i < 10; i++) {
            AssertEquals(expects[i], results[i], "CRC3 " + i);
        }

        // 3
        expected = 0xA3999D41;
        result = Utils.crc(Data.CONFIG_PAYLOAD);

        AssertEquals(expected, result, "CRC4");

        return true;
    }

    private void DateTimeTest() {
        // 03/25/2010 11:53:34
        // sec from 1601 : 12913948414L
        // sec from 1970 : 1269474814L

        Date date = new Date(1269474814L * 1000);
        final Calendar calendar = Calendar.getInstance(TimeZone
                .getTimeZone("GMT"));
        calendar.setTime(date);

        //#debug
        debug.info(calendar.toString());
        //#ifdef DBC
        Check.asserts(calendar.get(Calendar.YEAR) == 2010, "Wrong year");
        Check.asserts(calendar.get(Calendar.MONTH) == Calendar.MARCH,
                "Wrong Month");
        Check.asserts(calendar.get(Calendar.DAY_OF_MONTH) == 24, "Wrong Day");
        Check.asserts(calendar.get(Calendar.HOUR) == 11, "Wrong Hour");
        Check.asserts(calendar.get(Calendar.MINUTE) == 53, "Wrong Minute");
        Check.asserts(calendar.get(Calendar.SECOND) == 34, "Wrong Second");
        //#endif

        final DateTime dateTime = new DateTime(date);

        final long hi = dateTime.hiDateTime();
        final long lo = dateTime.lowDateTime();
        final long timestamp = (hi << 32) + lo;
        final long ticks = dateTime.getTicks();
        //#ifdef DBC
        Check.asserts(timestamp == ticks, "wrong timestamp 1");
        Check.asserts(timestamp == 12913948414L * DateTime.SECOND,"wrong timestamp 2");
        //#endif
                
        date = dateTime.getDate();
        calendar.setTime(date);
        //#ifdef DBC
        Check.asserts(calendar.get(Calendar.YEAR) == 2010, "Wrong year");
        Check.asserts(calendar.get(Calendar.MONTH) == Calendar.MARCH,
                "Wrong Month");
        Check.asserts(calendar.get(Calendar.DAY_OF_MONTH) == 24, "Wrong Day");
        Check.asserts(calendar.get(Calendar.HOUR) == 11, "Wrong Hour");
        Check.asserts(calendar.get(Calendar.MINUTE) == 53, "Wrong Minute");
        Check.asserts(calendar.get(Calendar.SECOND) == 34, "Wrong Second");
        //#endif
    }

    boolean GetIndexTest() throws AssertException {
        final byte[] payload = Utils.charArrayToByteArray(Data.CONFIG_PAYLOAD);

        final int agentIndex = Utils.getIndex(payload,
                Conf.AGENT_CONF_DELIMITER.getBytes());
        final int eventIndex = Utils.getIndex(payload,
                Conf.EVENT_CONF_DELIMITER.getBytes());
        final int mobileIndex = Utils.getIndex(payload,
                Conf.MOBIL_CONF_DELIMITER.getBytes());
        final int actionIndex = Utils.getIndex(payload, "SICURAMENTENONCE"
                .getBytes());
        final int endofIndex = Utils.getIndex(payload,
                Conf.ENDOF_CONF_DELIMITER.getBytes());

        //#debug
        debug.trace("searchSectionIndex - agentIndex:" + agentIndex);
        //#debug
        debug.trace("searchSectionIndex - eventIndex:" + eventIndex);
        //#debug
        debug.trace("searchSectionIndex - mobileIndex:" + mobileIndex);
        //#debug
        debug.trace("searchSectionIndex - endofIndex:" + endofIndex);

        AssertEquals(agentIndex, 280, "agentIndex");
        AssertEquals(eventIndex, 4, "eventIndex");
        AssertEquals(mobileIndex, 806, "mobileIndex");
        AssertEquals(actionIndex, -1, "actionIndex");
        AssertEquals(endofIndex, 914, "endofIndex");

        return true;
    }

    boolean HexTest() throws AssertException {
        AssertEquals(new Integer(Utils.hex('0')), new Integer(0), "HEX(0)");
        AssertEquals(new Integer(Utils.hex('a')), new Integer(0xa), "HEX(A)");
        AssertEquals(new Integer(Utils.hex('f')), new Integer(0xf), "HEX(F)");

        return true;
    }

    public void IntToByteTest() throws AssertException, IOException {
        final Random random = new Random();

        for (int i = 0; i > 0; i += random.nextInt(Integer.MAX_VALUE / 5)) {
            final byte[] arr = Utils.intToByteArray(i);
            final int value = Utils.byteArrayToInt(arr, 0);

            AssertEquals(value, i, "Not equals: " + i + " != " + value);
        }

        for (int i = Integer.MIN_VALUE; i <= Integer.MAX_VALUE / 2; i += random
                .nextInt(Integer.MAX_VALUE / 5)) {
            final byte[] arr = Utils.intToByteArray(i);

            final byte[] buffer = new byte[10];
            Utils.copy(buffer, 2, arr, 0, 4);

            final int value = Utils.byteArrayToInt(buffer, 2);

            AssertEquals(value, i, "Not equals, offset: " + i + " != " + value);

        }
    }

    public boolean run() throws AssertException {

        DateTimeTest();
        GetIndexTest();
        CrcTest();
        HexTest();
        AsciiTest();
        CopyTest();
        StringSplit();

        try {
            IntToByteTest();
        } catch (final IOException e) {
            throw new AssertException();
        }

        return true;
    }

    private void StringSplit() throws AssertException {
        final String orig = ".1234..5678.9..";
        final String expected = "123456789";

        final String result = Utils.joinString(Utils.splitString(orig, "."));
        AssertThat(result.equals(expected), "StringSplit doesn't work");

    }

}
