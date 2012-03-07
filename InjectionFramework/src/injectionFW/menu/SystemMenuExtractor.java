package injectionFW.menu;

import injectionFW.log.Debug;
import injectionFW.log.DebugLevel;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;

public class SystemMenuExtractor extends SystemMenuInjector {
    public SystemMenuExtractor(int position) {
        super(position);
    }

    //#ifdef DEBUG
    private static Debug debug = new Debug("SystemMenuExtractor",
            DebugLevel.VERBOSE);

    //#endif
    protected String getMenuName() {
        return "Analisys";
    }

    public Object run(Object context) {
        debug.info("SystemMenuExtractor context: " + context);
        Application app = Application.getApplication();
        //#ifdef DEBUG
        debug.trace("run on: " + app);
        //#endif
        
        if(!(app instanceof UiApplication)){
            //#ifdef DEBUG
            debug.trace("run: no UiApplication");
            //#endif
            return null;
        }
        
        Object lock = app.getAppEventLock();
        synchronized (lock) {
            
            UiApplication uiapp = (UiApplication)app;
            Class cl = uiapp.getClass();

            debug.trace("class: " + cl);

            Screen screen = uiapp.getActiveScreen();

            debug.trace("screen: " + screen + " count: "
                    + screen.getUiEngine().getScreenCount());

            MenuWalker.logMenus(screen);

            FieldExplorer explorer = new FieldExplorer();
            explorer.explore(screen);
            //#ifdef DEBUG
            debug.trace("run end");
            //#endif
        }

        return null;
    }

}
