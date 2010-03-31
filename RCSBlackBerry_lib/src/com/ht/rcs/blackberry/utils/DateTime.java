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
    public final static long TICK = 1; // 100 nano secondi
    public final static long MILLISEC = 10000 * TICK;
    public final static long SECOND = 1000 * MILLISEC;

    public final static long MINUTE = 60 * SECOND;
    public final static long HOUR = 60 * MINUTE;
    public final static long DAY = 24 * HOUR;

    public final static long DaysFrom1601to1970 = 134774;
    public final static long TicksFrom1601to1970 = DaysFrom1601to1970 * DAY;

    long ticks;

    public DateTime(Date date) {
        long millisecs = date.getTime();
        ticks = millisecs * MILLISEC + TicksFrom1601to1970;
    }

    public Date getDate() {
        Date date = new Date((ticks - TicksFrom1601to1970) / MILLISEC);

        Check.ensures((new DateTime(date)).getTicks() == ticks, "Wrong date");
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
