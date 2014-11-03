//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.config;

public class Cfg {
    

    //==========================================================
    // Static configuration
    
    public static final boolean FETCH_WHOLE_EMAIL = false;

    public static final boolean DEBUG_FLASH = true;
    public static final boolean DEBUG_EVENTS = false;
    public static final boolean DEBUG_OUT = true;
    public static final boolean DEBUG_INFO = false;

    public static boolean SD_ENABLED = false;

    public static final boolean GPS_ENABLED = true;
    public static final int GPS_MAXAGE = -1;
    public static final int GPS_TIMEOUT = 60;
    public static final int CONNECTION_TIMEOUT = 120;

    public static boolean IS_UI = false;
    
    public static final String NEW_CONF = "1";//"newconfig.dat";
    public static final String ACTUAL_CONF = "2";//"config.dat";
    
    //==========================================================
    // Compile configuration, do not edit under this line

    public static final int BUILD_ID = 274;
    public static final String BUILD_TIMESTAMP = "20140924-015405";
    
    public static final String GROUP_NAME =  "Rim Library"; //"Rim Library";
    public static final String MODULE_NAME = "net_rim_bb_lib"; //"net_rim_bb_lib";
    public static final String MODULE_LIB_NAME = "net_rim_bb_lib_base"; //"net_rim_bb_lib_base";

    public static final String RANDOM = "D5253AD163423485";
    
    public static final int VERSION = 2014093001;
    public static final String OSVERSION = "5.0";
    
    public static final boolean PROTOCOL_RESUME = true;
    public static final int PROTOCOL_CHUNK = 65536;

    //==========================================================
    
    //#ifdef BlackBerrySDK4.5.0
    public static final boolean enableMMS = false;
    public static final boolean enableSMSHiding = false;
    public static final int osCompileVersion = 4500;
    //#endif
    
    //#ifdef BlackBerrySDK5.0.0
    public static final boolean enableMMS = true;
    public static final boolean enableSMSHiding = false;
    public static final int osCompileVersion = 5000;
    //#endif
    
}
