//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : BacklightObserver.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry.interfaces;

public interface BacklightObserver extends Observer {
    void onBacklightChange(final boolean status);
}
