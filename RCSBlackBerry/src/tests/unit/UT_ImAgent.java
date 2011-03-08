package tests.unit;

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
        return true;
    }

    private void parseConversationNull() throws AssertException {
        String conversation="";
        Vector result = ConversationScreen.parseConversation(conversation);
        AssertNull(result, "not null result");
    }

}
