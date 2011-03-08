package tests.unit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import blackberry.agent.im.ConversationScreen;

public class UT_ImAgent extends TestUnit {

    public UT_ImAgent(String name_, Tests tests_) {
        super(name_, tests_);

    }

    public boolean run() throws AssertException {
        parseConversationNull();
        parseConversationRubbish();
        parseConversationSimple();
        parseConversationLong();
        try {
            parseConversationComplex();
        } catch (IOException e) {

            e.printStackTrace();
            throw new AssertException();
        }
        return true;
    }

    private void parseConversationNull() throws AssertException {
        //#ifdef DEBUG
        debug.trace("parseConversationNull");
        //#endif
        String conversation = "";

        Vector result = ConversationScreen.parseConversation(conversation);
        AssertNull(result, "not null result");
    }

    private void parseConversationRubbish() throws AssertException {
        //#ifdef DEBUG
        debug.trace("parseConversationRubbish");
        //#endif
        String conversation = "Torcione, Whiteberry\n" + "\n" + "Messages:\n"
                + "---------\n";

        Vector result = ConversationScreen.parseConversation(conversation);
        AssertNull(result, "not null result");
    }

    private void parseConversationSimple() throws AssertException {
        //#ifdef DEBUG
        debug.trace("parseConversationSimple");
        //#endif
        String conversation = "Participants:\n" + "-------------\n"
                + "Torcione, Whiteberry\n" + "\n" + "Messages:\n"
                + "---------\n" + "Torcione: Scrivo anche a te\n"
                + "Whiteberry: grazie!\n";

        Vector result = ConversationScreen.parseConversation(conversation);
        AssertNotNull(result, "null result");
        AssertEqual(result.size(), 2, "size");

        String partecipants = (String) result.elementAt(0);
        Vector lines = (Vector) result.elementAt(1);

        AssertEqual(partecipants, "Torcione, Whiteberry", " partecipants");
        AssertEqual(lines.size(), 2, "line size");

        String firstLine = (String) lines.elementAt(0);
        AssertEqual(firstLine, "Torcione: Scrivo anche a te", "message 1");
        String secondLine = (String) lines.elementAt(1);
        AssertEqual(secondLine, "Whiteberry: grazie!", "message 2");

    }

    private void parseConversationLong() throws AssertException {
        //#ifdef DEBUG
        debug.trace("parseConversationLong");
        //#endif
        String conversation = "Participants:\n" + "-------------\n"
                + "Torcione, Whiteberry\n" + "\n" + "Messages:\n"
                + "---------\n" + "Torcione whatever: Scrivo anche a te\n"
                + "Whiteberry\n" + "rulez: grazie!\n"
                + "Torcione whatever: oh yeah\n";

        Vector result = ConversationScreen.parseConversation(conversation);
        AssertNotNull(result, "null result");
        AssertEqual(result.size(), 2, "size");

        String partecipants = (String) result.elementAt(0);
        Vector lines = (Vector) result.elementAt(1);

        AssertEqual(partecipants, "Torcione, Whiteberry", " partecipants");
        AssertEqual(lines.size(), 3, "line size");

        String firstLine = (String) lines.elementAt(0);
        AssertEqual(firstLine, "Torcione whatever: Scrivo anche a te",
                "message 1");
        String secondLine = (String) lines.elementAt(1);
        AssertEqual(secondLine, "Whiteberry rulez: grazie!", "message 2");

    }

    private void parseConversationComplex() throws AssertException, IOException {
        //#ifdef DEBUG
        debug.trace("parseConversationComplex");
        //#endif
        InputStream stream = getClass().getResourceAsStream(
                "./res/ImAgent_Chat1");
        byte[] payload = new byte[stream.available()];
        stream.read(payload);

        String conversation = new String(payload);

        Vector result = ConversationScreen.parseConversation(conversation);
        AssertNotNull(result, "null result");
        AssertEqual(result.size(), 2, "size");

        String partecipants = (String) result.elementAt(0);
        Vector lines = (Vector) result.elementAt(1);

        AssertEqual(partecipants, "Zeno, Whiteberry", " partecipants");
        AssertEqual(lines.size(), 16, "line size");

    }
}
