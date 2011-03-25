//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : DebugLevel.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.debug;

/**
 * The Class DebugLevel.
 */
public final class DebugLevel {
    //#ifdef DEBUG
    //#endif
    
    public static final int CRITICAL = 1;
    public static final int HIGH = 2;
    public static final int LOW = 3;
    public static final int NOTIFY = 4;
    public static final int VERBOSE = 5;

    public static final int SEVERE_ERROR = 1;
    public static final int ERROR = 2;
    public static final int WARNING = 3;
    public static final int INFORMATION = 4;
    public static final int DEBUG_INFO = 5;

    private DebugLevel() {
    }
}
