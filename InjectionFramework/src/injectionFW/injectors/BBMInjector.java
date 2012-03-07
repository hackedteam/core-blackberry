package injectionFW.injectors;

import injectionFW.log.Debug;
import injectionFW.log.DebugLevel;
import net.rim.device.api.ui.Screen;

public class BBMInjector extends Injector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("BBMInjector", DebugLevel.VERBOSE);
    //#endif
    
    public String getAppName() {        
        return "Messenger";
    }

    public String[] getWantedScreen() {
        // TODO Auto-generated method stub
        return new String[0];
    }

    public void playOnScreen(Screen screen) {
        //#ifdef DEBUG
        debug.trace("playOnScreen: " + screen);
        //#endif
    }

    public String getCodName() {
        return "net_rim_bb_qm_peer";
    }

}
