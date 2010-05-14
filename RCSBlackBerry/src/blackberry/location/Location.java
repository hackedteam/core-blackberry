//#preprocess
package blackberry.location;

import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import net.rim.device.api.system.Application;

public final class Location implements LocationListener {
    
    private static Location instance;
    public static Location getInstance(){
        if(instance == null){
            instance = new Location();            
        }
        return instance;
    }
    
    private Location(){
        Application application = Application.getApplication();
        
    }
    

    public void locationUpdated(LocationProvider arg0,
            javax.microedition.location.Location arg1) {
        // TODO Auto-generated method stub

    }

    public void providerStateChanged(LocationProvider arg0, int arg1) {
        // TODO Auto-generated method stub

    }

}
