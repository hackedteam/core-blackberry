package tests.unit;

import blackberry.agent.Agent;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import tests.accessor.TransferAccessor;
import net.rim.blackberry.api.mail.Message;

public class UT_Mail extends TestUnit{

    //#ifdef DEBUG
    static Debug debug = new Debug("UT_Mail", DebugLevel.VERBOSE);
    //#endif
    //String host = "rcs-prod";
    String host = "192.168.1.177";
    int port = 80;

    TransferAccessor transfer;
    
    public UT_Mail(String name, Tests tests) {
        super(name, tests);
    }

    public boolean run() throws AssertException {
        UTF8Mail();
        return false;
    }

    private void UTF8Mail() {
        final Agent agent = Agent.factory(Agent.AGENT_MESSAGE, true, Utils
                .intToByteArray(0));
        
        Message msg = new Message();
        
    }

}
