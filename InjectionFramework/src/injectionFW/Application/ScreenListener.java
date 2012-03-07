package injectionFW.Application;

import injectionFW.interfaces.Listener;


public class ScreenListener extends Listener implements ApplicationObserver {

    public ScreenListener(){
        AppListener.getInstance().addApplicationObserver(this);
    }

    public void onApplicationChange(String startedName, String stoppedName,
            String startedMod, String stoppedMod) {
        
        
    }
    
}
