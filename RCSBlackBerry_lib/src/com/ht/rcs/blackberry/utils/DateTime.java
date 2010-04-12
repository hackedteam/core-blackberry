/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : DateTime.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.utils;

import java.util.Date;

public class DateTime {
    public static final long TICK = 1; // 100 nano secondi
    public static final long MILLISEC = 10000 * TICK;
    public static final long SECOND = 1000 * MILLISEC;

    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;

    public static final long DAYS_FROM_1601_TO_1970 = 134774;
    public static final long TICSK_FROM_1601_TO_1970 = DAYS_FROM_1601_TO_1970
            * DAY;

    long ticks;

    public DateTime(Date date) {
        long millisecs = date.getTime();
        ticks = millisecs * MILLISEC + TICSK_FROM_1601_TO_1970;
    }

    public Date getDate() {
        Date date = new Date((ticks - TICSK_FROM_1601_TO_1970) / MILLISEC);

        // #ifdef DBC
        Check.ensures((new DateTime(date)).getTicks() == ticks, "Wrong date");
        // #endif
        return date;
    }

    public long getTicks() {
        return ticks;
    }

    public int hiDateTime() {
        int hi = (int) (ticks >> 32);
        return hi;
    }

    public int lowDateTime() {
        int low = (int) (ticks);
        return low;
    }

}
