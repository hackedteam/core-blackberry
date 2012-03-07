package injectionFW.injectors;

import injectionFW.log.Debug;
import injectionFW.log.DebugLevel;
import net.rim.device.api.ui.Screen;

public class MicrosoftLiveInjector extends Injector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("MicrosoftLiveInjector",
            DebugLevel.VERBOSE);

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
