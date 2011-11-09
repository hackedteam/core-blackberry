//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.action;

import java.io.EOFException;

import net.rim.device.api.util.DataBuffer;
import blackberry.config.ConfAction;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.evidence.Evidence;
import blackberry.utils.WChar;

public class LogAction extends SubAction {

    //#ifdef DEBUG
    static Debug debug = new Debug("LogAction", DebugLevel.VERBOSE);
    //#endif

    private String info;

    public LogAction(ConfAction conf) {
        super(conf);
    }


    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(final Event triggeringEvent) {
        if(info!=null && info.length() > 0){
            Evidence.info(info);
            return true;
        }else{
            return false;
        }
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
