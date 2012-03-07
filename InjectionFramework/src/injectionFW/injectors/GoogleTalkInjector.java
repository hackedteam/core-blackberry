package injectionFW.injectors;

import injectionFW.log.Debug;
import injectionFW.log.DebugLevel;
import net.rim.device.api.ui.Screen;

public class GoogleTalkInjector extends Injector {

    //#ifdef DEBUG
    private static Debug debug = new Debug("GoogleTalkInjector",
            DebugLevel.VERBOSE);
    //#endif
    
    public String getAppName() {        
        return "Google talk";
    }

    public String[] getWantedScreen() {
        return new String[0];
    }

    public void playOnScreen(Screen screen) {
        //#ifdef DEBUG
        debug.trace("playOnScreen: " +screen);
        //#endif
    }

    public String getCodName() {
        return "net_rim_bb_qm_google";
    }

}
