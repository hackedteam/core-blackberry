/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : DateTime.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.utils;

import java.util.Calendar;
import java.util.Date;

import net.rim.device.api.util.DataBuffer;

// TODO: Auto-generated Javadoc
/**
 * The Class DateTime.
 */
public final class DateTime {
    public static final long TICK = 1; // 100 nano secondi
    public static final long MILLISEC = 10000 * TICK;
    public static final long SECOND = 1000 * MILLISEC;

    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;

    public static final long DAYS_FROM_1601_TO_1970 = 134774;
    public static final long TICSK_FROM_1601_TO_1970 = DAYS_FROM_1601_TO_1970
            * DAY;
    private static final int BASE_YEAR_TM = 1900;

    long ticks;
    Date date;

    /**
     * Instantiates a new date time.
     */
    public DateTime() {
        this(new Date());
    }

    /**
     * Instantiates a new date time.
     * 
     * @param date
     *            the date
     */
    public DateTime(final Date date) {
        this.date = date;
        final long millisecs = date.getTime();
        ticks = millisecs * MILLISEC + TICSK_FROM_1601_TO_1970;
    }

    /**
     * Gets the date.
     * 
     * @return the date
     */
    public Date getDate() {

        // #ifdef DBC
        final Date ldate = new Date((ticks - TICSK_FROM_1601_TO_1970)
                / MILLISEC);
        Check.ensures(ldate.getTime() == date.getTime(), "Wrong getTime()");
        Check.ensures((new DateTime(ldate)).getFiledate() == ticks,
                "Wrong date");
        // #endif

        return date;
    }

    /**
     * Gets the filedate.
     * 
     * @return the filedate, 100 ns starting from 1601
     */
    public long getFiledate() {
        return ticks;
    }

    /**
     * struct tm { int tm_sec; // seconds after the minute [0-60] int tm_min; //
     * minutes after the hour [0-59] int tm_hour; // hours since midnight [0-23]
     * int tm_mday; // day of the month [1-31] int tm_mon; // months since
     * January [0-11] int tm_year; // years since 1900
     * int tm_wday; // days since Sunday [0-6]
     * int tm_yday; // days since January 1 [0-365]
     * int tm_isdst; // Daylight Savings Time flag
     * long tm_gmtoff;// offset from CUT in seconds
     * char *tm_zone; //timezone abbreviation };.
     * 
     * @return the struct tm
     */
    public synchronized byte[] getStructTm() {

        // #ifdef DBC
        Check.requires(date != null, "getStructTm date != null");
        // #endif

        final int tm_len = 9 * 4;
        final byte[] tm = new byte[tm_len];
        final DataBuffer databuffer = new DataBuffer(tm, 0, tm_len, false);

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        databuffer.writeInt(calendar.get(Calendar.SECOND));
        databuffer.writeInt(calendar.get(Calendar.MINUTE));
        databuffer.writeInt(calendar.get(Calendar.HOUR_OF_DAY));

        databuffer.writeInt(calendar.get(Calendar.DAY_OF_MONTH));
        databuffer.writeInt(calendar.get(Calendar.MONTH) + 1);
        databuffer.writeInt(calendar.get(Calendar.YEAR));

        databuffer.writeInt(calendar.get(Calendar.DAY_OF_WEEK)); // days since Sunday [0-6]
        databuffer.writeInt(117); // days since January 1 [0-365]
        databuffer.writeInt(0); // Daylight Savings Time flag
        //databuffer.writeLong(0); // offset from CUT in seconds
        //databuffer.writeInt(0); //timezone abbreviation

        // #ifdef DBC
        Check.ensures(tm.length == tm_len, "getStructTm tm_len");
        // #endif

        return tm;
    }

    /**
     * Hi date time.
     * 
     * @return the int
     */
    public int hiDateTime() {
        final int hi = (int) (ticks >> 32);
        return hi;
    }

    /**
     * Low date time.
     * 
     * @return the int
     */
    public int lowDateTime() {
        final int low = (int) (ticks);
        return low;
    }

}
