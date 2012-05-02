//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.event;

import blackberry.config.ConfEvent;

public class EventLoop extends Event {

    protected boolean parse(ConfEvent event) {

        return true;
    }

    protected void actualLoop() {

    }

    protected void actualStart() {
        onEnter();
    }

    protected void actualStop() {
        onExit();
    }

}
