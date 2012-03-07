//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : BacklightObserver.java
 * Created      : 26-mar-2010
 * *************************************************/

package injectionFW.Application;

import injectionFW.interfaces.Observer;

public interface BacklightObserver extends Observer {
    //#ifdef DEBUG
    //#endif
    void onBacklightChange(final boolean status);
}
