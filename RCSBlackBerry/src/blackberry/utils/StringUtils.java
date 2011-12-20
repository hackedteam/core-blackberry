//#preprocessor
package blackberry.utils;

import java.util.Vector;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class StringUtils {
    //#ifdef DEBUG
    private static Debug debug = new Debug("StringUtils", DebugLevel.VERBOSE);
    //#endif
    /**
     * split a string into tokens, delimited by the delimiter
     * 
     * @param inString
     * @param delimeter
     * @return
     */
    public static Vector split(String inString, String delimeter) {
        boolean finished = false;
        Vector vec = new Vector();
        try {
            int indexA = 0;
            int indexB = inString.indexOf(delimeter);

            while (indexB != -1) {
                if (indexB > indexA)
                    vec.addElement(new String(inString
                            .substring(indexA, indexB)));
                indexA = indexB + delimeter.length();
                indexB = inString.indexOf(delimeter, indexA);
            }
            vec.addElement(new String(inString.substring(indexA,
                    inString.length())));

        } catch (Exception e) {

        }
        return vec;
    }

    /**
     * Returns the difference between new and last. If last is null, or if
     * there's not a starting equal subset, returns newConversation
     * 
     * @param newConversation
     * @param lastConversation
     * @return
     */
    public static String diffStrings(String newConversation,
            String lastConversation) {
        if (lastConversation != null
                && newConversation.startsWith(lastConversation)) {
            //#ifdef DEBUG
            debug.trace("diffStrings: startsWith");
            //#endif
          
            String diffConversation = newConversation.substring(lastConversation.length());
            return diffConversation;
        } else {
            //#ifdef DEBUG
            debug.trace("diffStrings: return conversation");
            //#endif
            return newConversation;
        }
    }

    /**
     * check if a string is null or empty
     * 
     * @param string
     * @return
     */
    public static boolean empty(String string) {
        return string == null || string.length() == 0;
    }

    /**
     * get the next line of conversation starting from posMessage
     * 
     * @param conversation
     * @param posMessages
     * @return
     */
    public static String getNextLine(String conversation, int posMessages) {

        int endLinePos = conversation.indexOf("\n", posMessages);
        if (endLinePos > 0) {
            return conversation.substring(posMessages, endLinePos);
        } else {
            //#ifdef DEBUG
            debug.trace("getNextLine: no next line");
            //#endif
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
    public static int getLinePos(String conversation, int numLine) {
        int nextLine = 0;
        for (int i = 0; i < numLine; i++) {
            nextLine = conversation.indexOf("\n", nextLine) + 1;
        }
        return nextLine;
    }
}
