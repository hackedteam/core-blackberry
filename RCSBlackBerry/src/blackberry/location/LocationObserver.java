//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	

package blackberry.location;

import javax.microedition.location.Location;

public interface LocationObserver {
    void newLocation(Location loc);

    void waitingForPoint(boolean value);

    void errorLocation(boolean interrupted);
}
