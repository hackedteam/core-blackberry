package injectionFW.injectors;

import injectionFW.Application.InjectorManager;
import injectionFW.log.Debug;
import injectionFW.log.DebugLevel;
import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.device.api.ui.UiApplication;

public class InjectorSystemMenu extends ApplicationMenuItem {
    //#ifdef DEBUG
    private static Debug debug = new Debug("InjectorSystemMenu",
            DebugLevel.VERBOSE);
    private InjectorManager manager;
    private Injector injector;

    //#endif

    public InjectorSystemMenu(InjectorManager manager, Injector injector) {
        super(0);
        this.manager = manager;
        this.injector=injector;
    }

    public Object run(Object context) {
        //#ifdef DEBUG
        debug.trace("run");
        //#endif
        UiApplication app = UiApplication.getUiApplication();
        //#ifdef DEBUG
        debug.trace("run on: " + app);
        //#endif
      
        injector.setInjectedApp(app);     
        
        return null;
    }

    public String toString() {
        return "Abort";
    }

}
