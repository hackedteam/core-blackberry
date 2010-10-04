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
    
    boolean entered = false;
    
    private static long GUID = 0xb6b1c761129a5249L;

    public static LocationHelper getInstance() {
        if (instance == null) {
            instance = (LocationHelper) RuntimeStore.getRuntimeStore().get(GUID);
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
    
    boolean waitingForPoint;
    
    private void locationGPS(final LocationProvider lp, final LocationObserver callback) {
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

        Runnable closure = new Runnable() {
            public void run() {
                //#ifdef DEBUG
                debug.init();
                //#endif
                try {
                    waitingForPoint = true;
                    if (lp.getState() == LocationProvider.AVAILABLE) {
                        //#ifdef DEBUG_TRACE
                        debug.trace("getLocation");
                        //#endif
                        Location loc = lp.getLocation(Conf.GPS_TIMEOUT);
                        callback.newLocation(loc);
                    }
                } catch (LocationException e) {
                    //#ifdef DEBUG_ERROR
                    debug.error(e);
                    //#endif
                } catch (InterruptedException e) {
                    //#ifdef DEBUG_ERROR
                    debug.error(e);
                    //#endif
                } finally {
                    waitingForPoint = false;
                }
            }
        };
        closure.run();
    }

}
