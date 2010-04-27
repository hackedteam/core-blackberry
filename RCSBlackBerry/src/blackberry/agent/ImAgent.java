package blackberry.agent;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class ImAgent extends Agent {
	// #debug
	static Debug debug = new Debug("ImAgent", DebugLevel.VERBOSE);

	int loop;

	public ImAgent(final boolean agentStatus) {
		super(Agent.AGENT_IM, agentStatus, true, "ImAgent");
		loop = 0;
		setPeriod(1000);
	}

	protected ImAgent(final boolean agentStatus, final byte[] confParams) {
		this(agentStatus);
		parse(confParams);
	}

	public void actualRun() {
		// #debug debug
		debug.trace("run");

		// verifica che ci siano email *nuove* da leggere

		// per ogni email da leggere

		// genera un log con la email

	}

	protected boolean parse(final byte[] confParameters) {
		// TODO Auto-generated method stub
		return false;
	}

}
