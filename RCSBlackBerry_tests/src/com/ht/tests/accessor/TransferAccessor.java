package com.ht.tests.accessor;

import com.ht.rcs.blackberry.transfer.ProtocolException;
import com.ht.rcs.blackberry.transfer.Transfer;

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
		
		sendChallenge();
		getResponse();
		
		disconnect();
	}
}
