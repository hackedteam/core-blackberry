package blackberry.injection.injectors;

import net.rim.device.api.ui.Screen;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class GoogleTalkInjector extends AInjector {

    //#ifdef DEBUG
    private static Debug debug = new Debug("GoogleTalkInjector",
            DebugLevel.VERBOSE);
    //#endif
    
    public boolean enabled(){
        return false;
    }
    
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
