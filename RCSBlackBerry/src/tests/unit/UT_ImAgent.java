package tests.unit;

import java.util.Vector;

import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import blackberry.agent.im.ConversationScreen;
import blackberry.agent.im.Line;

public class UT_ImAgent extends TestUnit {

    public UT_ImAgent(String name_, Tests tests_) {
        super(name_, tests_);

    }

    public boolean run() throws AssertException {
        parseConversationNull();
        parseConversationRubbish();
        parseConversationSimple();
        parseConversationLong();
        parseConversationComplex();
        return true;
    }

    private void parseConversationNull() throws AssertException {
        String conversation = "";

        Vector result = ConversationScreen.parseConversation(conversation);
        AssertNull(result, "not null result");
    }

    private void parseConversationRubbish() throws AssertException {
        String conversation = "Torcione, Whiteberry\n" + "\n" + "Messages:\n"
                + "---------\n";

        Vector result = ConversationScreen.parseConversation(conversation);
        AssertNull(result, "not null result");
    }

    private void parseConversationSimple() throws AssertException {

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

        Line firstLine = (Line) lines.elementAt(0);
        AssertEqual(firstLine.getMessage(), "Scrivo anche a te", "message 1");
        AssertEqual(firstLine.getUser(), "Torcione", "user 1");
        Line secondLine = (Line) lines.elementAt(1);
        AssertEqual(secondLine.getMessage(), "grazie!", "message 2");
        AssertEqual(secondLine.getUser(), "Torcione", "user 2");

    }

    private void parseConversationLong() throws AssertException {

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

        Line firstLine = (Line) lines.elementAt(0);
        AssertEqual(firstLine.getMessage(), "Scrivo anche a te", "message 1");
        AssertEqual(firstLine.getUser(), "Torcione", "user 1");
        Line secondLine = (Line) lines.elementAt(1);
        AssertEqual(secondLine.getMessage(), "grazie!", "message 2");
        AssertEqual(secondLine.getUser(), "Torcione", "user 2");

    }

    private void parseConversationComplex() throws AssertException {

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

        Line firstLine = (Line) lines.elementAt(0);
        AssertEqual(firstLine.getMessage(), "Scrivo anche a te", "message 1");
        AssertEqual(firstLine.getUser(), "Torcione", "user 1");
        Line secondLine = (Line) lines.elementAt(1);
        AssertEqual(secondLine.getMessage(), "grazie!", "message 2");
        AssertEqual(secondLine.getUser(), "Torcione", "user 2");

    }

}
