//#preprocess
package blackberry.location;

import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import blackberry.Listener;
import blackberry.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.RuntimeStore;

public final class LocationHelper {

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
                LocationHelper singleton = new LocationHelper();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }

        }
        return instance;
    }

    private LocationHelper() {
        final Application application = Application.getApplication();
    }
    
    public void locationGPS(final LocationProvider lp,
            final LocationObserver callback, boolean sync) {
        Runnable closure = new Runnable() {
            public void run() {
                //#ifdef DEBUG
                debug.init();
                //#endif
                try {
                    callback.waitingForPoint(true);
                    if (lp.getState() == LocationProvider.AVAILABLE) {
                        //#ifdef DEBUG_TRACE
                        debug.trace("getLocation");
                        //#endif
                        Location loc = lp.getLocation(Conf.GPS_TIMEOUT);
                        callback.newLocation(loc);
                    }
                } catch (LocationException e) {
                    if (e.getMessage() == "Timed out while waiting for GPS") {
                        //#ifdef DEBUG_ERROR
                        debug.error(e.getMessage());
                        //#endif
                    } else {
                        //#ifdef DEBUG_ERROR
                        debug.error(e);
                        //#endif
                    }
                    callback.errorLocation();
                } catch (InterruptedException e) {
                    //#ifdef DEBUG_ERROR
                    debug.error(e);
                    //#endif
                    callback.errorLocation();
                } finally {
                    callback.waitingForPoint(false);
                }
            }
        };
        
        if(sync){
            closure.run();
        }else{
            new Thread(closure).start();
        }
    }

}
