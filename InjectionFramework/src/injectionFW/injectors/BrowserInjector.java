package injectionFW.injectors;

import injectionFW.log.Debug;
import injectionFW.log.DebugLevel;
import net.rim.device.api.ui.Screen;

public class BrowserInjector extends Injector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("BrowserInjector",
            DebugLevel.VERBOSE);
    //#endif
    
    public String getAppName() {        
        return "Browser";
    }

    public String getCodName() {        
        //return "net.rim.device.apps.internal.browser.core.BrowserImpl";
        return "net_rim_bb_browser_daemon";
        //return "net.rim.java.browser";
    }

    public String[] getWantedScreen() {        
        return new String[]{ "net.rim.device.apps.internal.browser.ui.BrowserScreen" };
    }

    public void playOnScreen(Screen screen) {
        //#ifdef DEBUG
        debug.trace("playOnScreen: " + screen);
        //#endif                
    }

}
